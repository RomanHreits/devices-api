package com.example.devicesapi.controller;

import com.example.devicesapi.model.CreateDeviceRequest;
import com.example.devicesapi.model.DeviceResponse;
import com.example.devicesapi.model.ErrorResponse;
import com.example.devicesapi.model.State;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static com.example.devicesapi.TestUtil.DEVICE_BRAND_1;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase(
        replace = AutoConfigureTestDatabase.Replace.NONE
)
@AutoConfigureTestRestTemplate
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DeviceControllerFunctionalTests {
    @Autowired
    private TestRestTemplate testRestTemplate;

    @Test
    @Order(1)
    public void getDevices_ShouldReturnOk() {
        ResponseEntity<List<DeviceResponse>> resp = testRestTemplate.exchange(
                "/devices",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {
                }
        );

        assertTrue(resp.getStatusCode().is2xxSuccessful());
        assertNotNull(resp.getBody());
        assertEquals(3, resp.getBody().size());
    }

    @Test
    @Order(2)
    public void getDeviceById_ShouldReturnOk() {
        ResponseEntity<DeviceResponse> resp = testRestTemplate.getForEntity(
                "/devices/1",
                DeviceResponse.class
        );

        assertTrue(resp.getStatusCode().is2xxSuccessful());
        assertNotNull(resp.getBody());
        assertEquals(1L, resp.getBody().id());
        assertNotNull(resp.getBody().createdAt());
    }

    @Test
    @Order(3)
    public void getDeviceById_NotFound_ShouldReturn404() {
        ResponseEntity<ErrorResponse> resp = testRestTemplate.getForEntity(
                "/devices/999",
                ErrorResponse.class
        );

        assertEquals(404, resp.getStatusCode().value());
        assertNotNull(resp.getBody());
        assertNotNull(resp.getBody().message());
        assertNotNull(resp.getBody().details());
    }

    @Test
    @Order(4)
    public void createDevice_ShouldReturnCreated() {
        CreateDeviceRequest request = new CreateDeviceRequest(
                "New Device",
                "New Brand",
                null);
        ResponseEntity<DeviceResponse> resp = testRestTemplate.postForEntity(
                "/devices",
                request,
                DeviceResponse.class);

        assertEquals(201, resp.getStatusCode().value());
        assertNotNull(resp.getBody());
        assertEquals("New Device", resp.getBody().name());
        assertEquals("New Brand", resp.getBody().brand());
        assertNotNull(resp.getBody().createdAt());
    }

    @Test
    @Order(5)
    public void createDevice_InvalidInput_ShouldReturn400() {
        CreateDeviceRequest request = new CreateDeviceRequest(
                "",  // Invalid name
                "New Brand",
                null);
        ResponseEntity<ErrorResponse> resp = testRestTemplate.postForEntity(
                "/devices",
                request,
                ErrorResponse.class);

        assertEquals(400, resp.getStatusCode().value());
        assertNotNull(resp.getBody());
        assertNotNull(resp.getBody().message());
        assertNotNull(resp.getBody().details());
    }

    @Test
    @Order(6)
    public void getDevices_FilterByBrand_ShouldReturnOk() {
        ResponseEntity<List<DeviceResponse>> resp = testRestTemplate.exchange(
                "/devices?brand=%s".formatted(DEVICE_BRAND_1),
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {
                }
        );

        assertTrue(resp.getStatusCode().is2xxSuccessful());
        assertNotNull(resp.getBody());
        assertEquals(1, resp.getBody().size());
    }

    @Test
    @Order(7)
    public void deleteDevice_NotFound_ShouldReturn404() {
        ResponseEntity<ErrorResponse> resp = testRestTemplate.exchange(
                "/devices/999",
                HttpMethod.DELETE,
                null,
                ErrorResponse.class
        );

        assertEquals(404, resp.getStatusCode().value());
        assertNotNull(resp.getBody());
        assertNotNull(resp.getBody().message());
        assertNotNull(resp.getBody().details());
    }

    @Test
    @Order(8)
    public void partialUpdateDevice_ShouldReturnOk() {
        String patchBody = """
                {
                    "state": "in-use"
                }
                """;

        ResponseEntity<DeviceResponse> resp = testRestTemplate.exchange(
                "/devices/2",
                HttpMethod.PATCH,
                org.springframework.http.RequestEntity
                        .patch("/devices/2")
                        .header("Content-Type", "application/json")
                        .body(patchBody),
                DeviceResponse.class
        );

        assertTrue(resp.getStatusCode().is2xxSuccessful());
        assertNotNull(resp.getBody());
        assertEquals(2L, resp.getBody().id());
        assertEquals(State.IN_USE.getValue(), resp.getBody().state());
    }

    @Test
    @Order(9)
    public void deleteDeviceById_ShouldReturnLocked() {
        ResponseEntity<ErrorResponse> resp = testRestTemplate.exchange(
                "/devices/2",
                HttpMethod.DELETE,
                null,
                ErrorResponse.class
        );

        assertEquals(423, resp.getStatusCode().value());
        assertNotNull(resp.getBody());
        assertNotNull(resp.getBody().message());
        assertNotNull(resp.getBody().details());
    }

    @Test
    @Order(10)
    public void updateDevice_ThatIsInUse_ShouldReturnLocked() {
        CreateDeviceRequest request = new CreateDeviceRequest(
                "Updated Device Name",
                "Updated Brand",
                State.IN_USE);
        ResponseEntity<ErrorResponse> resp = testRestTemplate.exchange(
                "/devices/2",
                HttpMethod.PUT,
                org.springframework.http.RequestEntity
                        .put("/devices/2")
                        .header("Content-Type", "application/json")
                        .body(request),
                ErrorResponse.class
        );

        assertEquals(423, resp.getStatusCode().value());
        assertNotNull(resp.getBody());
        assertNotNull(resp.getBody().message());
        assertNotNull(resp.getBody().details());
    }

    @Test
    @Order(11)
    public void partialUpdateDevice_ThatIsInUse_ShouldReturnLocked() {
        String patchBody = """
                {
                    "name": "Partially Updated Device Name"
                }
                """;

        ResponseEntity<ErrorResponse> resp = testRestTemplate.exchange(
                "/devices/2",
                HttpMethod.PATCH,
                org.springframework.http.RequestEntity
                        .patch("/devices/3")
                        .header("Content-Type", "application/json")
                        .body(patchBody),
                ErrorResponse.class
        );

        assertEquals(423, resp.getStatusCode().value());
        assertNotNull(resp.getBody());
        assertNotNull(resp.getBody().message());
        assertNotNull(resp.getBody().details());
    }

    @Test
    @Order(12)
    public void deleteDevice_ShouldReturnOk() {
        ResponseEntity<DeviceResponse> resp = testRestTemplate.exchange(
                "/devices/3",
                HttpMethod.DELETE,
                null,
                DeviceResponse.class
        );

        assertTrue(resp.getStatusCode().is2xxSuccessful());
        assertNotNull(resp.getBody());
        assertEquals(3L, resp.getBody().id());
    }
}
