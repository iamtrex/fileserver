package com.rweqx.rest;

import com.google.gson.JsonObject;
import com.rweqx.files.FileBrowserService;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import javax.annotation.security.RolesAllowed;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

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

        File file = FileBrowserService.getInstance().getFile(userKey, path);
        return Response.ok(file, MediaType.APPLICATION_OCTET_STREAM)
                .header("Content-Disposition", "attachment; filename=\"" + file.getName() + "\"")
                .header("Content-Length", String.valueOf(file.length()))
                .build();
    }

    @RolesAllowed("ADMIN")
    @POST
    @Path("/upload")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response uploadFile(@Context HttpServletRequest request, @DefaultValue("") @QueryParam("path") String path, @FormDataParam("file") InputStream fileStream, @FormDataParam("file") FormDataContentDisposition fileDetail) {
        final HttpSession session = request.getSession(false);
        final String userKey = (String) session.getAttribute("authenticated-user");

        if (fileStream == null || fileDetail == null) {
            return Response.status(400).entity("Invalid form data").build();
        }

        System.out.println(fileDetail.getFileName());
        System.out.println(fileDetail.getSize());
        FileBrowserService.getInstance().uploadFile(userKey, path, fileStream, fileDetail);
        return Response.ok().build();
    }

    /*
    @RolesAllowed("ADMIN")
    @GET
    @Path("/stream")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response getStream(@Context HttpServletRequest request, @DefaultValue("") @QueryParam("path") String path){
        HttpSession session = request.getSession(false);
        final String userKey = (String) session.getAttribute("authenticated-user");

        File file = FileBrowserService.getInstance().getFile(userKey, path);
        return Response.status(206)
                .entity(file)
                .type(MediaType.APPLICATION_OCTET_STREAM)
                .header("Content-Disposition", "attachment; filename=\"" + file.getName() + "\"")
                .header("Content-Length", String.valueOf(file.length()))
                .build();
    }*/

}
