package com.rweqx.utils;

import com.rweqx.exceptions.ServerException;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Handles reading the properties of the server.
 */
public class PropertyUtils {
    private final String PROPERTIES_FILE = "ServerConfigurations.properties";
    private final Properties properties;

    private final String SECRET_KEY;
    private final String SERVER_ROOT;
    private final String ISSUER;

    private final List<String> MANDATORY_PROPERTIES;

    private static final Logger LOGGER = Logger.getLogger(PropertyUtils.class.getName());
    public PropertyUtils() throws ServerException {
        properties = new Properties();

        InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(PROPERTIES_FILE);
        try {
            if (in != null) {
                properties.load(in);
                LOGGER.severe("Could load");
                SECRET_KEY = properties.getProperty("jwtSecretKey");
                SERVER_ROOT = properties.getProperty("fileServerRoot");
                ISSUER = properties.getProperty("issuer");

                MANDATORY_PROPERTIES = List.of(SECRET_KEY, SERVER_ROOT, ISSUER);
            } else {
                LOGGER.severe("Properties file not found");
                throw new ServerException(500, "Server property file was not found");
            }
        } catch (IOException e) {
            LOGGER.severe("Properties server failed to read");
            throw new ServerException(500, "Failed to read server property file.");
        }

        verifyProperties();
        LOGGER.severe("Properties good");
    }

    private void verifyProperties() throws ServerException {
        MANDATORY_PROPERTIES.forEach(s -> {
            if (!isKeyValid(s)) {
                LOGGER.severe("Properties server failed to read");
                throw new ServerException(500, "Server property file is not properly configured");
            }
        });
    }

    private boolean isKeyValid(String s) {
        return !(s == null || s.trim().equals(""));
    }

    public String getSecretKey() {
        return this.SECRET_KEY;
    }
    public String getFileServerRoot() {
        return this.SERVER_ROOT;
    }
    public String getIssuer() {
        return this.ISSUER;
    }
}
