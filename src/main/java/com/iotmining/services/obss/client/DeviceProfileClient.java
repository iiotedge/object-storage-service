package com.iotmining.services.obss.client;

import com.iotmining.services.obss.dto.DeviceProfileDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
        name = "device-profile-service",
        url = "${services.device-profile-service.url}"
)
public interface DeviceProfileClient {
    @GetMapping("/api/device-profiles/{id}")
    DeviceProfileDto getProfile(@PathVariable("id") String id);
}