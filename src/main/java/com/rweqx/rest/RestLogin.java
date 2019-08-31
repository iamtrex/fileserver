package com.rweqx.rest;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.rweqx.files.FileBrowserService;
import com.rweqx.sql.SecureStore;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/")
public class RestLogin {

    @Inject
    private SecureStore secureStore;


    @PermitAll
    @Path("/signup")
    @POST
    @Produces("application/json")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response signup(String json) {
        JsonParser parser = new JsonParser();
        JsonObject obj = parser.parse(json).getAsJsonObject();
        final String username = obj.get("username").getAsString();
        final String password = obj.get("password").getAsString();

        boolean success = secureStore.attemptSaveUser(username, password);

        if (success) {
            // Create user's folder.
            FileBrowserService.getInstance().setupUser(secureStore.getUserKey(username));
            return Response.ok().build();
        }

        return Response.status(401, "Failed to create user").build();
    }


    @Path("/login")
    @RolesAllowed("ADMIN")
    @GET
    @Produces("application/json")
    public Response login(@CookieParam("session-token") Cookie cookie, @Context HttpServletRequest request) {
        if (request.getSession(false) != null) {
            System.out.println("Has a session!");
        } else {
            System.out.println("Doesnt' have a session!");
        }

        if (cookie != null) {
            System.out.println("Hello world it has a cookie!");
        }
        return Response.ok().build();
        // Return a session token since it's authenticated.
        //NewCookie newCookie = new NewCookie("session-token", "123");
        //return Response.ok("OK").cookie(newCookie).build();
    }
}
