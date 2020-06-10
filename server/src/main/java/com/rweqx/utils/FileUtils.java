package com.rweqx.utils;

import com.rweqx.files.FileType;
import net.coobird.thumbnailator.Thumbnailator;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class FileUtils {

    private static final int ICON_SIZE = 50;
    private static final int THUMB_SIZE = 16 * 10; // Size in pixels

    private static final Set IMAGE_EXTENSION_SET = Collections.unmodifiableSet(new HashSet<>(Arrays.asList("png", "jpg", "jpeg", "cr2")));

    public static String getIconBase64(File file) {
        try {
            BufferedImage image = getDefaultImage(file, ICON_SIZE);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ImageIO.write(image, "png", out);
            byte[] data = out.toByteArray();
            return Base64.getEncoder().encodeToString(data);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return "";
    }

    /**
     * Gets a Base64 encoding of the thumbnail image of the file.
     * Assumes the file exists.
     *
     * @param file
     * @return
     */
    public static Image getThumbnailBase64(File file) {
        BufferedImage image = null;
        if (FileUtils.getFileType(file) == FileType.IMAGE) {
            try {
                // This seems too expensive. -> Need to probably do it differently.
                image = Thumbnailator.createThumbnail(file, THUMB_SIZE, THUMB_SIZE);
            } catch (Exception e) {
            }
        }

        if (image == null) {
            image = getDefaultImage(file, THUMB_SIZE);
        }
        return image;
    }

    private static BufferedImage getDefaultImage(File file, int size) {
        Icon icon = FileSystemView.getFileSystemView().getSystemIcon(file);
        Image image = ((ImageIcon) icon).getImage().getScaledInstance(size, size, Image.SCALE_DEFAULT);

        BufferedImage bufferedImage = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = bufferedImage.createGraphics();
        graphics.drawImage(image, 0, 0, null);
        graphics.dispose();

        return bufferedImage;
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
