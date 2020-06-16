package com.rweqx.files;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.rweqx.authentication.AccessType;
import com.rweqx.exceptions.ServerException;
import com.rweqx.sql.SecureStore;
import com.rweqx.utils.FileUtils;
import com.rweqx.utils.PropertyUtils;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;

import javax.inject.Inject;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class FileBrowserService {
    private final Logger LOGGER = Logger.getLogger(FileBrowserService.class.getName());
    @Inject
    private SecureStore secureStore;
    private PropertyUtils properties;
    private String root;

    public FileBrowserService(PropertyUtils properties) {
        this.properties = properties;
        this.root = properties.getFileServerRoot();
    }

    public Image getFileThumbnail(String userKey, String path) {
        LOGGER.info("Getting thumbnail for file with path - " + path);

        String fullPath = prependPath(userKey, path);
        if (!isPathAccessValid(userKey, fullPath)) {
            throw new ServerException(403, "User is not authorized to read from this path " + path);
        }

        File file = new File(fullPath);
        if (file.exists()) {
            return FileUtils.getThumbnailBase64(file);
        } else {
            throw new ServerException(404, "File was not found");
        }
    }

    /**
     * Gets the file or throws 403 or 404 if user has no access to file or path is bad.
     *
     * @param userKey
     * @param filePath
     * @return
     */
    public File getFile(String userKey, String filePath) {
        LOGGER.info("Getting file with path - " + filePath);

        String fullPath = prependPath(userKey, filePath);
        if (!isPathAccessValid(userKey, fullPath)) {
            throw new ServerException(403, "User is not authorized to read from this path " + filePath);
        }

        File file = new File(fullPath);
        if (file.exists()) {
            return file;
        } else {
            throw new ServerException(404, "File was not found");
        }
    }

    public JsonObject getFiles(String userKey, String folderPath) {
        JsonObject responseObj = new JsonObject();

        if (userKey == null) {
            JsonArray filesArray = new JsonArray();
            responseObj.add("files", filesArray);
            return responseObj;
        }

        String fullPath = prependPath(userKey, folderPath);
        if (!isPathAccessValid(userKey, fullPath)) {
            throw new ServerException(403, "User is not authorized to read from this path " + folderPath);
        }

        File f = new File(fullPath);
        if (f.exists() && f.isDirectory()) {

            Path path = f.toPath();
            JsonArray array = Arrays.asList(f.list())
                    .parallelStream()
                    .map(path::resolve)
                    .map(p -> convertToObject(p, userKey))
                    .collect(
                            JsonArray::new,
                            JsonArray::add,
                            JsonArray::addAll
                    );
            responseObj.add("files", array);
        } else {
            LOGGER.info("Could not find " + fullPath);
        }

        return responseObj;
    }

    private JsonObject convertToObject(Path path, String userKey) {
        JsonObject object = new JsonObject();
        File file = path.toFile();

        String stringPath = path.toString();
        String returnPath = removePrependPath(userKey, stringPath);

        object.addProperty("name", file.getName());
        object.addProperty("path", returnPath);
        object.addProperty("type", file.isFile() ? "FILE" : "FOLDER");

        String urlPath = returnPath.replace("\\", "/");
        object.addProperty("pathUrl", URLEncoder.encode(urlPath, StandardCharsets.UTF_8));

        if (file.isFile()) {
            String size = String.valueOf(file.length());
            object.addProperty("size", size);
        }

        object.addProperty("thumbnail", FileUtils.getIconBase64(file));

        return object;
    }

    /**
     * Setup user by creating their folder in the filesystem.
     *
     * @param userKey - The user's unique identifying key.
     */
    public void setupUser(String userKey) {
        String path = root + userKey;

        File f = new File(path);
        if (f.exists()) {
            LOGGER.severe("This is bad, the file already exists...");
        }

        boolean success = f.mkdirs(); // Create folder for the user.
        LOGGER.info("Making files was a " + success);
    }

    public void uploadFile(String userKey, String rootPath, InputStream fileStream, FormDataContentDisposition fileDetail) {
        String filePath = rootPath + "/" + fileDetail.getFileName();
        String fullPath = prependPath(userKey, filePath);
        LOGGER.info("Saving file with path - " + filePath);

        if (!isPathAccessValid(userKey, fullPath)) {
            throw new ServerException(401, "User does not have authorization to upload to this path " + filePath);
        }
        File file = new File(fullPath);
        try {
            org.apache.commons.io.FileUtils.copyInputStreamToFile(fileStream, file);
        } catch (IOException e) {
            e.printStackTrace();
            throw new ServerException(500, "Failed to write file for some reason");
        }
    }

    public void createObject(String userKey, String type, String directoryPath, String name) {
        String objectPath = directoryPath + "/" + name;
        String fullPath = prependPath(userKey, objectPath);

        if (!isPathAccessValid(userKey, fullPath)) {
            throw new ServerException(403, "User does not have authorization to create to this path " + objectPath);
        }


        // Check the current path is already existing:
        if (!directoryExists(prependPath(userKey, directoryPath))) {
            throw new ServerException(403, "Invalid path " + objectPath);
        }

        switch (type) {
            case "FOLDER":
                createFolder(fullPath);
                break;
            default:
                throw new ServerException(400, "Bad request");
        }
    }

    private void createFolder(String fullPath) {
        File file = new File(fullPath);
        if (file.exists()) {
            throw new ServerException(400, "Already exists");
        }

        file.mkdir();

        if (!file.exists()) {
            throw new ServerException(500, "Failed to create folder");
        }
    }

    private String getPrependKey(String userKey) {
        return new File(root + userKey).toPath().toString();

    }

    private String prependPath(String userKey, String filePath) {
        return getPrependKey(userKey) + filePath;
    }

    private String removePrependPath(String userKey, String filePath) {
        String prependKey = getPrependKey(userKey);

        if (filePath.startsWith(prependKey)) {
            return filePath.substring(filePath.indexOf(prependKey) + prependKey.length());
        } else {
            System.out.println(filePath);
            System.out.println(prependKey);
            throw new ServerException(500, "Somehow the file path lost the prepended key");
        }
    }

    private boolean directoryExists(String path) {
        File file = new File(path);
        return file.exists();
    }

    private boolean isPathAccessValid(String userKey, String path) {
        File file = new File(path);
        String prependKey = root + userKey;

        if (!file.getPath().startsWith((new File(prependKey)).getPath())) {
            LOGGER.severe("User trying to read/write to unauthorized path : " + path);
            return false;
        }
        return true;
    }

    public File getSharedFile(String userKey, String fileId, AccessType requiredAccessType) {
        if (!secureStore.isFileSharedWithUser(userKey, fileId, requiredAccessType)) {
            throw new ServerException(403, "No authorization to access this file");
        }

        OwnedFile ownedFile = secureStore.getOwnedFileFromId(fileId);

        String fullPath = prependPath(ownedFile.getOwner(), ownedFile.getPath());
        File file = new File(fullPath);

        if (!file.exists()) {
            throw new ServerException(500, "File was not found. It may have been lost or corrupted");
        }

        return file;
    }

    // TODO - Need to elaborate behaviour of updating sharing or etc.
    //      Currently you would be able to share but not unshare.

    /**
     * Sets sharing permissions for the following users, returning the id.
     *
     * @param ownerKey       - The owner of the file
     * @param path           - Direct path (excluding prepend).
     * @param configurations
     * @return
     */
    public String shareFileAndGetId(String ownerKey, String path, JsonArray configurations) {
        // Check if file exists. - getFile asserts file existence or error is thrown.
        getFile(ownerKey, path);

        String fileId = null;

        for (JsonElement elt : configurations) {
            JsonObject obj = elt.getAsJsonObject();
            String userKey = obj.get("user").getAsString();
            long expiresInMillis = obj.get("expiresIn").getAsLong();
            String expiresAt = String.valueOf(System.currentTimeMillis() + expiresInMillis);
            AccessType accessType = AccessType.valueOf(obj.get("accessType").getAsString());
            SharedFile file = new SharedFile(userKey, ownerKey, path, accessType, expiresAt);

            String resolvedFileId = secureStore.shareFile(file);

            if (fileId == null) {
                fileId = resolvedFileId;
            } else {
                if (fileId != resolvedFileId) {
                    throw new ServerException(500, "Resolved multple fileIds for the path");
                }
            }
        }

        if (fileId == null) {
            throw new ServerException(500, "Unable to resolve fileId");
        }

        return fileId;
    }

    /**
     * TODO - Keeping this in FBS rather than RestShare because there probably needs be optimization for this or
     * I'd assume the sharing performance is trash once the number of files gets too huge.
     *
     * @param userKey
     * @param filePaths
     * @param configurations
     * @return
     */
    public List<String> shareFilesAndGetId(String userKey, JsonArray filePaths, JsonArray configurations) {

        List<String> ids = new ArrayList<>();
        for (JsonElement elt : filePaths) {
            String path = elt.getAsString();
            String decodedPath = URLDecoder.decode(path, StandardCharsets.UTF_8);
            ids.add(shareFileAndGetId(userKey, decodedPath, configurations));
        }

        return ids;
    }

    public List<String> getAllSharedFiles(String userKey) {
        List<SharedFile> sharedFiles = secureStore.getSharedFiles(userKey);
        return sharedFiles.parallelStream().map(SharedFile::getFileId).collect(Collectors.toList());
    }
}
