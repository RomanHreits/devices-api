package com.example.devicesapi.controller;

import com.example.devicesapi.exception.BlockedResourceException;
import com.example.devicesapi.exception.DuplicatedDataException;
import com.example.devicesapi.exception.ResourceNotFoundException;
import com.example.devicesapi.model.CreateDeviceRequest;
import com.example.devicesapi.model.DeviceResponse;
import com.example.devicesapi.model.State;
import com.example.devicesapi.service.DeviceService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DeviceController.class)
public class DeviceControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DeviceService deviceService;

    @Test
    public void testCreateDeviceWithSuccessfulResponse() throws Exception {
        String requestJson = """
                {"name":"Device A","brand":"newBrand"}
                """;
        DeviceResponse deviceResponse = getDeviceResponse(State.AVAILABLE);

        when(deviceService.createDevice(any(CreateDeviceRequest.class)))
                .thenReturn(deviceResponse);

        mockMvc.perform(post("/devices").contentType(MediaType.APPLICATION_JSON).content(requestJson))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(deviceResponse.id()))
                .andExpect(jsonPath("$.name").value(deviceResponse.name()))
                .andExpect(jsonPath("$.brand").value(deviceResponse.brand()))
                .andExpect(jsonPath("$.state").value(deviceResponse.state()));;
    }

    @Test
    public void testCreateDeviceWithInvalidRequest() throws Exception {
        String requestJson = "{}";
        mockMvc.perform(post("/devices").contentType(MediaType.APPLICATION_JSON).content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.details", containsString("name:")));
    }

    @Test
    public void testCreateDeviceWhenServiceThrowsDuplicatedDataException() throws Exception {
        String requestJson = """
                {"name":"Device A","brand":"newBrand"}
                """;
        String errorMessage = "Device with the same name and brand already exists";

        when(deviceService.createDevice(any(CreateDeviceRequest.class)))
                .thenThrow(new DuplicatedDataException(errorMessage));

        mockMvc.perform(post("/devices").contentType(MediaType.APPLICATION_JSON).content(requestJson))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Database constraint violation"))
                .andExpect(jsonPath("$.details").value(errorMessage));
    }

    @Test
    public void testGetDeviceById() throws Exception {
        DeviceResponse deviceResponse = getDeviceResponse(State.IN_USE);

        when(deviceService.getDeviceById(any(Long.class)))
                .thenReturn(deviceResponse);
        mockMvc.perform(get("/devices/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(deviceResponse.id()))
                .andExpect(jsonPath("$.name").value(deviceResponse.name()))
                .andExpect(jsonPath("$.brand").value(deviceResponse.brand()))
                .andExpect(jsonPath("$.state").value(deviceResponse.state()));
    }

    @Test
    public void testGetByDeviceIdWhenDeviceDoesNotExist() throws Exception {
        when(deviceService.getDeviceById(any(Long.class)))
                .thenThrow(new ResourceNotFoundException("Device not found with id: 1"));

        mockMvc.perform(get("/devices/1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Resource not found"))
                .andExpect(jsonPath("$.details").value("Device not found with id: 1"));
    }

    @Test
    public void testGetDevicesListWithAllPossibleRequestParams() throws Exception {
        DeviceResponse deviceResponse = getDeviceResponse(State.AVAILABLE);
        when(deviceService.getDevices(anyString(), any(State.class))).thenReturn(List.of(deviceResponse));

        mockMvc.perform(get("/devices?brand=newBrand&state=available"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(deviceResponse.id()))
                .andExpect(jsonPath("$[0].name").value(deviceResponse.name()))
                .andExpect(jsonPath("$[0].brand").value(deviceResponse.brand()))
                .andExpect(jsonPath("$[0].state").value(deviceResponse.state()));
    }

    @Test
    public void testDeleteDeviceById() throws Exception {
        when(deviceService.getDeviceById(any(Long.class))).thenReturn(getDeviceResponse(State.INACTIVE));

        mockMvc.perform(delete("/devices/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    public void testDeleteDeviceByIdWhenDeviceDoesNotExist() throws Exception {
        String errorMessage = "Device not found with id: 1";
        doThrow(new ResourceNotFoundException(errorMessage))
                .when(deviceService).deleteDevice(any(Long.class));

        mockMvc.perform(delete("/devices/1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Resource not found"))
                .andExpect(jsonPath("$.details").value(errorMessage));
    }

    @Test
    public void testDeleteDeviceByIdWhenDeviceIsInUse() throws Exception {
        String errorMessage = "Cannot delete a device that is currently IN_USE";
        doThrow(new BlockedResourceException(errorMessage))
                .when(deviceService).deleteDevice(any(Long.class));

        mockMvc.perform(delete("/devices/1"))
                .andExpect(status().isLocked())
                .andExpect(jsonPath("$.message").value("Resource is blocked"))
                .andExpect(jsonPath("$.details").value(errorMessage));
    }

    @Test
    public void testUpdateDevice() throws Exception {
        String requestJson = """
                {"name":"Updated Device","brand":"Updated Brand","state":"in-use"}""";

        DeviceResponse deviceResponse = getDeviceResponse(State.IN_USE);
        when(deviceService.updateDevice(any(Long.class), any(CreateDeviceRequest.class)))
                .thenReturn(deviceResponse);

        mockMvc.perform(put("/devices/1").contentType(MediaType.APPLICATION_JSON).content(requestJson))
                .andExpect(status().isOk());
    }

    @Test
    public void testUpdateDeviceWhenDeviceDoesNotExist() throws Exception {
        String requestJson = """
                {"name":"Updated Device","brand":"Updated Brand","state":"in-use"}""";
        String errorMessage = "Device not found with id: 1";

        when(deviceService.updateDevice(any(Long.class), any(CreateDeviceRequest.class)))
                .thenThrow(new ResourceNotFoundException(errorMessage));

        mockMvc.perform(put("/devices/1").contentType(MediaType.APPLICATION_JSON).content(requestJson))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Resource not found"))
                .andExpect(jsonPath("$.details").value(errorMessage));
    }

    @Test
    public void testUpdateDeviceWhenDeviceIsInUse() throws Exception {
        String requestJson = """
                {"name":"Updated Device","brand":"Updated Brand","state":"in-use"}""";
        String errorMessage = "Cannot update a device that is currently IN_USE";

        when(deviceService.updateDevice(any(Long.class), any(CreateDeviceRequest.class)))
                .thenThrow(new BlockedResourceException(errorMessage));

        mockMvc.perform(put("/devices/1").contentType(MediaType.APPLICATION_JSON).content(requestJson))
                .andExpect(status().isLocked())
                .andExpect(jsonPath("$.message").value("Resource is blocked"))
                .andExpect(jsonPath("$.details").value(errorMessage));
    }

    @Test
    public void testUpdateDeviceWhenServiceThrowsDuplicatedDataException() throws Exception {
        String requestJson = """
                {"name":"Updated Device","brand":"Updated Brand","state":"in-use"}""";
        String errorMessage = "Device with the same name and brand already exists";

        when(deviceService.updateDevice(any(Long.class), any(CreateDeviceRequest.class)))
                .thenThrow(new DuplicatedDataException(errorMessage));

        mockMvc.perform(put("/devices/1").contentType(MediaType.APPLICATION_JSON).content(requestJson))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Database constraint violation"))
                .andExpect(jsonPath("$.details").value(errorMessage));
    }

    @Test
    public void testUpdateDeviceWithoutRequiredRequestFields() throws Exception {
        String requestJson = """
                {"brand":"Updated Brand","state":"available"}""";
        mockMvc.perform(put("/devices/1").contentType(MediaType.APPLICATION_JSON).content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.details", containsString("name:")));;
    }

    @Test
    public void testPartialUpdateDevice() throws Exception {
        String requestJson = """
                {"state":"inactive"}""";

        DeviceResponse deviceResponse = getDeviceResponse(State.INACTIVE);
        when(deviceService.partialUpdateDevice(any(Long.class), any()))
                .thenReturn(deviceResponse);

        mockMvc.perform(patch("/devices/1").contentType(MediaType.APPLICATION_JSON).content(requestJson))
                .andExpect(status().isOk());
    }

    @Test
    public void testPartialUpdateDeviceWhenDeviceDoesNotExist() throws Exception {
        String requestJson = """
                {"state":"inactive"}""";
        String errorMessage = "Device not found with id: 1";

        when(deviceService.partialUpdateDevice(any(Long.class), any()))
                .thenThrow(new ResourceNotFoundException(errorMessage));

        mockMvc.perform(patch("/devices/1").contentType(MediaType.APPLICATION_JSON).content(requestJson))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Resource not found"))
                .andExpect(jsonPath("$.details").value(errorMessage));
    }

    private DeviceResponse getDeviceResponse(State state) {
        return new DeviceResponse(
                1L,
                "Device A",
                "Brand A",
                state.getValue(),
                OffsetDateTime.now());
    }
}
