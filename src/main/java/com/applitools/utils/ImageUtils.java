/*
 * Applitools software.
 */
package com.applitools.utils;

import com.applitools.eyes.EyesException;
import com.applitools.eyes.Region;
import org.apache.commons.codec.binary.Base64;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

public class ImageUtils {
    /**
     * Encodes a given image as PNG.
     * @param image The image to encode.
     * @return The PNG bytes representation of the image.
     */
    public static byte[] encodeAsPng(BufferedImage image) {

        ArgumentGuard.notNull(image, "image");

        byte[] encodedImage; // PNG representation.
        ByteArrayOutputStream pngBytesStream = new ByteArrayOutputStream();

        try {
            // Get the clipped image in PNG encoding.
            ImageIO.write(image, "png", pngBytesStream);
            pngBytesStream.flush();
            encodedImage = pngBytesStream.toByteArray();
        } catch (IOException e) {
            throw new EyesException("Failed to encode image", e);
        } finally {
            try{
                pngBytesStream.close();
            } catch (IOException e) {
                //noinspection ThrowFromFinallyBlock
                throw new EyesException("Failed to close png byte stream", e);
            }
        }
        return encodedImage;
    }

    @SuppressWarnings("UnusedDeclaration")
    /**
     * Creates a {@code BufferedImage} from an image file specified by {@code
     * path}.
     * @param path The path to the image file.
     * @return A {@code BufferedImage} instance.
     * @throws com.applitools.eyes.EyesException If there was a problem
     * creating the {@code BufferedImage} instance.
     */
    public static BufferedImage imageFromFile(String path) throws
            EyesException {
        BufferedImage result;
        try {
            result = ImageIO.read(new File(path));
        } catch (IOException e) {
            throw new EyesException("Failed to to load the image bytes from "
                    + path, e);
        }
        return result;
    }

    /**
     * Creates a {@code BufferedImage} instance from a base64 encoding of an
     * image's bytes.
     * @param image64 The base64 encoding of an image's bytes.
     * @return A {@code BufferedImage} instance.
     * @throws com.applitools.eyes.EyesException If there was a problem
     * creating the {@code BufferedImage} instance.
     */
    public static BufferedImage imageFromBase64(String image64) throws
            EyesException {
        ArgumentGuard.notNullOrEmpty(image64, "image64");

        // Get the image bytes
        byte[] imageBytes =
                Base64.decodeBase64(image64.getBytes(Charset.forName("UTF-8")));
        return imageFromBytes(imageBytes);
    }

    /**
     *
     * @param image The image from which to get its base64 representation.
     * @return The base64 representation of the image (bytes encoded as PNG).
     */
    public static String base64FromImage(BufferedImage image) {
        ArgumentGuard.notNull(image, "image");

        byte[] imageBytes = encodeAsPng(image);
        return Base64.encodeBase64String(imageBytes);
    }

    /**
     * Creates a BufferedImage instance from raw image bytes.
     * @param imageBytes The raw bytes of the image.
     * @return A BufferedImage instance representing the image.
     * @throws com.applitools.eyes.EyesException If there was a problem
     * creating the {@code BufferedImage} instance.
     */
    public static BufferedImage imageFromBytes(byte[] imageBytes) throws
            EyesException {
        BufferedImage image;
        try {
            ByteArrayInputStream screenshotStream =
                    new ByteArrayInputStream(imageBytes);
            image = ImageIO.read(screenshotStream);
            screenshotStream.close();
        } catch (IOException e) {
            throw new EyesException("Failed to create buffered image!", e);
        }
        return image;
    }

    /**
     * Get a copy of the part of the image given by region.
     * @param image The image from which to get the part.
     * @param region The region which should be copied from the image.
     * @return The part of the image.
     */
    public static BufferedImage getImagePart(BufferedImage image,
                                             Region region) {
        ArgumentGuard.notNull(image, "image");

        // Get the clipped region as a BufferedImage.
        BufferedImage imagePart = image.getSubimage(
                region.getLeft(), region.getTop(), region.getWidth(),
                region.getHeight());
        // IMPORTANT We copy the image this way because just using getSubImage
        // created a later problem (maybe an actual Java bug): the pixels
        // weren't what they were supposed to be.
        byte[] imagePartBytes = encodeAsPng(imagePart);
        return imageFromBytes(imagePartBytes);
    }

    @SuppressWarnings("UnusedDeclaration")
    /**
     * Rotates an image by the given degrees.
     *
     * @param image The image to rotate.
     * @param deg The degrees by which to rotate the image.
     * @return A rotated image.
     */
    public static BufferedImage rotateImage(BufferedImage image, double deg) {
        double radians = Math.toRadians(deg);

        // We need this to calculate the width/height of the rotated image.
        double angleSin = Math.abs(Math.sin(radians));
        double angleCos = Math.abs(Math.cos(radians));

        int originalWidth = image.getWidth();
        double originalHeight = image.getHeight();

        int rotatedWidth = (int) Math.floor(
                (originalWidth * angleCos) + (originalHeight * angleSin)
        );

        int rotatedHeight = (int) Math.floor(
                (originalHeight * angleCos) + (originalWidth * angleSin)
        );

        BufferedImage rotatedImage =
                new BufferedImage(rotatedWidth, rotatedHeight, image.getType());

        Graphics2D g = rotatedImage.createGraphics();

        // Notice we must first perform translation so the rotated result
        // will be properly positioned.
        g.translate((rotatedWidth-originalWidth)/2,
                (rotatedHeight-originalHeight)/2);

        g.rotate(radians, originalWidth / 2, originalHeight / 2);

        g.drawRenderedImage(image, null);
        g.dispose();

        return rotatedImage;
    }
}
