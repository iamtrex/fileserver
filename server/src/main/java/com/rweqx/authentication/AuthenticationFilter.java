package com.rweqx.authentication;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.rweqx.constants.AuthConstants;
import com.rweqx.sql.SecureStore;
import com.rweqx.utils.PropertyUtils;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.*;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.security.Principal;
import java.util.Date;
import java.util.Map;
import java.util.logging.Logger;

@Secured
@Provider
@Priority(Priorities.AUTHENTICATION)
public class AuthenticationFilter implements ContainerRequestFilter {

    private static final Logger LOGGER = Logger.getLogger(AuthenticationFilter.class.getName());

    @Inject
    private SecureStore secureStore;

    @Context
    private ResourceInfo resourceInfo;

    @Context
    private HttpServletRequest request;

    @Inject
    private PropertyUtils properties;


    private final String REALM = "FileServer"; //TODO remove.

    private static final String AUTHENTICATION_SCHEME = "Bearer";

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        Map<String, Cookie> cookies  = requestContext.getCookies();
        Cookie cookie = cookies.get(AuthConstants.SESSION_ID_TOKEN);

        // Bind annotation to ignore cookie authentication method.
        AllowCookieAuthentication allowCookie = resourceInfo.getResourceMethod().getAnnotation(AllowCookieAuthentication.class);
        if (allowCookie != null) {
            LOGGER.info("Checking cookies");
            // User has a sessionId token. Validate it.
            if (cookie != null) {
                String sessionId = cookie.getValue();

                try {
                    String userKey = secureStore.getUserKeyFromSessionId(sessionId);
                    requestContext.setSecurityContext(
                            createSecurityContext(requestContext.getSecurityContext(), userKey));
                    // Request is validated, user is logged in.
                    return;
                } catch (Exception e) {
                    // Cookie is invalid (either bad or expired)
                    // Check normal auth method...
                    // TODO how to remove invalid cookies when the user has valid auth header though???
                    e.printStackTrace();
                }
            }
        }

        LOGGER.info("Checking auth header");
        String authorizationHeader = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);

        if (!isTokenBasedAuthentication(authorizationHeader)) {
            LOGGER.severe("Client isn't using token-based auth");
            abortWithUnauthorized(requestContext, cookie);
            return;
        }

        String token = authorizationHeader.substring(AUTHENTICATION_SCHEME.length()).trim();

        try {
            final String userKey = validateTokenAndGetUserKey(token);
            requestContext.setSecurityContext(
                    createSecurityContext(requestContext.getSecurityContext(), userKey));

        } catch (Exception e) {
            LOGGER.severe("Client has invalid token");
            abortWithUnauthorized(requestContext, cookie);
            return;
        }
    }

    private boolean isTokenBasedAuthentication(String authorizationHeader) {
        return authorizationHeader != null && authorizationHeader.toLowerCase().startsWith(AUTHENTICATION_SCHEME.toLowerCase() + " ");
    }

    private void abortWithUnauthorized(ContainerRequestContext requestContext, Cookie cookie) {
        // Abort the filter chain with a 401 status code response
        // The WWW-Authenticate header is sent along with the response

        Response.ResponseBuilder builder = Response.status(Response.Status.UNAUTHORIZED)
                        .header(HttpHeaders.WWW_AUTHENTICATE,
                                AUTHENTICATION_SCHEME + " realm=\"" + REALM + "\"");

        // Delete invalidated cookies if they exist.
        if (cookie != null) {
            NewCookie sessionCookie = new NewCookie(cookie, null, 0, false);
            builder.cookie(sessionCookie);
        }

        requestContext.abortWith(builder.build());
    }

    private String validateTokenAndGetUserKey(String token) throws Exception {
        DecodedJWT jwt = JWT.decode(token);

        Date now = new Date(System.currentTimeMillis());
        Date expires = jwt.getExpiresAt();

        if (now.after(expires)) {
            LOGGER.severe("Client token expired");
            throw new Exception();
        }

        if (!jwt.getIssuer().equals(properties.getIssuer())) {
            LOGGER.severe("Client fake issuer");
            throw new Exception();
        }

        if (jwt.getId() == null) {
            LOGGER.severe("Client invalid id.");
            throw new Exception();
        }

        return jwt.getId();
    }

    private SecurityContext createSecurityContext(SecurityContext currentSecurityContext, String userKey) {
        return new SecurityContext() {
            @Override
            public Principal getUserPrincipal() {
                return () -> userKey;
            }

            @Override
            public boolean isUserInRole(String role) {
                return secureStore.isUserInRole(userKey, role);
            }

            @Override
            public boolean isSecure() {
                return currentSecurityContext.isSecure();
            }

            @Override
            public String getAuthenticationScheme() {
                return AUTHENTICATION_SCHEME;
            }
        };
    }

}
