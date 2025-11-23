package com.example.devicesapi.model;

import io.swagger.v3.oas.annotations.media.Schema;

public record PartialUpdateDeviceRequest(
        @Schema(description = "Name of the device", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        String name,
        @Schema(description = "Current state of the device", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        State state,
        @Schema(description = "Device brand", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        String brand) {
}
