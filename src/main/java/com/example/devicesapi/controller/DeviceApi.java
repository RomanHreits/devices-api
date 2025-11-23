package com.example.devicesapi.controller;

import com.example.devicesapi.model.CreateDeviceRequest;
import com.example.devicesapi.model.DeviceResponse;
import com.example.devicesapi.model.ErrorResponse;
import com.example.devicesapi.model.PartialUpdateDeviceRequest;
import com.example.devicesapi.model.State;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@RequestMapping("/devices")
public interface DeviceApi {
    @Operation(
            summary = "Create a device",
            description = "Creates a new device and return the created resource with all the fields.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Device has been created successfully",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = DeviceResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid input.",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "409", description = "Such device has already been created before.")
            }
    )
    @PostMapping
    ResponseEntity<DeviceResponse> createDevice(@RequestBody @Valid CreateDeviceRequest request);

    @Operation(summary = "Get device by id", responses = {
            @ApiResponse(responseCode = "200", description = "Successfully found a device with provided id."),
            @ApiResponse(responseCode = "404", description = "Device was not found, provided id is invalid.")
    })
    @GetMapping("/{id}")
    ResponseEntity<DeviceResponse> getById(@PathVariable("id") long id);

    @Operation(summary = "Get list devices",
            description = "Returns all devices filtered by provided optional query parameters `brand` and/or `state`.",
            responses = {
                    @ApiResponse(responseCode = "200",
                            description = "List of devices (possible empty)",
                            content = @Content(mediaType = "application/json", array = @ArraySchema(
                                    schema = @Schema(implementation = DeviceResponse.class)
                            ))),
                    @ApiResponse(responseCode = "400", description = "Invalid request parameters")
            })

    @GetMapping
    ResponseEntity<List<DeviceResponse>> getDevices(
            @Parameter(description = "Filter by device brand", example = "Apple")
            @RequestParam(value = "brand", required = false) String brand,
            @Parameter(description = "filter be device state", example = "AVAILABLE")
            @RequestParam(value = "state", required = false) State state);

    @Operation(summary = "Delete device by id", responses = {
            @ApiResponse(responseCode = "204", description = "Device has been deleted successfully."),
            @ApiResponse(responseCode = "404", description = "Device was not found, provided id is invalid.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "423", description = "Device cannot be deleted in its current state.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/{id}")
    ResponseEntity<Void> deleteDevice(@PathVariable("id") long id);

    @Operation(summary = "Update device by id", responses = {
            @ApiResponse(responseCode = "204", description = "Device has been updated successfully."),
            @ApiResponse(responseCode = "400", description = "Invalid input.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Device was not found, provided id is invalid.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "423", description = "Device cannot be updated in its current state.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping("/{id}")
    ResponseEntity<Void> updateDevice(@PathVariable("id") long id, @RequestBody @Valid CreateDeviceRequest request);


    @Operation(summary = "Partially update device by id", responses = {
            @ApiResponse(responseCode = "204", description = "Device has been updated successfully."),
            @ApiResponse(responseCode = "400", description = "Invalid input.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Device was not found, provided id is invalid.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "423", description = "Device cannot be updated in its current state.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PatchMapping("/{id}")
    ResponseEntity<Void> partialUpdate(@PathVariable("id") long id, @RequestBody PartialUpdateDeviceRequest request);
}
