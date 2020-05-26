package com.rweqx.rest;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.rweqx.authentication.AuthenticationFilter;
import com.rweqx.files.FileBrowserService;
import com.rweqx.sql.SecureStore;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.util.logging.Logger;

@Path("/")
public class RestLogin {

    private static final Logger LOGGER  =  Logger.getLogger(RestLogin.class.getName());

    @Inject
    private SecureStore secureStore;

    @PermitAll
    @Path("/logout")
    @GET
    public Response logout(@CookieParam("session-token") Cookie cookie, @Context HttpServletRequest request) {
        // Clear HTTP Session and cookies.

        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }

        NewCookie newCookie = new NewCookie("session-token", null, "/", null, null, 0, false, true);

        return Response.ok().cookie(newCookie).build();
    }

    /**
     * Signs up the user by creating appropriate entries and folder structure within the db.
     * @param json
     * @return
     */
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
            FileBrowserService.getInstance().setupUser(secureStore.getUserKey(username));
            return Response.ok().build();
        }

        return Response.status(401, "Failed to create user").build();
    }


    // TODO - Rewrite. Do not use cookie-based auth. Also this request shouldn't be the one setting such auths.
    @Path("/login")
    @RolesAllowed("ADMIN")
    @GET
    @Produces("application/json")
    public Response login(@CookieParam("session-token") Cookie cookie, @Context HttpServletRequest request) {
        String stayLoggedIn = request.getHeader("stay-logged-in");
        if (stayLoggedIn != null && stayLoggedIn.equalsIgnoreCase("TRUE")) {
            // Set cookies.
            // TODO - Can't actaully set cookies here. Would have to set in the login request/
            NewCookie newCookie = new NewCookie("session-token", "123", "/", null, null, 60 * 60 * 24 * 7, false, true);
            return Response.ok(newCookie).build();
        }
        return Response.ok().build();
    }
}
