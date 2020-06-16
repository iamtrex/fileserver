package com.rweqx.rest;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.rweqx.authentication.AllowCookieAuthentication;
import com.rweqx.authentication.Secured;
import com.rweqx.exceptions.ServerException;
import com.rweqx.files.FileBrowserService;
import com.rweqx.streaming.MultipartFileSender;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.imageio.ImageIO;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.awt.*;
import java.awt.image.RenderedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.logging.Logger;


@Path("/")
@Secured
public class RestFiles {
    private final Logger LOGGER = Logger.getLogger(RestFiles.class.getName());

    @Inject
    private FileBrowserService fileService;

    @RolesAllowed("ADMIN")
    @GET
    @Path("/files")
    @Produces("application/json")
    public String getFiles(@Context SecurityContext securityContext, @DefaultValue("") @QueryParam("path") String path) {
        final String userKey = securityContext.getUserPrincipal().getName();

        JsonObject object = fileService.getFiles(userKey, path);
        return object.toString();
    }

    @AllowCookieAuthentication
    @RolesAllowed("ADMIN")
    @GET
    @Path("/file")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response getFile(@Context SecurityContext securityContext, @DefaultValue("") @QueryParam("path") String path) {
        final String userKey = securityContext.getUserPrincipal().getName();

        File file = fileService.getFile(userKey, path);
        return Response.ok(file, MediaType.APPLICATION_OCTET_STREAM)
                .header("Content-Disposition", "attachment; filename=\"" + file.getName() + "\"")
                .header("Content-Length", String.valueOf(file.length()))
                .build();
    }

    @AllowCookieAuthentication
    @RolesAllowed("ADMIN")
    @GET
    @Path("/thumbnail")
    @Produces("image/png")
    public Response getThumbnail(@Context SecurityContext securityContext, @DefaultValue("") @QueryParam("path") String path) {
        final String userKey = securityContext.getUserPrincipal().getName();

        Image image = fileService.getFileThumbnail(userKey, path);
        if (image == null) {
            return Response.status(500, "Couldn't find image").build();
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            ImageIO.write((RenderedImage) image, "png", out);
            return Response.ok(Base64.getEncoder().encodeToString(out.toByteArray())).build();
        } catch (IOException e) {
            return Response.status(500, "Couldn't copy image").build();
        }
    }

    @RolesAllowed("ADMIN")
    @POST
    @Path("/upload")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response uploadFile(@Context SecurityContext securityContext, @DefaultValue("") @QueryParam("path") String path,
                               @FormDataParam("file") InputStream fileStream, @FormDataParam("file") FormDataContentDisposition fileDetail) {
        final String userKey = securityContext.getUserPrincipal().getName();

        if (fileStream == null || fileDetail == null) {
            return Response.status(400).entity("Invalid form data").build();
        }

        System.out.println(fileDetail.getFileName());
        System.out.println(fileDetail.getSize());
        fileService.uploadFile(userKey, path, fileStream, fileDetail);
        return Response.ok().build();
    }

    @PermitAll
    @POST
    @Path("/create")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response create(@Context SecurityContext securityContext, String json) {
        final String userKey = securityContext.getUserPrincipal().getName();
        JsonObject obj = (new JsonParser()).parse(json).getAsJsonObject();

        final String type = obj.get("type").getAsString();
        final String path = URLDecoder.decode(obj.get("path").getAsString(), StandardCharsets.UTF_8);
        final String name = obj.get("name").getAsString();

        try {
            fileService.createObject(userKey, type, path, name);
            return Response.ok().build();
        } catch (ServerException e) {
            return Response.status(e.getCode()).entity(e.getMessage()).build();
        }
    }

    @AllowCookieAuthentication
    @RolesAllowed("ADMIN")
    @GET
    @Path("/stream")
    public void getStream(@Context HttpServletRequest request, @Context SecurityContext securityContext, @Context HttpServletResponse response, @DefaultValue("") @QueryParam("path") String path) {
        final String userKey = securityContext.getUserPrincipal().getName();

        File file = fileService.getFile(userKey, path);

        try {
            LOGGER.info("Response sending");
            MultipartFileSender.fromFile(file).setRequest(request).setResponse(response).serveResource();
            LOGGER.info("Response sent successfully");
        } catch (Exception e) {
            e.printStackTrace();
        }


        /*
        return Response.status(206)
                .entity(file)
                .type(MediaType.APPLICATION_OCTET_STREAM)
                .header("Content-Disposition", "attachment; filename=\"" + file.getName() + "\"")
                .header("Content-Length", String.valueOf(file.length()))
                .build();*/
    }

}
