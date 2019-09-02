package com.rweqx.authentication;

import com.rweqx.sql.SecureStore;

import javax.annotation.security.DenyAll;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;


@Provider
public class AuthenticationFilter implements ContainerRequestFilter {

    private final int LOGIN_DURATION = 30; //Minutes.

    @Inject
    private SecureStore secureStore;

    @Context
    private ResourceInfo resourceInfo;

    @Context
    private HttpServletRequest request;

    private static final String AUTHORIZATION_PROPERTY = "Authorization";
    private static final String AUTHORIZATION_SCHEME = "Basic";

    @Override
    public void filter(ContainerRequestContext requestContext) {
        final Method method = resourceInfo.getResourceMethod();

        if (!method.isAnnotationPresent(PermitAll.class)) {
            if (method.isAnnotationPresent(DenyAll.class)) {
                System.out.println("Access blocked!");
                requestContext.abortWith(Response.status(Response.Status.FORBIDDEN).entity("Access blocked").build());
                return;
            }
        }

        if (!method.isAnnotationPresent(RolesAllowed.class)) {
            System.out.println("Warning - method is not protected.");
            return;
        }

        final RolesAllowed rolesAnnotation = method.getAnnotation(RolesAllowed.class);
        final Set<String> rolesSet = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(rolesAnnotation.value())));

        if (hasValidSession(requestContext, rolesSet)) {
            System.out.println("Authenticated with Session");
            return;
        } else if (hasValidCookies(requestContext, rolesSet)) {
            System.out.println("Authenticated with Cookies");
            return;
        } else if (hasValidBasicAuthentication(requestContext, rolesSet)) {
            System.out.println("Authenticated with Username and Password");

            setSession(requestContext);
            return;
        }

        System.out.println("Aborting cuz failed all auths!");
        requestContext.abortWith(Response.status(Response.Status.FORBIDDEN).entity("Authorization is missing").build());
    }

    private void setSession(final ContainerRequestContext requestContext) {
        HttpSession session = request.getSession();
        if (session.getAttribute("authenticated-user") == null) { // Necessary?
            final StringTokenizer tokenizer = getAuthentiacationTokenizer(requestContext);
            final String user = tokenizer.nextToken();

            final String key = secureStore.getUserKey(user);
            if (key == null) {
                requestContext.abortWith(Response.status(Response.Status.FORBIDDEN).entity("User key invalid.").build());
            }

            session.setAttribute("authenticated-user", key);

            session.setAttribute("authentication-token", "AUTH!");
            LocalDateTime date = LocalDateTime.now().plus(LOGIN_DURATION, ChronoUnit.MINUTES);

            session.setAttribute("authentication-expiry-date", date);
        } else {
            System.out.println("Somehow already has a session!!!");
        }
    }


    private boolean hasValidSession(final ContainerRequestContext requestContext, final Set<String> rolesSet) {
        HttpSession session = request.getSession(false);

        if (session == null) {
            return false;
        }

        String value = (String) session.getAttribute("authentication-token");
        LocalDateTime date = (LocalDateTime) session.getAttribute("authentication-expiry-date");
        if (value == null || date == null) {
            return false;
        }

        // TODO have this actually map to a user's valid auth token and check rolesSet
        boolean valid = value.equals("AUTH!") && date.isAfter(LocalDateTime.now());
        if (!valid) {
            // Invalidate the session officially.
            session.invalidate();
        }

        System.out.println("Session will last until: " + date.toString());
        return valid;
    }


    private boolean hasValidCookies(final ContainerRequestContext requestContext, final Set<String> rolesSet) {
        final Map<String, Cookie> cookies = requestContext.getCookies();
        if (cookies == null || cookies.get("session-token") == null) {
            return false;
        }

        //TODO actually map user to their roles and compare to rolesSet
        return isCookieValid(cookies.get("session-token"), rolesSet);
    }

    private boolean isCookieValid(final Cookie sessionToken, final Set<String> rolesSet) {
        // TODO have to actually map this to user and get their roles, and then compare to rolesSet
        if (sessionToken.getValue().equals("123")) {
            return true;
        }
        return false;
    }

    private StringTokenizer getAuthentiacationTokenizer(final ContainerRequestContext requestContext) {
        final MultivaluedMap<String, String> headers = requestContext.getHeaders();
        final List<String> auth = headers.get(AUTHORIZATION_PROPERTY);

        if (auth == null || auth.isEmpty()) {
            return null;
        }

        final String encodedUserPassword = auth.get(0).replaceFirst(AUTHORIZATION_SCHEME + " ", "");
        final String userAndPassword = new String(Base64.getDecoder().decode(encodedUserPassword.getBytes()));

        return new StringTokenizer(userAndPassword, ":");
    }

    private boolean hasValidBasicAuthentication(final ContainerRequestContext requestContext, final Set<String> rolesSet) {
        final StringTokenizer tokenizer = getAuthentiacationTokenizer(requestContext);
        if (tokenizer == null) {
            return false;
        }
        final String user = tokenizer.nextToken();
        final String pass = tokenizer.nextToken();

        //Is user valid?
        if (!isUserAllowed(user, pass, rolesSet)) {
            return false;
        }
        return true;
    }


    private boolean isUserAllowed(final String username, final String password, final Set<String> rolesSet) {
        // TODO actually map to users and check if the hash'd password matches the documentation.
        boolean isAllowed = false;

        if (secureStore.isValidUser(username, password)) {
            return true;
            //TODO roles.
        }

        if (username.equals("localhcpuser") && password.equals("Password1")) {
            String userRole = "ADMIN";
            if (rolesSet.contains(userRole)) {
                isAllowed = true;
            }
        } else if (username.equals("cherie") && password.equals("123")) {
            String userRole = "ADMIN";
            if (rolesSet.contains(userRole)) {
                isAllowed = true;
            }
        }
        return isAllowed;
    }
}
