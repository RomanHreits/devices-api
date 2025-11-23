package com.example.devicesapi.repository;

import com.example.devicesapi.entity.DeviceEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface DeviceRepository extends CrudRepository<DeviceEntity, Long> {

    List<DeviceEntity> findByBrandAndState(String brand, String state);

    List<DeviceEntity> findByState(String name);

    List<DeviceEntity> findByBrand(String brand);

    List<DeviceEntity> findAll();
}
