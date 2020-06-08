package com.rweqx.rest;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.google.gson.JsonObject;
import com.rweqx.authentication.Secured;
import com.rweqx.constants.AuthConstants;
import com.rweqx.exceptions.AuthenticationException;
import com.rweqx.exceptions.ServerException;
import com.rweqx.sql.SecureStore;
import com.rweqx.utils.PropertyUtils;

import javax.annotation.security.PermitAll;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.util.Date;
import java.util.UUID;
import java.util.logging.Logger;

@Path("/")
public class RestLogin {
    private static final Logger LOGGER = Logger.getLogger(RestLogin.class.getName());

    @Inject
    private PropertyUtils properties;

    private final long TIME_TO_EXPIRES_SECONDS = 60 * 60;
    private final long TIME_TO_EXPIRE_MILLIS = TIME_TO_EXPIRES_SECONDS * 1000; // 1 hour.

    @Inject
    private SecureStore secureStore;

    @POST
    @Path("/login")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response authenticateUser(@FormParam("username") String username,
                                     @FormParam("password") String password) {
        LOGGER.severe("Username and password are " + username + " " + password);
        try {
            authenticate(username, password);
            String token = issueToken(username);
            JsonObject object = new JsonObject();
            object.addProperty("token", token);

            LOGGER.severe("Successfully logged in :)");

            String userKey = secureStore.getUserKey(username);
            String sessionId = issueSessionId(userKey);

            return Response.ok(object.toString())
                    // Setting cookie via header method to include sameSite=strict.
                    .header("Set-Cookie", AuthConstants.SESSION_ID_TOKEN + "=" + sessionId + "; HttpOnly; SameSite=strict")
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }
    }

    // TODO - How should I use this... Should it verify if current cookie(s) are valid too ?? :o
    @GET
    @Secured
    @Path("/getSessionIdCookie")
    @PermitAll
    public Response getSessionIdCookie(@Context SecurityContext securityContext) {
        // Issue a new token for the username.
        final String userKey = securityContext.getUserPrincipal().getName();
        String sessionId = issueSessionId(userKey);
        return Response.ok()
                // Setting cookie via header method to include sameSite=strict.
                .header("Set-Cookie", AuthConstants.SESSION_ID_TOKEN + "=" + sessionId + "; HttpOnly; SameSite=strict")
                .build();
    }

    @GET
    @Secured
    @Path("/isAuthenticated")
    @PermitAll
    public Response isAuthenticated() {
        return Response.ok().build();
    }

    private void authenticate(String username, String password) throws Exception {
        if (secureStore.isValidUser(username, password)) {
            return;
        } else {
            throw new AuthenticationException("Invalid username and password combination");
        }
    }

    private String issueToken(String username) {
        String userKey = secureStore.getUserKey(username);
        long nowMillis = System.currentTimeMillis();
        long expMillis = nowMillis + TIME_TO_EXPIRE_MILLIS;
        Date now = new Date(nowMillis);
        Date exp = new Date(expMillis);

        try {
            Algorithm algorithm = Algorithm.HMAC256(properties.getSecretKey());
            String token = JWT.create()
                    .withIssuer(properties.getIssuer())
                    .withIssuedAt(now)
                    .withJWTId(userKey)
                    .withExpiresAt(exp)
                    .sign(algorithm);
            return token;
        } catch (JWTCreationException exception) {
            //Invalid Signing configuration / Couldn't convert Claims.
            throw new ServerException(500, "Token generation failed");
        }
    }

    private String issueSessionId(String userKey) {
        long expMillis = System.currentTimeMillis() + TIME_TO_EXPIRE_MILLIS;
        String uuid = UUID.randomUUID().toString();
        secureStore.registerSessionId(userKey, uuid, expMillis);
        return uuid;
    }

}
