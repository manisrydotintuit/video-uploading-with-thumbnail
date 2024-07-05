package com.manibhadra.fileuploading.service;

import com.manibhadra.fileuploading.configuration.AppConfig;
import com.manibhadra.fileuploading.model.ApiResponse;
import com.manibhadra.fileuploading.model.FileUpload;
import com.manibhadra.fileuploading.repo.FileRepo;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

@Service
public class FileStorageService {

    private final String DIRECTORY;
    private final String THUMBNAIL_DIRECTORY;

    @Autowired
    private FileRepo fileRepo;

    @Autowired
    public FileStorageService(AppConfig appConfig) {
        this.DIRECTORY = appConfig.getUploadDirectory();
        this.THUMBNAIL_DIRECTORY = appConfig.getThumbnailDirectory();
    }

    public ApiResponse storeFiles(MultipartFile videoFile) throws IOException {
        // Process video file and generate thumbnail
        FileUpload videoUpload = processAndStoreVideo(videoFile);

        ApiResponse response = new ApiResponse(true, "File uploaded successfully", List.of(videoUpload));
        return response;
    }

    public Resource downloadFile(String filename) throws IOException {
        Path filePath = Paths.get(DIRECTORY).toAbsolutePath().normalize().resolve(filename);
        if (!Files.exists(filePath)) {
            return null;
        }

        Resource resource = new UrlResource(filePath.toUri());
        return resource;
    }

    private FileUpload processAndStoreVideo(MultipartFile file) throws IOException {
        String originalFilename = Objects.requireNonNull(file.getOriginalFilename());
        String cleanFilename = cleanFilename(originalFilename);
        String timestampedFilename = generateTimestampedFilename(cleanFilename);

        Path directoryPath = Paths.get(DIRECTORY).toAbsolutePath().normalize();
        if (!Files.exists(directoryPath)) {
            Files.createDirectories(directoryPath);
        }

        Path fileStorage = directoryPath.resolve(timestampedFilename);
        Files.copy(file.getInputStream(), fileStorage, REPLACE_EXISTING);

        // Generate thumbnail
        String thumbnailFilename = generateThumbnail(fileStorage.toString());

        FileUpload fileUpload = new FileUpload();
        fileUpload.setFileName(cleanFilename);
        fileUpload.setFileType(file.getContentType());
        fileUpload.setTimestamp(LocalDateTime.now());
        fileUpload.setThumbnail(thumbnailFilename); // Set the generated thumbnail path
        fileRepo.save(fileUpload);

        return fileUpload;
    }

    private String cleanFilename(String filename) {
        // Replace special characters with underscores
        return filename.replaceAll("[^a-zA-Z0-9.-]", "_");
    }

    private String generateTimestampedFilename(String filename) {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
        String timestamp = now.format(formatter);
        return timestamp + "_" + filename;
    }

    private String generateThumbnail(String videoPath) throws IOException {
        FFmpegFrameGrabber frameGrabber = new FFmpegFrameGrabber(videoPath);
        frameGrabber.start();

        Java2DFrameConverter converter = new Java2DFrameConverter();
        BufferedImage frameImage = converter.convert(frameGrabber.grabKeyFrame());

        if (frameImage != null) {
            Path thumbnailDirectoryPath = Paths.get(THUMBNAIL_DIRECTORY).toAbsolutePath().normalize();
            if (!Files.exists(thumbnailDirectoryPath)) {
                Files.createDirectories(thumbnailDirectoryPath);
            }

            String thumbnailFilename = "thumbnail_" + System.currentTimeMillis() + ".png";
            Path thumbnailPath = thumbnailDirectoryPath.resolve(thumbnailFilename);
            ImageIO.write(frameImage, "png", thumbnailPath.toFile());

            frameGrabber.stop();
            return thumbnailFilename;
        } else {
            frameGrabber.stop();
            throw new IOException("Failed to generate thumbnail for video: " + videoPath);
        }
    }
}
