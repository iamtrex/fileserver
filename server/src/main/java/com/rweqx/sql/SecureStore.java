package com.rweqx.sql;

import com.rweqx.authentication.AccessType;
import com.rweqx.exceptions.DatabaseException;
import com.rweqx.files.OwnedFile;
import com.rweqx.files.SharedFile;
import com.rweqx.types.User;
import com.rweqx.utils.AuthorizationUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.*;
import java.util.Date;
import java.util.*;
import java.util.logging.Logger;

public class SecureStore {

    private final String ENCODING_ALGORITHM = "SHA-256";

    private final Logger LOGGER = Logger.getLogger(SecureStore.class.getName());
    private final String USER_TABLE = "users";
    private final String SESSION_TABLE = "sessions";
    private final String SHARE_TABLE = "sharedFiles";
    private final String FILE_TABLE = "files";
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

    /**
     * Creates SQL Table as SecureStore if it doesn't already exist.
     */
    private void createStore() {
        LOGGER.info("Trying to create tables");
        attemptCreateTable(USER_TABLE + " (username VARCHAR(255), password VARCHAR(255), " +
                "salt VARCHAR(255), user_key VARCHAR(255))");
        attemptCreateTable(SESSION_TABLE +
                " (user_key VARCHAR(255), session_id VARCHAR(255), expires_at VARCHAR(255))");
        attemptCreateTable(SHARE_TABLE +
                " (file_id VARCHAR(255), user_id VARCHAR(255), access_type VARCHAR(255), expires_at VARCHAR(255))");
        attemptCreateTable(FILE_TABLE + " (file_id VARCHAR(255) NOT NULL, owner VARCHAR(255), file_path VARCHAR(32672), PRIMARY KEY (file_id))");
    }

    /**
     * Attempts to create table.
     * Ignores table already created error(s).
     * <p>
     * // Note - This would be more professional if I passed in the table name and columns as parameters. But I'm too lazy.
     *
     * @param tableStatement - The table name with it's columns in sql form.
     */
    private void attemptCreateTable(String tableStatement) {
        try {
            PreparedStatement statement = connection.prepareStatement("CREATE TABLE " + tableStatement);
            statement.execute();
        } catch (SQLException e) {
            if (!e.getSQLState().equalsIgnoreCase("X0Y32")) { // Table already created
                e.printStackTrace();
            }
        }
    }

    /**
     * Check if username/password combination exists in our DB.
     *
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
     *
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
     *
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
                java.util.Date now = new Date(nowMillis);
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

    public void deleteSessionId(String sessionId) {
        try {
            final PreparedStatement statement = connection.prepareStatement(
                    "DELETE FROM " + SESSION_TABLE + " WHERE session_id = ?");
            statement.setString(1, sessionId);
            statement.executeQuery();
        } catch (SQLException e) {
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
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // TODO probably want to consider what happens if it already exists. How do we update.
    public String shareFile(SharedFile file) {
        String fileId = getFileIdFromPath(file.getOwnerKey(), file.getPath());

        try {
            final PreparedStatement statement = connection.prepareStatement(
                    "INSERT_INTO " + SHARE_TABLE + " (file_id, user_id, access_type, expires_at) VALUES (?, ?, ?, ?)");

            statement.setString(1, fileId);
            statement.setString(2, file.getUserKey());
            statement.setString(3, file.getAccessType().name());
            statement.setString(4, file.getExpiresAtMillis());
            return fileId;
        } catch (SQLException e) {
            throw new DatabaseException(500, "Unable to share file", e);
        }
    }

    /**
     * Gets a fileId for the file at the given path.
     * If none exists, creates one and stores it in the db.
     *
     * @param path
     * @return
     */
    private String getFileIdFromPath(String userKey, String path) {
        try {
            final PreparedStatement statement = connection.prepareStatement(
                    "SELECT file_id FROM" + FILE_TABLE + " WHERE file_path = ? AND owner = ?");

            statement.setString(1, path);
            statement.setString(2, userKey);


            final ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                return rs.getString(1);
            } else {
                // Generates a random UUID for this file.
                final String fileId = UUID.randomUUID().toString();
                registerFileId(fileId, userKey, path);
                return fileId;
            }
        } catch (SQLException e) {
            throw new DatabaseException(500, "Failed to get fileId from path");
        }
    }

    /**
     * Gets a fileId for the file at the given path.
     * If none exists, then throws error.
     *
     * @param
     * @return
     */
    public OwnedFile getOwnedFileFromId(String fileId) {
        try {
            final PreparedStatement statement = connection.prepareStatement(
                    "SELECT (file_path, owner) FROM" + FILE_TABLE + " WHERE file_id = ?");

            statement.setString(1, fileId);

            final ResultSet rs = statement.executeQuery();

            if (!rs.next()) {
                throw new DatabaseException(500, "Failed to get path from fileId - does not exist.");
            }

            String path = rs.getString(1);
            String owner = rs.getString(2);
            return new OwnedFile(owner, path);
        } catch (SQLException e) {
            throw new DatabaseException(500, "DB Error - Failed to get path from fileId");
        }
    }


    // TODO - I think I need to mark fileId as the

    /**
     * Inserts the fileId for path.
     *
     * @param fileId
     * @param path
     */
    private void registerFileId(String fileId, String userKey, String path) {
        try {
            PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO " + FILE_TABLE + " (file_id, owner, file_path) VALUES (?, ?, ?)");
            statement.setString(1, fileId);
            statement.setString(2, userKey);
            statement.setString(3, path);

            int rows = statement.executeUpdate();
            if (rows != 1) {
                throw new DatabaseException(500, "Register FileId updated " + rows + " rows");
            }
        } catch (SQLException e) {
            throw new DatabaseException(500, "Register FileId failed", e);
        }
    }

    /**
     * Gets files shared with the user.
     *
     * @param userKey
     * @return List of files (by path)
     */
    public List<SharedFile> getSharedFiles(String userKey) {
        Date current = new Date(System.currentTimeMillis());
        List<SharedFile> result = new ArrayList<>();
        try {
            final PreparedStatement statement = connection.prepareStatement(
                    "SELECT file_id, access_type, expires_at FROM " + SHARE_TABLE + " WHERE user_id = ?");
            statement.setString(1, userKey);

            final ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                String fileId = rs.getString(1);
                OwnedFile ownedFile = getOwnedFileFromId(fileId);

                String owner = ownedFile.getOwner();
                String path = ownedFile.getPath();
                AccessType accessType = AccessType.valueOf(rs.getString(2));
                String expiresAtMillis = rs.getString(3);

                Date expires = new Date(Long.valueOf(expiresAtMillis));

                if (current.after(expires)) {
                    // TOOD - Queue deletion of sharing right from server?
                    continue;
                }

                result.add(new SharedFile(fileId, userKey, owner, path, accessType, expiresAtMillis));
            }

        } catch (SQLException e) {
            throw new DatabaseException(500, "Failed to get shared files", e);
        }
        return result;
    }

    // TODO - Better to make this throw errors?

    /**
     * Returns file path for the corresponding fileId if it's been shared.
     * throws
     *
     * @param userKey
     * @param requiredAccessType
     * @param fileId
     * @return
     */
    public boolean isFileSharedWithUser(String userKey, String fileId, AccessType requiredAccessType) {
        try {
            final PreparedStatement statement = connection.prepareStatement(
                    "SELECT FROM " + SHARE_TABLE +
                            " (access_type, expires_at) WHERE file_id = ? AND user_id = ?");

            statement.setString(1, fileId);
            statement.setString(2, userKey);

            final ResultSet rs = statement.executeQuery();

            while (rs.next()) {
                AccessType userAccessType = AccessType.valueOf(rs.getString(1));
                if (!AuthorizationUtils.hasAccessRights(requiredAccessType, userAccessType)) {
                    continue;
                }

                Date current = new Date(System.currentTimeMillis());
                Date expires = new Date(Long.valueOf(rs.getString(2)));

                if (current.after(expires)) {
                    // TOOD - Queue deletion of sharing right from server?
                    continue;
                }
                return true;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        try {
            final PreparedStatement statement = connection.prepareStatement(
                    "SELECT (user_key, username) FROM " + USER_TABLE);
            final ResultSet rs = statement.executeQuery();

            while (rs.next()) {
                users.add(new User(rs.getString(1), rs.getString(2)));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }
}
