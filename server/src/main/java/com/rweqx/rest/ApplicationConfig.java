package com.rweqx.rest;


import com.rweqx.authentication.AuthenticationFilter;
import com.rweqx.files.FileBrowserService;
import com.rweqx.sql.SecureStore;
import com.rweqx.utils.PropertyUtils;
import org.glassfish.jersey.internal.inject.AbstractBinder;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature;

import javax.ws.rs.ApplicationPath;

/**
 * Configurates Server Binding.
 */
@ApplicationPath("rest")
public class ApplicationConfig extends ResourceConfig {
    public ApplicationConfig() {
        register(new ApplicationBinder());
        register(AuthenticationFilter.class);
        register(RolesAllowedDynamicFeature.class);
        register(MultiPartFeature.class);
        packages("com.rweqx.rest");

    }


    // TODO - probably want to secure the DB? :)
    private final String DB = "jdbc:derby:db;create=true";
    private final String USER = "";
    private final String PASS = "";

    private class ApplicationBinder extends AbstractBinder {
        @Override
        protected void configure() {
            PropertyUtils properties = new PropertyUtils();
            bind(properties).to(PropertyUtils.class);
            bind(new SecureStore(DB, USER, PASS)).to(SecureStore.class);
            bind(new FileBrowserService(properties)).to(FileBrowserService.class);
        }
    }
}

