package com.example.devicesapi.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Request model for creating a new device")
public record CreateDeviceRequest(

    @NotBlank
    @Schema(description = "Name of the device", requiredMode = Schema.RequiredMode.REQUIRED)
    String name,

    @NotBlank
    @Schema(description = "Device brand", requiredMode = Schema.RequiredMode.REQUIRED)
    String brand,

    @Schema(description = "Current state of the device", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    State state) {

}
