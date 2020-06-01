package com.rweqx.rest;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.rweqx.files.FileBrowserService;
import com.rweqx.sql.SecureStore;

import javax.annotation.security.PermitAll;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class RestSignup {
    @Inject
    private SecureStore secureStore;

    @Inject
    private FileBrowserService fileService;

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
            fileService.setupUser(secureStore.getUserKey(username));
            return Response.ok().build();
        }

        return Response.status(401, "Failed to create user").build();
    }
}
