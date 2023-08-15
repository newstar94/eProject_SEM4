package com.example.DecorEcommerceProject.Service.Impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Map;

@Service
public class CloudinaryService {
    private final Cloudinary cloudinary;

    @Autowired
    public CloudinaryService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    public String saveProductImageToCloudinary(MultipartFile imageFile) {
        try {
            // Upload image to Cloudinary
            Map uploadResult = cloudinary.uploader().upload(imageFile.getBytes(), ObjectUtils.emptyMap());

            // Retrieve the image URL from the upload result
            return uploadResult.get("secure_url").toString();
        } catch (IOException e) {
            throw new RuntimeException("Failed to save product image to Cloudinary", e);
        }
    }
    public String saveOrderToCloudinary(File imageFile) {
        try {
            // Upload image to Cloudinary
            Map uploadResult = cloudinary.uploader().upload(imageFile, ObjectUtils.emptyMap());

            // Retrieve the image URL from the upload result
            return uploadResult.get("secure_url").toString();
        } catch (IOException e) {
            throw new RuntimeException("Failed to save to Cloudinary", e);
        }
    }
    public void deleteProductImageFromCloudinary(String imageUrl) {
        try {
            // Extract the public ID from the image URL
            String publicId = extractPublicIdFromImageUrl(imageUrl);

            // Delete the image from Cloudinary
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());

            // Alternatively, you can use the following code to delete the image using the URL directly:
            // cloudinary.uploader().destroy(imageUrl, ObjectUtils.emptyMap());
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete product image from Cloudinary", e);
        }
    }
    private String extractPublicIdFromImageUrl(String imageUrl) {
        try {
            // Extract the public ID from the image URL
            URL url = new URL(imageUrl);
            String path = url.getPath();

            // Remove the leading forward slash
            if (path.startsWith("/")) {
                path = path.substring(1);
            }

            // Remove the file extension if present
            int extensionIndex = path.lastIndexOf(".");
            if (extensionIndex != -1) {
                path = path.substring(0, extensionIndex);
            }

            // Extract the public ID by splitting the path with slashes and returning the last part
            String[] pathParts = path.split("/");
            String publicId = pathParts[pathParts.length - 1];

            return publicId;
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid image URL: " + imageUrl, e);
        }
    }
}
