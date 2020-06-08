package com.rweqx.sql;

import com.rweqx.authentication.AuthorizationRoles;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.*;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

public class SecureStore {

    private final String ENCODING_ALGORITHM = "SHA-256";

    private final Logger LOGGER = Logger.getLogger(SecureStore.class.getName());
    private Connection connection;

    private final String USER_TABLE = "users";
    private final String SESSION_TABLE = "sessions";

    public SecureStore(String db, String user, String pass) {
        try {
            DriverManager.registerDriver(new org.apache.derby.jdbc.EmbeddedDriver());
            connection = DriverManager.getConnection(db, user, pass);

            createStore();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates SQL Table as SecureStore if it doesn't already exist.
     */
    private void createStore() {
        LOGGER.info("Trying to create tables");
        PreparedStatement statement;
        try {
            statement = connection.prepareStatement(
                    "CREATE TABLE " + USER_TABLE + " (username VARCHAR(255), password VARCHAR(255), " +
                            "salt VARCHAR(255), user_key VARCHAR(255))");
            statement.execute();

        } catch (SQLException e) {
            if (!e.getSQLState().equalsIgnoreCase("X0Y32")) { // Table already created
                e.printStackTrace();
            }
        }

        try {
            statement = connection.prepareStatement(
                    "CREATE TABLE " + SESSION_TABLE + " (user_key VARCHAR(255), session_id VARCHAR(255), " +
                            "expires_at VARCHAR(255))");
            statement.execute();
        } catch (SQLException e) {
            if (!e.getSQLState().equalsIgnoreCase("X0Y32")) { // Table already created
                e.printStackTrace();
            }
        }
    }

    /**
     * Check if username/password combination exists in our DB.
     * @return - true if valid username password combination.
     */
    public boolean isValidUser(final String username, final String password) {
        try {
            final PreparedStatement statement = connection.prepareStatement(
                    "SELECT username, password, salt FROM " + USER_TABLE + " WHERE username = ?");
            statement.setString(1, username);

            final ResultSet rs = statement.executeQuery();

            if (rs.next()) {
                final String serverHashedPassword = rs.getString(2);
                final String serverSalt = rs.getString(3);
                final String hashedPassword = hashPassword(password,
                        Base64.getDecoder().decode(serverSalt.getBytes(StandardCharsets.UTF_8)));
                // TODO - Remove.
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

    /**
     * Saves the user with salted password into database.
     * @param username
     * @param password
     * @return
     */
    public boolean attemptSaveUser(final String username, final String password) {
        // TODO CHECK TO MAKE SURE USERNAME DOESN'T ALREADY EXIST!
        final byte[] salt = generateSalt();
        final String hashedPassword = hashPassword(password, salt);

        if (hashedPassword == null) {
            LOGGER.severe("Failed to hash password.");
            return false;
        }

        SecureRandom random = new SecureRandom();

        // In theory we should confirm that this doesn't key-clash, even if the probability is < 1/2^63
        final String userKey = String.valueOf(Math.abs(random.nextLong()));

        try {
            PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO " + USER_TABLE +
                            " (username, password, salt, user_key) VALUES (?, ?, ?, ?)");

            statement.setString(1, username);
            statement.setString(2, hashedPassword);
            statement.setString(3, Base64.getEncoder().encodeToString(salt));
            statement.setString(4, userKey);
            statement.executeUpdate();

            return true;
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    /**
     * Get User's unique id key from username.
     * @return
     */
    public String getUserKey(final String username) {
        try {
            final PreparedStatement statement = connection.prepareStatement(
                    "SELECT (user_key) FROM " + USER_TABLE + " WHERE username = ?");
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

    private byte[] generateSalt() {
        final SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        return salt;
    }

    private String hashPassword(final String password, final byte[] salt) {
        try {
            final MessageDigest md = MessageDigest.getInstance(ENCODING_ALGORITHM);
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

    public boolean isUserInRole(String username, String role) {
        // TODO implement.
        return true;
    }

    /**
     *
     * @param userKey
     * @param uuid
     * @param expMillis
     */
    // TODO AGAIN IN THEORY WE SHOULD PREVENT KEY CLASH.
    // TODO - delete old keys from this user?
    public void registerSessionId(String userKey, String uuid, long expMillis) {
        deleteSessionIdsForUserKey(userKey);
        try {
            PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO " + SESSION_TABLE + " (user_key, session_id, expires_at) VALUES (?, ?, ?)");

            statement.setString(1, userKey);
            statement.setString(2, uuid);
            statement.setString(3, String.valueOf(expMillis));
            statement.executeUpdate();

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public String getUserKeyFromSessionId(String sessionId) {
        try {
            final PreparedStatement statement = connection.prepareStatement(
                    "SELECT user_key, expires_at FROM " + SESSION_TABLE + " WHERE session_id = ?");
            statement.setString(1, sessionId);

            final ResultSet rs = statement.executeQuery();

            if (rs.next()) {
                String userKey = rs.getString(1);
                String expiresAt = rs.getString(2);

                long nowMillis = System.currentTimeMillis();
                long expMillis = Long.parseLong(expiresAt);
                java.util.Date now = new java.util.Date(nowMillis);
                java.util.Date exp = new Date(expMillis);

                if (now.after(exp)) {
                    // Delete this invalid session id;
                    deleteSessionId(sessionId);
                    return null;
                }

                return userKey;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void deleteSessionId(String sessionId) {
        try {
            final PreparedStatement statement = connection.prepareStatement(
                    "DELETE FROM " + SESSION_TABLE + " WHERE session_id = ?");
            statement.setString(1, sessionId);
            statement.executeQuery();
        } catch(SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteSessionIdsForUserKey(String userKey) {
        if (userKey == null) {
            return;
        }

        try {
            final PreparedStatement statement = connection.prepareStatement(
                    "DELETE FROM " + SESSION_TABLE + " WHERE user_key = ?");
            statement.setString(1, userKey);
            statement.execute();
        } catch(SQLException e) {
            e.printStackTrace();
        }
    }
}
