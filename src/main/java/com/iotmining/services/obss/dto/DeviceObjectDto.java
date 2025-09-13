package com.iotmining.services.obss.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class DeviceObjectDto {
    private Long id;
    private String tenantId;
    private String deviceId;
    private String objectType;
    private String category;
    private String filename;
    private String objectUrl;
    private Long size;
    private LocalDateTime uploadedAt;
}
