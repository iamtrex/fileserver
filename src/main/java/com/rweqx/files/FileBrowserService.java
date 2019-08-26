package com.rweqx.files;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Logger;

public class FileBrowserService {

    private final Logger LOGGER = Logger.getLogger(FileBrowserService.class.getName());

    private static FileBrowserService instance;

    private String root = "D:/Server";

    public static FileBrowserService getInstance() {
        if (instance == null) {
            instance = new FileBrowserService();
        }
        return instance;
    }


    public Response getFile(String filePath) {
        LOGGER.info("Getting file with path - " + filePath);

        File file = new File(filePath);
        if (!file.getPath().startsWith(root)) {
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

    public JsonObject getFiles(String subPath) {
        JsonObject responseObj = new JsonObject();
        JsonArray filesArray = new JsonArray();
        responseObj.add("files", filesArray);

        String path = subPath;
        if (!subPath.startsWith(root)) {
            if (!subPath.startsWith("/")) {
                subPath = "/" + subPath;
            }
            path = root + subPath;
        }

        LOGGER.info(path);
        File f = new File(path);

        if (f.exists() && f.isDirectory()) {
            System.out.println(f.getAbsolutePath());
            LOGGER.info("Here");
            try {
                Files.list(Paths.get(path))
                        .map(this::convertToObject)
                        .forEach(filesArray::add);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return responseObj;
    }

    private JsonObject convertToObject(Path p) {
        JsonObject object = new JsonObject();

        File file = p.toFile();

        String path = p.toString();
        object.addProperty("name", file.getName());
        object.addProperty("path", path);
        object.addProperty("type", file.isFile() ? "file" : "folder");

        path = path.replace("\\", "/");
        object.addProperty("pathUrl", URLEncoder.encode(path, StandardCharsets.UTF_8));

        if (file.isFile()) {
            String size = String.valueOf(file.length());
            object.addProperty("size", size);
        }

        return object;
    }

    private FileBrowserService() {

    }
}
