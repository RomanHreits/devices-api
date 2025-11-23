package com.example.devicesapi.controller;

import com.example.devicesapi.model.CreateDeviceRequest;
import com.example.devicesapi.model.DeviceResponse;
import com.example.devicesapi.model.PartialUpdateDeviceRequest;
import com.example.devicesapi.model.State;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/devices")
public class DeviceController implements DeviceApi {

    @Override
    public ResponseEntity<DeviceResponse> createDevice(CreateDeviceRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(new DeviceResponse());
    }

    @Override
    public ResponseEntity<DeviceResponse> getById(long id) {
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<List<DeviceResponse>> getDevices(String brand, State state) {
        return ResponseEntity.ok(new ArrayList<>());
    }

    @Override
    public ResponseEntity<Void> deleteDevice(long id) {
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<Void> updateDevice(long id, CreateDeviceRequest request) {
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<Void> partialUpdate(long id, PartialUpdateDeviceRequest request) {
        return ResponseEntity.ok().build();
    }
}
