package com.iotmining.services.obss.controller;

import com.iotmining.services.obss.dto.DeviceObjectDto;
import com.iotmining.services.obss.model.DeviceObject;
import com.iotmining.services.obss.service.ObjectStorageService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/objects")
public class ObjectController {

    @Autowired
    private ObjectStorageService objectStorageService;

    // 1. Upload
// 1. Upload (return metadata + presigned URL)
    @PostMapping("/upload")
    public ResponseEntity<?> upload(
            @RequestPart("file") MultipartFile file,
            @RequestPart("tenantId") String tenantId,
            @RequestPart("deviceId") String deviceId,
            @RequestPart("objectType") String objectType,
            @RequestPart("category") String category,
            @RequestParam(value = "expiry", defaultValue = "86400") int expiry
    ) {
        try {
            DeviceObject obj = objectStorageService.uploadObject(tenantId, deviceId, objectType, category, file);
            DeviceObjectDto dto = new DeviceObjectDto();
            BeanUtils.copyProperties(obj, dto);
            String url = objectStorageService.generatePresignedUrl(obj, expiry);
            // Return both the metadata and the presigned url
            return ResponseEntity.ok().body(Map.of(
                    "object", dto,
                    "presignedUrl", url
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    @PostMapping("/upload-simple")
    public ResponseEntity<?> uploadSimple(
            @RequestPart("file") MultipartFile file,
            @RequestPart("tenantId") String tenantId,
            @RequestPart("deviceId") String deviceId,
            @RequestPart("objectType") String objectType,
            @RequestPart("category") String category,
            @RequestParam(value = "expiry", defaultValue = "86400") int expiry
    ) {
        try {
            DeviceObject obj = objectStorageService.uploadObjectSimple(tenantId, deviceId, objectType, category, file);
            DeviceObjectDto dto = new DeviceObjectDto();
            BeanUtils.copyProperties(obj, dto);
            String url = objectStorageService.generatePresignedUrl(obj, expiry);
            // Return both the metadata and the presigned url
            return ResponseEntity.ok().body(Map.of(
                    "object", dto,
                    "presignedUrl", url
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    // 2. Download by objectId
    @GetMapping("/download/{objectId}")
    public ResponseEntity<?> downloadObject(
            @RequestParam("tenantId") String tenantId,
            @PathVariable("objectId") Long objectId
    ) {
        try {
            DeviceObject obj = objectStorageService.getObjectById(objectId, tenantId);
            if (obj == null) return ResponseEntity.notFound().build();
            InputStreamResource resource = new InputStreamResource(objectStorageService.getObjectStream(obj));
            String contentType = "application/octet-stream";
            String filename = obj.getFilename().toLowerCase();
            if (filename.endsWith(".jpg") || filename.endsWith(".jpeg")) contentType = "image/jpeg";
            else if (filename.endsWith(".png")) contentType = "image/png";
            else if (filename.endsWith(".gif")) contentType = "image/gif";
            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=\"" + obj.getFilename() + "\"")
                    .header("Content-Type", contentType)
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    // 3. Download latest by (tenant, device, objectType)
    @GetMapping("/download/latest")
    public ResponseEntity<?> downloadLatestObject(
            @RequestParam("tenantId") String tenantId,
            @RequestParam("deviceId") String deviceId,
            @RequestParam("objectType") String objectType
    ) {
        try {
            DeviceObject obj = objectStorageService.getLatestObject(tenantId, deviceId, objectType);
            if (obj == null) return ResponseEntity.notFound().build();
            InputStreamResource resource = new InputStreamResource(objectStorageService.getObjectStream(obj));
            String contentType = "application/octet-stream";
            String filename = obj.getFilename().toLowerCase();
            if (filename.endsWith(".jpg") || filename.endsWith(".jpeg")) contentType = "image/jpeg";
            else if (filename.endsWith(".png")) contentType = "image/png";
            else if (filename.endsWith(".gif")) contentType = "image/gif";
            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=\"" + obj.getFilename() + "\"")
                    .header("Content-Type", contentType)
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    // 4. Download latest by category (optionally presigned)
    @GetMapping("/download/latest-by-category")
    public ResponseEntity<?> downloadLatestByCategory(
            @RequestParam("tenantId") String tenantId,
            @RequestParam("deviceId") String deviceId,
            @RequestParam("category") String category,
            @RequestParam(value = "objectType", required = false) String objectType,
            @RequestParam(value = "presigned", defaultValue = "false") boolean presigned,
            @RequestParam(value = "expiry", defaultValue = "300") int expiry
    ) {
        try {
            DeviceObject obj;
            if (objectType != null && !objectType.isEmpty()) {
                obj = objectStorageService.getLatestByCategoryAndType(tenantId, deviceId, category, objectType);
            } else {
                obj = objectStorageService.getLatestByCategory(tenantId, deviceId, category);
            }
            if (obj == null) return ResponseEntity.notFound().build();

            if (presigned) {
                String url = objectStorageService.generatePresignedUrl(obj, expiry);
                return ResponseEntity.ok().body("{\"url\":\"" + url + "\"}");
            } else {
                InputStreamResource resource = new InputStreamResource(objectStorageService.getObjectStream(obj));
                String contentType = "application/octet-stream";
                String filename = obj.getFilename().toLowerCase();
                if (filename.endsWith(".jpg") || filename.endsWith(".jpeg")) contentType = "image/jpeg";
                else if (filename.endsWith(".png")) contentType = "image/png";
                else if (filename.endsWith(".gif")) contentType = "image/gif";
                return ResponseEntity.ok()
                        .header("Content-Disposition", "attachment; filename=\"" + obj.getFilename() + "\"")
                        .header("Content-Type", contentType)
                        .body(resource);
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    // 5. Get latest presigned (by objectType)
    @GetMapping("/latest/presigned")
    public ResponseEntity<?> getLatestPresigned(
            @RequestParam("tenantId") String tenantId,
            @RequestParam("deviceId") String deviceId,
            @RequestParam("objectType") String objectType,
            @RequestParam(value = "expiry", defaultValue = "300") int expiry
    ) {
        try {
            DeviceObject obj = objectStorageService.getLatestObject(tenantId, deviceId, objectType);
            if (obj == null)
                return ResponseEntity.notFound().build();
            String url = objectStorageService.generatePresignedUrl(obj, expiry);
            return ResponseEntity.ok().body("{\"url\":\"" + url + "\"}");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    @GetMapping("/history")
    public ResponseEntity<?> getObjectHistory(
            @RequestParam("tenantId") String tenantId,
            @RequestParam("deviceId") String deviceId,
            @RequestParam("category") String category,
            @RequestParam("objectType") String objectType,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "expiry", defaultValue = "300") int expiry
    ) {
        try {
            Page<DeviceObject> objects = objectStorageService.getLatestObjects(
                    tenantId, deviceId, category, objectType, PageRequest.of(page, size)
            );
            var items = objects.stream().map(obj -> {
                DeviceObjectDto dto = new DeviceObjectDto();
                BeanUtils.copyProperties(obj, dto);
                String presignedUrl = "";
                try {
                    presignedUrl = objectStorageService.generatePresignedUrl(obj, expiry);
                } catch (Exception e) {
                    presignedUrl = null;
                }
                return Map.of(
                        "object", dto,
                        "presignedUrl", presignedUrl
                );
            }).collect(Collectors.toList());

            return ResponseEntity.ok().body(Map.of(
                    "page", objects.getNumber(),
                    "size", objects.getSize(),
                    "totalPages", objects.getTotalPages(),
                    "totalElements", objects.getTotalElements(),
                    "data", items
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    @GetMapping("/download-simple")
    public ResponseEntity<?> downloadSimpleObject(@RequestParam("key") String key) {
        try {
            InputStreamResource resource = new InputStreamResource(objectStorageService.downloadSimpleObjectByKey(key));
            String contentType = "application/json"; // or derive from extension
            String filename = key.substring(key.lastIndexOf('/') + 1);
            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=\"" + filename + "\"")
                    .header("Content-Type", contentType)
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
    @GetMapping("/simple/presigned")
    public ResponseEntity<?> getSimplePresignedUrl(
            @RequestParam("tenantId") String tenantId,
            @RequestParam("deviceId") String deviceId,
            @RequestParam("objectType") String objectType,
            @RequestParam(value = "category", defaultValue = "UI") String category,
            @RequestParam(value = "expiry", defaultValue = "300") int expiry
    ) {
        try {
            // Compose the path exactly as used in uploadObjectSimple:
            String objectPath = String.format("%s/%s/%s/%s/%s.json",
                    tenantId, deviceId, category, objectType, deviceId);

            String url = objectStorageService.generatePresignedUrlForKey(objectPath, expiry);
            return ResponseEntity.ok(Map.of("url", url));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

}
