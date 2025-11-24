package com.example.devicesapi.controller;

import com.example.devicesapi.model.CreateDeviceRequest;
import com.example.devicesapi.model.DeviceResponse;
import com.example.devicesapi.model.PartialUpdateDeviceRequest;
import com.example.devicesapi.model.State;
import com.example.devicesapi.service.DeviceService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/devices")
public class DeviceController implements DeviceApi {
    private final DeviceService deviceService;

    public DeviceController(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @Override
    public ResponseEntity<DeviceResponse> createDevice(CreateDeviceRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(deviceService.createDevice(request));
    }

    @Override
    public ResponseEntity<DeviceResponse> getById(long id) {
        return ResponseEntity.ok(deviceService.getDeviceById(id));
    }

    @Override
    public ResponseEntity<List<DeviceResponse>> getDevices(String brand, State state) {
        List<DeviceResponse> devices = deviceService.getDevices(brand, state);
        return ResponseEntity.ok(devices);
    }

    @Override
    public ResponseEntity<DeviceResponse> deleteDevice(long id) {
        return ResponseEntity.ok(deviceService.deleteDevice(id));
    }

    @Override
    public ResponseEntity<DeviceResponse> updateDevice(long id, CreateDeviceRequest request) {
        return ResponseEntity.ok(deviceService.updateDevice(id, request));
    }

    @Override
    public ResponseEntity<DeviceResponse> partialUpdate(long id, PartialUpdateDeviceRequest request) {
        return ResponseEntity.ok(deviceService.partialUpdateDevice(id, request));
    }
}
