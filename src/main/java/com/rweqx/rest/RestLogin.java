package com.rweqx.rest;

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.CookieParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.Response;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Path("/login")
public class RestLogin {

    @RolesAllowed("ADMIN")
    @GET
    @Produces("application/json")
    public Response login(@CookieParam("session-token") Cookie cookie, @Context HttpServletRequest request) {
        if (request.getSession(false) != null) {
            System.out.println("Has a session!");
        } else {
            System.out.println("Doesnt' have a session!");
        }

        HttpSession session = request.getSession();
        if (session.getAttribute("authentication-token") == null) {
            session.setAttribute("authentication-token", "AUTH!"); // Set session attribute.
            LocalDateTime date = LocalDateTime.now().plus(1, ChronoUnit.MINUTES);

            session.setAttribute("authentication-expiry-date", date);
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
