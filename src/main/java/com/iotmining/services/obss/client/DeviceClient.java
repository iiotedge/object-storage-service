package com.iotmining.services.obss.client;

import com.iotmining.services.obss.dto.DeviceDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
        name = "device-service",
        url = "${services.device-service.url}"
)
public interface DeviceClient {
    @GetMapping("/api/devices/{id}")
    DeviceDto getDevice(@PathVariable("id") String id);
}
