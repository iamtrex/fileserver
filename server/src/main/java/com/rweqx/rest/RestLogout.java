package com.rweqx.rest;

import com.rweqx.authentication.AllowCookieAuthentication;
import com.rweqx.authentication.Secured;
import com.rweqx.constants.AuthConstants;
import com.rweqx.sql.SecureStore;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.*;

@Path("/")
public class RestLogout {
    @Inject
    private SecureStore secureStore;

    @GET
    @Secured
    @Path("/logout")
    @AllowCookieAuthentication
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response authenticateUser(@Context SecurityContext securityContext, @CookieParam(AuthConstants.SESSION_ID_TOKEN) Cookie cookie) {
        // Delete session token and stuff from server-side.
        final String userKey = securityContext.getUserPrincipal().getName();
        secureStore.deleteSessionIdsForUserKey(userKey);

        Response.ResponseBuilder builder = Response.ok();
        if (cookie != null) {
            NewCookie sessionCookie = new NewCookie(cookie, null, 0, false);
            builder.cookie(sessionCookie);
        }

        return builder.build();
    }
}
