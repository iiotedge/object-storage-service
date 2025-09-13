package com.iotmining.services.obss.controller;

import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.http.Method;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/test")
public class TestUploadController {

    @Autowired
    private MinioClient minioClient;

    @Value("${minio.bucket}")
    private String bucket;

    @PostMapping("/upload-temp")
    public ResponseEntity<?> uploadTemp(@RequestParam("file") MultipartFile file) {
        String objectPath = "temp/" + file.getOriginalFilename();
        try {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectPath)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );
            return ResponseEntity.ok().body("{\"objectPath\": \"" + objectPath + "\"}");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }
    @GetMapping("/presigned-temp")
    public ResponseEntity<?> getPresignedTemp(
            @RequestParam("filename") String filename,
            @RequestParam(value = "expiry", required = false, defaultValue = "300") int expirySeconds) {
        String objectPath = "temp/" + filename;
        try {
            String url = minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .bucket(bucket)
                            .object(objectPath)
                            .method(Method.GET)
                            .expiry(expirySeconds, TimeUnit.SECONDS)
                            .build()
            );
            return ResponseEntity.ok().body("{\"url\": \"" + url + "\"}");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }
}
