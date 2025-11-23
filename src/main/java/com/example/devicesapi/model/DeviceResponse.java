package com.example.devicesapi.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;

@Schema(description = "Response model for creating a new device")
public record DeviceResponse(
    @Schema(description = "Unique identifier of the device", example = "1")
    Long id,

    @Schema(description = "Name of the device")
    String name,

    @Schema(description = "Device brand")
    String brand,

    @Schema(description = "Current state of the device", example = "available")
    String state,

    @Schema(description = "Timestamp when the device was created", example = "2024-01-01T12:00:00")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
    OffsetDateTime createdAt) {

    public DeviceResponse() {
        this(null, null, null, null, null);
    }
}
