package com.example.devicesapi.repository;

import com.example.devicesapi.config.DataConfig;
import com.example.devicesapi.entity.DeviceEntity;
import com.example.devicesapi.model.State;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jdbc.test.autoconfigure.DataJdbcTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.Commit;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static com.example.devicesapi.TestUtil.DEVICE_BRAND_1;
import static com.example.devicesapi.TestUtil.DEVICE_NAME_1;
import static com.example.devicesapi.TestUtil.DEVICE_NAME_2;
import static com.example.devicesapi.TestUtil.UPDATED_DEVICE_NAME_1;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJdbcTest
@Import({DataConfig.class})
@AutoConfigureTestDatabase(
        replace = AutoConfigureTestDatabase.Replace.NONE
)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ActiveProfiles("test")
public class DeviceRepositoryTest {
    @Autowired
    private DeviceRepository deviceRepository;
    private Instant createdAt;

    @Test
    @Order(1)
    public void testGetAll() {
        List<DeviceEntity> allDevices = deviceRepository.findAll();
        assertEquals(3, allDevices.size()); // 3 items were pre-loaded from test-data.sql
    }

    @Test
    @Order(2)
    public void testFindByBrand() {
        List<DeviceEntity> devices = deviceRepository.findByBrand(DEVICE_BRAND_1);
        assertEquals(1, devices.size());
        assertEquals(DEVICE_NAME_1, devices.getFirst().getName());
        assertNotNull(devices.getFirst().getCreatedAt());
    }

    @ParameterizedTest
    @MethodSource("findByStateParameters")
    @Order(3)
    public void testFindByState(State state, int expectedCound) {
        List<DeviceEntity> byState = deviceRepository.findByState(state.getValue());
        assertEquals(expectedCound, byState.size());
    }

    @Test
    @Order(4)
    public void testFindById() {
        Optional<DeviceEntity> deviceOptional = deviceRepository.findById(2L);
        assertTrue(deviceOptional.isPresent());
        assertEquals(DEVICE_NAME_2, deviceOptional.get().getName());
    }

    @Test
    @Order(5)
    @Commit
    public void updateDevice() {
        DeviceEntity entity = deviceRepository.findById(1L).get();
        createdAt = entity.getCreatedAt();
        DeviceEntity newEntity = new DeviceEntity(
                entity.getId(),
                UPDATED_DEVICE_NAME_1,
                entity.getBrand(),
                State.IN_USE.getValue(),
                Instant.now()
        );

        assertNotEquals(createdAt, newEntity.getCreatedAt());
        assertDoesNotThrow(() -> deviceRepository.save(newEntity));
    }

    @Test
    @Order(6)
    @Commit
    public void testDeleteDevice() {
        DeviceEntity entity = deviceRepository.findById(1L).get();
        // Check the previous update operation was applied and committed to the database
        assertEquals(UPDATED_DEVICE_NAME_1, entity.getName());
        assertEquals(State.IN_USE.getValue(), entity.getState());
        assertEquals(createdAt, entity.getCreatedAt());

        assertDoesNotThrow(() -> deviceRepository.deleteById(1L));
    }

    @Test
    @Order(7)
    public void testGetAllAfterDelete() {
        List<DeviceEntity> allDevices = deviceRepository.findAll();
        assertEquals(2, allDevices.size());
    }

    private static Stream<Arguments> findByStateParameters() {
        return Stream.of(
                Arguments.of(State.INACTIVE, 3),
                Arguments.of(State.AVAILABLE, 0)
        );
    }
}
