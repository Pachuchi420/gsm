package tools;

import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.*;
import com.drew.metadata.exif.ExifIFD0Directory;
import javafx.embed.swing.SwingFXUtils;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;

public class ImageRotationHelper {

    public static BufferedImage autoOrientImage(File imageFile, BufferedImage image) {
        try {
            Metadata metadata = ImageMetadataReader.readMetadata(imageFile);
            ExifIFD0Directory directory = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);

            if (directory != null && directory.containsTag(ExifIFD0Directory.TAG_ORIENTATION)) {
                int orientation = directory.getInt(ExifIFD0Directory.TAG_ORIENTATION);
                return transformImage(image, orientation);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return image; // return original if no orientation found
    }

    private static BufferedImage transformImage(BufferedImage image, int orientation) {
        int width = image.getWidth();
        int height = image.getHeight();

        AffineTransform transform = new AffineTransform();
        int newWidth = width;
        int newHeight = height;

        switch (orientation) {
            case 1 -> {} // normal
            case 2 -> transform.scale(-1.0, 1.0); // flip X
            case 3 -> {
                transform.rotate(Math.PI, width / 2.0, height / 2.0); // rotate 180
            }
            case 4 -> {
                transform.scale(1.0, -1.0);
                transform.translate(0, -height);
            }
            case 5 -> {
                transform.rotate(Math.PI / 2, width / 2.0, height / 2.0);
                transform.scale(1.0, -1.0);
                newWidth = height;
                newHeight = width;
            }
            case 6 -> {
                transform.rotate(Math.PI / 2, width / 2.0, height / 2.0); // rotate 90 CW
                newWidth = height;
                newHeight = width;
            }
            case 7 -> {
                transform.rotate(-Math.PI / 2, width / 2.0, height / 2.0);
                transform.scale(1.0, -1.0);
                newWidth = height;
                newHeight = width;
            }
            case 8 -> {
                transform.rotate(-Math.PI / 2, width / 2.0, height / 2.0); // rotate 90 CCW
                newWidth = height;
                newHeight = width;
            }
            default -> {}
        }

        BufferedImage transformedImage = new BufferedImage(
                newWidth, newHeight, BufferedImage.TYPE_INT_RGB
        );

        Graphics2D g = transformedImage.createGraphics();

        // Center transform properly
        if (orientation == 5 || orientation == 6 || orientation == 7 || orientation == 8) {
            transform.preConcatenate(AffineTransform.getTranslateInstance((newWidth - width) / 2.0, (newHeight - height) / 2.0));
        }

        g.drawImage(image, transform, null);
        g.dispose();

        return transformedImage;
    }

    // ImageRotationHelper.java
    public static BufferedImage toBufferedImage(javafx.scene.image.Image fxImage) {
        return SwingFXUtils.fromFXImage(fxImage, null);
    }
}
