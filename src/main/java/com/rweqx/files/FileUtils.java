package com.rweqx.files;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import java.awt.*;
import java.awt.image.RenderedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class FileUtils {

    private static final int THUMB_SIZE = 50;

    private static final Set IMAGE_EXTENSION_SET = Collections.unmodifiableSet(new HashSet<>(Arrays.asList("png", "jpg", "jpeg", "cr2")));

    /**
     * Gets a Base64 encoding of the thumbnail image of the file.
     * Assumes the file exists.
     *
     * @param file
     * @return
     */
    public static String getThumnailBase64(File file) {
        try {
            Image image;
            if (FileUtils.getFileType(file) == FileType.IMAGE) {
                try {
                    // This seems too expensive. -> Need to probably do it differently.
                    //image = Thumbnailator.createThumbnail(file, THUMB_SIZE, THUMB_SIZE);
                    image = getDefaultImage(file);
                } catch (Exception e) {
                    image = getDefaultImage(file);
                }
            } else {
                image = getDefaultImage(file);
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ImageIO.write((RenderedImage) image, "png", out);

            byte[] data = out.toByteArray();

            return Base64.getEncoder().encodeToString(data);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return "";
    }

    private static Image getDefaultImage(File file) {
        Icon icon = FileSystemView.getFileSystemView().getSystemIcon(file);
        return ((ImageIcon) icon).getImage();
    }

    public static FileType getFileType(File file) {

        String path = file.getPath();
        String ext = "";
        if (path.lastIndexOf(".") != -1) {
            ext = path.substring(path.lastIndexOf(".") + 1).toLowerCase();
        }

        if (file.isFile()) {
            if (IMAGE_EXTENSION_SET.contains(ext)) {
                return FileType.IMAGE;
            }
            // TODO parse other types?
        }

        return FileType.UNKNOWN;
    }
}
