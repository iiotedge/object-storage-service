package com.iotmining.services.obss.service;


import com.iotmining.services.obss.model.DeviceObject;
import com.iotmining.services.obss.repository.DeviceObjectRepository;
import io.minio.*;
import io.minio.http.Method;
import io.minio.messages.Item;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class ObjectStorageService {

    @Autowired
    private DeviceObjectRepository deviceObjectRepository;

    @Autowired
    private MinioClient minioClient;

    @Value("${minio.bucket}")
    private String bucket;

    public DeviceObject uploadObject(String tenantId, String deviceId, String objectType, String category, MultipartFile file) throws Exception {
        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String objectPath = String.format("%s/%s/%s/%s/%s/%s",
                tenantId, deviceId, category, objectType, datePath, file.getOriginalFilename());

        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(bucket)
                        .object(objectPath)
                        .stream(file.getInputStream(), file.getSize(), -1)
                        .contentType(file.getContentType())
                        .build()
        );

        DeviceObject obj = new DeviceObject();
        obj.setTenantId(tenantId);
        obj.setDeviceId(deviceId);
        obj.setObjectType(objectType);
        obj.setCategory(category);
        obj.setFilename(file.getOriginalFilename());
        obj.setObjectUrl(objectPath);
        obj.setSize(file.getSize());
        obj.setUploadedAt(LocalDateTime.now());

        return deviceObjectRepository.save(obj);
    }

    public DeviceObject uploadObjectSimple(
            String tenantId, String deviceId, String objectType, String category, MultipartFile file
    ) throws Exception {
        // No date/time in path!
        String objectPath = String.format("%s/%s/%s/%s/%s",
                tenantId, deviceId, category, objectType, file.getOriginalFilename());

        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(bucket)
                        .object(objectPath)
                        .stream(file.getInputStream(), file.getSize(), -1)
                        .contentType(file.getContentType())
                        .build()
        );

        DeviceObject obj = new DeviceObject();
        obj.setTenantId(tenantId);
        obj.setDeviceId(deviceId);
        obj.setObjectType(objectType);
        obj.setCategory(category);
        obj.setFilename(file.getOriginalFilename());
        obj.setObjectUrl(objectPath);
        obj.setSize(file.getSize());
        obj.setUploadedAt(LocalDateTime.now());

        return deviceObjectRepository.save(obj);
    }

    public DeviceObject getObjectById(Long objectId, String tenantId) {
        return deviceObjectRepository.findByIdAndTenantId(objectId, tenantId).orElse(null);
    }

    public InputStream getObjectStream(DeviceObject obj) throws Exception {
        return minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(bucket)
                        .object(obj.getObjectUrl())
                        .build()
        );
    }

    public DeviceObject getLatestObject(String tenantId, String deviceId, String objectType) {
        return deviceObjectRepository
                .findFirstByTenantIdAndDeviceIdAndObjectTypeOrderByUploadedAtDesc(tenantId, deviceId, objectType)
                .orElse(null);
    }

    public DeviceObject getLatestByCategory(String tenantId, String deviceId, String category) {
        return deviceObjectRepository
                .findFirstByTenantIdAndDeviceIdAndCategoryOrderByUploadedAtDesc(tenantId, deviceId, category)
                .orElse(null);
    }

    public DeviceObject getLatestByCategoryAndType(String tenantId, String deviceId, String category, String objectType) {
        return deviceObjectRepository
                .findFirstByTenantIdAndDeviceIdAndCategoryAndObjectTypeOrderByUploadedAtDesc(tenantId, deviceId, category, objectType)
                .orElse(null);
    }

    public String generatePresignedUrl(DeviceObject obj, int expirySeconds) throws Exception {
        return minioClient.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                        .bucket(bucket)
                        .object(obj.getObjectUrl())
                        .method(Method.GET)
                        .expiry(expirySeconds, TimeUnit.SECONDS)
                        .build()
        );
    }
    public Page<DeviceObject> getLatestObjects(String tenantId, String deviceId, String category, String objectType, Pageable pageable) {
        return deviceObjectRepository.findByTenantIdAndDeviceIdAndCategoryAndObjectTypeOrderByUploadedAtDesc(
                tenantId, deviceId, category, objectType, pageable);
    }

    public String generatePresignedUrlForKey(String objectPath, int expirySeconds) throws Exception {
        return minioClient.getPresignedObjectUrl(
                io.minio.GetPresignedObjectUrlArgs.builder()
                        .bucket(bucket)
                        .object(objectPath)
                        .method(io.minio.http.Method.GET)
                        .expiry(expirySeconds, java.util.concurrent.TimeUnit.SECONDS)
                        .build()
        );
    }

    public InputStream downloadSimpleObjectByKey(String key) throws Exception {
        return minioClient.getObject(
                io.minio.GetObjectArgs.builder()
                        .bucket(bucket)
                        .object(key)
                        .build()
        );
    }
}

