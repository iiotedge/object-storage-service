package com.iotmining.services.obss.repository;

import com.iotmining.services.obss.model.DeviceObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DeviceObjectRepository extends JpaRepository<DeviceObject, Long> {
    Optional<DeviceObject> findByIdAndTenantId(Long id, String tenantId);

    Optional<DeviceObject> findFirstByTenantIdAndDeviceIdAndObjectTypeOrderByUploadedAtDesc(
            String tenantId, String deviceId, String objectType);

    Optional<DeviceObject> findFirstByTenantIdAndDeviceIdAndCategoryOrderByUploadedAtDesc(
            String tenantId, String deviceId, String category);

    Optional<DeviceObject> findFirstByTenantIdAndDeviceIdAndCategoryAndObjectTypeOrderByUploadedAtDesc(
            String tenantId, String deviceId, String category, String objectType);

    Page<DeviceObject> findByTenantIdAndDeviceIdAndCategoryAndObjectTypeOrderByUploadedAtDesc(
            String tenantId, String deviceId, String category, String objectType, Pageable pageable);
}
