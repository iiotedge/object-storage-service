package com.iotmining.services.obss.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "device_objects")
public class DeviceObject {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String tenantId;    // for multi-tenant isolation

    @Column(nullable = false)
    private String deviceId;

    @Column(nullable = false)
    private String objectType;  // e.g., snapshot, config

    @Column(nullable = false)
    private String category;    // e.g., CAMERA, FAN, etc.

    @Column(nullable = false)
    private String filename;

    @Column(nullable = false)
    private String objectUrl;   // MinIO key/path

    @Column(nullable = false)
    private Long size;

    @Column(nullable = false)
    private LocalDateTime uploadedAt;
}
