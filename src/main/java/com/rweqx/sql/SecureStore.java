package com.rweqx.sql;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.*;
import java.util.Base64;
import java.util.logging.Logger;

public class SecureStore {

    private final Logger LOGGER = Logger.getLogger(SecureStore.class.getName());
    private Connection connection;

    public SecureStore(String db, String user, String pass) {
        try {
            DriverManager.registerDriver(new org.apache.derby.jdbc.EmbeddedDriver());
            connection = DriverManager.getConnection(db, user, pass);

            createStore();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void createStore() {
        try {
            LOGGER.info("Trying to create table");
            PreparedStatement statement = connection.prepareStatement("CREATE TABLE users (username VARCHAR(255), password VARCHAR(255), salt VARCHAR(255), user_key VARCHAR(255))");
            boolean s = statement.execute();
        } catch (SQLException e) {
            if (!e.getSQLState().equalsIgnoreCase("X0Y32")) {
                e.printStackTrace(); //Ignore fail?
            }
        }
    }

    public boolean isValidUser(final String username, final String password) {

        try {
            final PreparedStatement statement = connection.prepareStatement("SELECT username, password, salt FROM users WHERE username = ?");
            statement.setString(1, username);

            final ResultSet rs = statement.executeQuery();

            if (rs.next()) {
                final String serverHashedPassword = rs.getString(2);
                final String serverSalt = rs.getString(3);
                final String hashedPassword = hashPassword(password, Base64.getDecoder().decode(serverSalt.getBytes(StandardCharsets.UTF_8)));
                LOGGER.info("Salted hash from server was " + serverHashedPassword);
                LOGGER.info("Server salt was " + serverSalt);
                LOGGER.info("Salted hash for password was " + hashedPassword);
                return serverHashedPassword.equals(hashedPassword);
            }
            return false;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private byte[] generateSalt() {
        final SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        return salt;
    }

    private String hashPassword(final String password, final byte[] salt) {
        try {
            final MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt);

            byte[] hashedPassword = md.digest(password.getBytes(StandardCharsets.UTF_8));

            final StringBuilder builder = new StringBuilder();
            for (byte b : hashedPassword) {
                builder.append(String.format("%02x", b));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return null;
    }

    public String getUserKey(final String username) {

        try {
            final PreparedStatement statement = connection.prepareStatement("SELECT (user_key) FROM users WHERE username = ?");
            statement.setString(1, username);

            final ResultSet rs = statement.executeQuery();

            if (rs.next()) {
                return rs.getString(1);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }


    public boolean attemptSaveUser(final String username, final String password) {
        final byte[] salt = generateSalt();
        final String hashedPassword = hashPassword(password, salt);

        if (hashedPassword == null) {
            LOGGER.severe("Failed to hash password.");
            return false;
        }

        SecureRandom random = new SecureRandom();
        final String userKey = String.valueOf(Math.abs(random.nextLong()));

        try {
            PreparedStatement statement = connection.prepareStatement("INSERT INTO users (username, password, salt, user_key) VALUES (?, ?, ?, ?)");

            statement.setString(1, username);
            statement.setString(2, hashedPassword);
            statement.setString(3, Base64.getEncoder().encodeToString(salt));
            statement.setString(4, userKey);
            statement.executeUpdate();

            statement = connection.prepareStatement("SELECT * FROM users");
            ResultSet set = statement.executeQuery();

            while (set.next()) {
                LOGGER.info(set.getString(1));
                LOGGER.info(set.getString(2));
                LOGGER.info(set.getString(3));
                LOGGER.info(set.getString(4));
            }
            return true;
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return false;
    }
}
