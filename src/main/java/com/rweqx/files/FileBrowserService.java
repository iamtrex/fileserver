package com.rweqx.files;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.logging.Logger;

public class FileBrowserService {

    private final Logger LOGGER = Logger.getLogger(FileBrowserService.class.getName());

    private static FileBrowserService instance;

    private final String root = "D:/Server";

    public static FileBrowserService getInstance() {
        if (instance == null) {
            instance = new FileBrowserService();
        }
        return instance;
    }


    public Response getFile(String userKey, String filePath) {
        LOGGER.info("Getting file with path - " + filePath);

        File file = new File(filePath);
        if (userKey == null) {
            return Response.status(403, "User was not found").build();
        }

        String userRoot = root + "/" + userKey;

        if (!file.getPath().startsWith((new File(userRoot)).getPath())) {
            LOGGER.severe(file.getPath());
            LOGGER.severe("Doesn't start with root, might be trolling");
            return Response.status(401, "User does not have authorization to download from this address").build();
        }

        if (file.exists()) {
            return Response.ok(file, MediaType.APPLICATION_OCTET_STREAM)
                    .header("Content-Disposition", "attachment; filename=\"" + file.getName() + "\"")
                    .header("Content-Length", String.valueOf(file.length()))
                    .build();
        } else {
            return Response.status(404, "File was not found").build();
        }
    }

    public JsonObject getFiles(String userKey, String subPath) {
        JsonObject responseObj = new JsonObject();

        if (userKey == null) {
            JsonArray filesArray = new JsonArray();
            responseObj.add("files", filesArray);
            return responseObj;
        }

        String userRoot = root + "/" + userKey;
        String stringPath = subPath;
        if (!subPath.startsWith(userRoot)) {
            if (!subPath.startsWith("/")) {
                subPath = "/" + subPath;
            }
            stringPath = userRoot + subPath;
        }

        LOGGER.info(stringPath);
        File f = new File(stringPath);

        if (f.exists() && f.isDirectory()) {
            System.out.println(f.getAbsolutePath());
            LOGGER.info("Here");

            Path path = f.toPath();
            JsonArray array = Arrays.asList(f.list())
                    .parallelStream()
                    .map(s -> path.resolve(s))
                    .map(this::convertToObject)
                    .collect(
                            JsonArray::new,
                            JsonArray::add,
                            JsonArray::addAll
                    );

            responseObj.add("files", array);
        } else {
            LOGGER.info("Could not find " + stringPath);
        }

        return responseObj;
    }

    private JsonObject convertToObject(Path path) {
        JsonObject object = new JsonObject();

        File file = path.toFile();

        String stringPath = path.toString();
        object.addProperty("name", file.getName());
        object.addProperty("path", stringPath);
        object.addProperty("type", file.isFile() ? "file" : "folder");

        stringPath = stringPath.replace("\\", "/");
        object.addProperty("pathUrl", URLEncoder.encode(stringPath, StandardCharsets.UTF_8));

        if (file.isFile()) {
            String size = String.valueOf(file.length());
            object.addProperty("size", size);
        }

        object.addProperty("thumbnail", FileUtils.getThumnailBase64(file));

        return object;
    }

    private FileBrowserService() {
    }

    public void setupUser(String userKey) {
        String path = root + "/" + userKey;

        File f = new File(path);
        if (f.exists()) {
            LOGGER.severe("This is bad, the file already exists...");
        }

        boolean success = f.mkdirs(); // Create folder for the user.
        LOGGER.info("Making files was a " + success);
    }
}
