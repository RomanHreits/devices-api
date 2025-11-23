package com.example.devicesapi.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DeviceController.class)
public class DeviceControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testCreateDeviceWithSuccessfulResponse() throws Exception {
        String requestJson = """
                {"name":"Device A","brand":"newBrand"}
                """;
        mockMvc.perform(post("/devices").contentType(MediaType.APPLICATION_JSON).content(requestJson))
                .andExpect(status().isCreated());
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
    public void testGetDeviceById() throws Exception {
        mockMvc.perform(get("/devices/1"))
                .andExpect(status().isOk());
    }

    @Test
    public void testGetDevicesListWithAllPossibleRequestParams() throws Exception {
        mockMvc.perform(get("/devices?brand=newBrand&state=AVAILABLE"))
                .andExpect(status().isOk());
    }

    @Test
    public void testDeleteDeviceById() throws Exception {
        mockMvc.perform(delete("/devices/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    public void testUpdateDevice() throws Exception {
        String requestJson = """
                {"name":"Updated Device","brand":"Updated Brand","state":"in-use"}""";
        mockMvc.perform(put("/devices/1").contentType(MediaType.APPLICATION_JSON).content(requestJson))
                .andExpect(status().isOk());
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
}
