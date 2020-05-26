package com.rweqx.files;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.rweqx.exceptions.ServerException;
import com.rweqx.utils.FileUtils;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.logging.Logger;

public class FileBrowserService {

    private final Logger LOGGER = Logger.getLogger(FileBrowserService.class.getName());

    private static FileBrowserService instance;
    private final String root = "D:/ServerNew/";

    public static FileBrowserService getInstance() {
        if (instance == null) {
            instance = new FileBrowserService();
        }
        return instance;
    }

    public Image getFileThumbnail(String userKey, String path) {
        LOGGER.info("Getting thumbnail for file with path - " + path);

        String fullPath = prependPath(userKey, path);
        if (!isPathAccessValid(userKey, fullPath)) {
            throw new ServerException(403, "User is not authorized to read from this path " + path);
        }

        File file = new File(fullPath);
        if (file.exists()) {
            return FileUtils.getThumnailBase64(file);
        } else {
            throw new ServerException(404, "File was not found");
        }
    }

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
                    .map(s -> path.resolve(s))
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

    private FileBrowserService() {
    }

    /**
     * Setup user by creating their folder in the filesystem.
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
            throw new ServerException(401, "User does not have authorization to download from this address " + filePath);
        }
        File file = new File(fullPath);
        try {
            org.apache.commons.io.FileUtils.copyInputStreamToFile(fileStream, file);
        } catch (IOException e) {
            e.printStackTrace();
            throw new ServerException(500, "Failed to write file for some reason");
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

    private boolean isPathAccessValid(String userKey, String path) {
        File file = new File(path);
        String prependKey = root + userKey;

        if (!file.getPath().startsWith((new File(prependKey)).getPath())) {
            LOGGER.severe("User trying to read/write to unauthorized path : " + path);
            return false;
        }
        return true;
    }

}
