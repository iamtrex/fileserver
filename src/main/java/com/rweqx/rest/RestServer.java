package com.rweqx.rest;

import com.google.gson.JsonObject;
import com.rweqx.files.FileBrowserService;

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/")
public class RestServer {

    @RolesAllowed("ADMIN")
    @GET
    @Path("/files")
    @Produces("application/json")
    public String getFiles(@Context HttpServletRequest request, @DefaultValue("") @QueryParam("path") String path) {
        HttpSession session = request.getSession(false);

        final String userKey = (String) session.getAttribute("authenticated-user");

        JsonObject object = FileBrowserService.getInstance().getFiles(userKey, path);
        return object.toString();
    }

    @RolesAllowed("ADMIN")
    @GET
    @Path("/file")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response getFile(@Context HttpServletRequest request, @DefaultValue("") @QueryParam("path") String path) {
        HttpSession session = request.getSession(false);

        final String userKey = (String) session.getAttribute("authenticated-user");

        return FileBrowserService.getInstance().getFile(userKey, path);
    }

}
