package com.manibhadra.fileuploading.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    @Value("${app.upload.directory}")
    private String uploadDirectory;

    @Value("${app.upload.thumbnail.directory}")
    private String thumbnailDirectory;

    public String getUploadDirectory() {
        return uploadDirectory;
    }

    public String getThumbnailDirectory() {
        return thumbnailDirectory;
    }
}
