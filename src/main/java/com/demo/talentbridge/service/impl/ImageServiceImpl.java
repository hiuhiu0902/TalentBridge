package com.demo.talentbridge.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.demo.talentbridge.service.ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ImageServiceImpl implements ImageService {
    private final Cloudinary cloudinary;

    @Override
    public String upload(MultipartFile file) {
        try {
            Map result = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", "tailent",
                            "resource_type", "auto"
                    )
            );
            return result.get("secure_url").toString();
        } catch (IOException e) {
            throw new RuntimeException("Upload image failed", e);
        }
    }

    @Override
    public void delete(String imageUrl) {

        try {
            String publicId = extractPublicId(imageUrl);
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        } catch (Exception e) {
            throw new RuntimeException("Delete image failed", e);
        }
    }

    @Override
    public String extractPublicId(String url) {

        String[] parts = url.split("/");

        String fileName = parts[parts.length - 1];

        String name = fileName.substring(0, fileName.lastIndexOf("."));

        return "tailent/" + name;
    }
}
