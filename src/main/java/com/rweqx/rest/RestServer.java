package com.rweqx.rest;

import com.google.gson.JsonObject;
import com.rweqx.files.FileBrowserService;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/")
public class RestServer {

    @RolesAllowed("ADMIN")
    @GET
    @Path("/files")
    @Produces("application/json")
    public String getFiles(@DefaultValue("") @QueryParam("path") String path) {
        JsonObject object = FileBrowserService.getInstance().getFiles(path);
        return object.toString();
    }

    @RolesAllowed("ADMIN")
    @GET
    @Path("/file")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response getFile1(@DefaultValue("") @QueryParam("path") String path) {
        return FileBrowserService.getInstance().getFile(path);
    }

}
