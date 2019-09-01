package com.rweqx.rest;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.rweqx.files.FileBrowserService;
import com.rweqx.sql.SecureStore;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.*;
import javax.ws.rs.core.*;

@Path("/")
public class RestLogin {

    @Inject
    private SecureStore secureStore;

    @PermitAll
    @Path("/logout")
    @GET
    public Response logout(@CookieParam("session-token") Cookie cookie, @Context HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }

        NewCookie newCookie = new NewCookie("session-token", null, "/", null, null, 0, false, true);

        return Response.ok().cookie(newCookie).build();
    }

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

        String stayLoggedIn = request.getHeader("stay-logged-in");
        if (stayLoggedIn != null && stayLoggedIn.equalsIgnoreCase("TRUE")) {
            // Set cookies.
            // TODO - Can't actaully set cookies here. Would have to set in the login request/
            NewCookie newCookie = new NewCookie("session-token", "123", "/", null, null, 60*60*24*7, false, true);
            return Response.ok(newCookie).build();
        }
        return Response.ok().build();
    }
}
