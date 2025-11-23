package com.example.devicesapi.mapper;

import com.example.devicesapi.entity.DeviceEntity;
import com.example.devicesapi.model.CreateDeviceRequest;
import com.example.devicesapi.model.DeviceResponse;
import com.example.devicesapi.model.PartialUpdateDeviceRequest;
import com.example.devicesapi.model.State;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static java.util.Optional.ofNullable;

@Component
public class DeviceMapper {

    public DeviceEntity createDeviceRequestToEntity(CreateDeviceRequest request) {
        return new DeviceEntity(request.name(), request.brand(),
                ofNullable(request.state())
                        .map(State::getValue)
                        .orElse(State.INACTIVE.getValue()));
    }

    public DeviceEntity createDeviceRequestToEntity(CreateDeviceRequest request, long id) {
        return new DeviceEntity(id, request.name(), request.brand(),
                ofNullable(request.state())
                        .map(State::getValue)
                        .orElse(State.INACTIVE.getValue()));
    }

    public DeviceEntity partialUpdateRequestToEntity(
            PartialUpdateDeviceRequest request,
            DeviceEntity existingEntity) {
        return new DeviceEntity(existingEntity.getId(),
                ofNullable(request.name()).orElse(existingEntity.getName()),
                ofNullable(request.brand()).orElse(existingEntity.getBrand()),
                ofNullable(request.state()).map(State::getValue).orElse(existingEntity.getState()));
    }

    public DeviceResponse fromEntityToDeviceResponse(DeviceEntity entity) {
        OffsetDateTime created = ofNullable(entity.getCreatedAt())
                .map(createdAt -> createdAt.atOffset(ZoneOffset.UTC))
                .orElse(null);

        return new DeviceResponse(
                entity.getId(),
                entity.getName(),
                entity.getBrand(),
                entity.getState(),
                created);
    }
}
