package com.manibhadra.fileuploading.controller;

import com.manibhadra.fileuploading.model.ApiResponse;
import com.manibhadra.fileuploading.model.FileUpload;
import com.manibhadra.fileuploading.service.FileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;

@RestController
@RequestMapping("/file")
public class FileResourceController {

    @Autowired
    private FileStorageService fileStorageService;

    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<FileUpload>> uploadFile(@RequestParam("video") MultipartFile videoFile) {
        try {
            ApiResponse uploadedFile = fileStorageService.storeFiles(videoFile);
            return ResponseEntity.ok().body(new ApiResponse<>(true, "File uploaded successfully", uploadedFile).getData());
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "File upload failed: " + e.getMessage(), null));
        }
    }

    @GetMapping("/download/{filename:.+}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String filename) {
        try {
            Resource resource = fileStorageService.downloadFile(filename);
            if (resource != null) {
                MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;
                String contentType = Files.probeContentType(resource.getFile().toPath());
                if (contentType != null) {
                    mediaType = MediaType.parseMediaType(contentType);
                }
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                        .contentType(mediaType)
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
