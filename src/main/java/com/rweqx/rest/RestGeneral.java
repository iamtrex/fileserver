package com.rweqx.rest;

import com.google.gson.JsonObject;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

@Path("/")
public class RestGeneral {
    @GET
    @Path("/health")
    @Produces("application/json")
    public String health() {
        JsonObject json = new JsonObject();
        json.addProperty("health", "ok!");
        return json.toString();
    }
}
