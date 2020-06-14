package com.rweqx.rest;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.rweqx.authentication.AccessType;
import com.rweqx.authentication.AllowCookieAuthentication;
import com.rweqx.authentication.Secured;
import com.rweqx.files.FileBrowserService;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.io.File;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

@Path("/")
@Secured
public class RestSharing {

    private final Logger LOGGER = Logger.getLogger(RestSharing.class.getName());

    @Inject
    private FileBrowserService fileService;

    @AllowCookieAuthentication
    @RolesAllowed("ADMIN")
    @GET
    @Path("/sharedFile")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response getSharedFile(@Context SecurityContext securityContext, @QueryParam("fileId") String fileId) {
        final String userKey = securityContext.getUserPrincipal().getName();
        if (fileId == null) {
            return Response.status(400).entity("Bad request - invalid file id.").build();
        }

        File file = fileService.getSharedFile(userKey, fileId, AccessType.READ);
        if (file == null) {
            Response.status(404).entity("Failed to resolve file.");
        }

        return Response.ok(file, MediaType.APPLICATION_OCTET_STREAM)
                .header("Content-Disposition", "attachment; filename=\"" + file.getName() + "\"")
                .header("Content-Length", String.valueOf(file.length()))
                .build();
    }

    @AllowCookieAuthentication
    @RolesAllowed("ADMIN")
    @POST
    @Path("/shareFile")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response shareFile(@Context SecurityContext securityContext, String json) {
        final String userKey = securityContext.getUserPrincipal().getName();

        JsonObject obj = (new JsonParser()).parse(json).getAsJsonObject();
        final String path = URLDecoder.decode(obj.get("path").getAsString(), StandardCharsets.UTF_8);
        final JsonArray configurations = obj.get("configurations").getAsJsonArray();
        final boolean shouldReturnShareId = obj.get("shouldReturnShareId").getAsBoolean();

        String shareId = fileService.shareFileAndGetId(userKey, path, configurations);

        JsonObject response = new JsonObject();
        if (shouldReturnShareId) {
            response.addProperty("shareId", shareId);
        }

        return Response.ok(response, MediaType.APPLICATION_JSON).build();
    }


    /**
     * Change sharing access to the users provided in the following JSON format
     *
     * @param securityContext
     * @param json
     * @return
     */
    @AllowCookieAuthentication
    @RolesAllowed("ADMIN")
    @POST
    @Path("/updateShareAccess")
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateShareAccess(@Context SecurityContext securityContext, String json) {
        final String userKey = securityContext.getUserPrincipal().getName();
        // TODO


        return Response.ok().build();
    }
}
