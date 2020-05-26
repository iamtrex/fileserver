package com.rweqx.rest;


import com.rweqx.authentication.AuthenticationFilter;
import com.rweqx.sql.SecureStore;
import org.glassfish.jersey.internal.inject.AbstractBinder;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;

import javax.ws.rs.ApplicationPath;

/**
 * Configurates Server Binding.
 */
@ApplicationPath("rest")
public class ApplicationConfig extends ResourceConfig {
    public ApplicationConfig() {
        packages("com.rweqx.rest");
        register(AuthenticationFilter.class);
        register(MultiPartFeature.class);
        register(new ApplicationBinder());

    }


    // TODO - probably want to secure the DB? :)
    private final String DB = "jdbc:derby:db;create=true";
    private final String USER = "";
    private final String PASS = "";

    private class ApplicationBinder extends AbstractBinder {
        @Override
        protected void configure() {
            bind(new SecureStore(DB, USER, PASS)).to(SecureStore.class);
        }
    }
}

