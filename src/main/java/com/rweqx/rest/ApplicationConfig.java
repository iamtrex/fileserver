package com.rweqx.rest;


import com.rweqx.authentication.AuthenticationFilter;
import org.glassfish.jersey.server.ResourceConfig;

import javax.ws.rs.ApplicationPath;

@ApplicationPath("rest")
public class ApplicationConfig extends ResourceConfig {
    public ApplicationConfig() {
        packages("com.rweqx.rest");
        register(AuthenticationFilter.class);
    }
}
