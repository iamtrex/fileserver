package com.rweqx.rest;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.google.gson.JsonObject;
import com.rweqx.authentication.Secured;
import com.rweqx.exceptions.AuthenticationException;
import com.rweqx.exceptions.ServerException;
import com.rweqx.sql.SecureStore;
import com.rweqx.utils.PropertyUtils;

import javax.annotation.security.PermitAll;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Date;
import java.util.logging.Logger;

@Path("/")
public class RestLogin {
    private static final Logger LOGGER = Logger.getLogger(RestLogin.class.getName());

    @Inject
    private PropertyUtils properties;

    private final long TIME_TO_EXPIRE_MILLIS = 60 * 60 * 1000; // 1 hour.

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
            return Response.ok(object.toString()).build();
        } catch (Exception e) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }
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

}
