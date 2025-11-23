package com.example.devicesapi.repository;

import com.example.devicesapi.config.DataConfig;
import com.example.devicesapi.entity.DeviceEntity;
import com.example.devicesapi.model.State;
import org.junit.jupiter.api.BeforeAll;
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
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
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
    private final String NAME_1 = "testName";
    private final String BRAND_1 = "testBrand";
    private final String NAME_2 = "testName2";
    private final String BRAND_2 = "testBrand2";
    private final String UPDATED_NAME = "updatedName";
    private Instant createdAt;

    @BeforeAll
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Commit
    public void setUp() {
        deviceRepository.deleteAll();
        DeviceEntity first = new DeviceEntity(NAME_1, BRAND_1, State.INACTIVE.getValue());
        DeviceEntity second = new DeviceEntity(NAME_2, BRAND_2, State.INACTIVE.getValue());
        deviceRepository.saveAll(List.of(first, second));
    }

    @Test
    @Order(1)
    public void testGetAll() {
        List<DeviceEntity> allDevices = deviceRepository.findAll();
        assertEquals(2, allDevices.size());
    }

    @Test
    @Order(2)
    public void testFindByBrand() {
        List<DeviceEntity> devices = deviceRepository.findByBrand(BRAND_1);
        assertEquals(1, devices.size());
        assertEquals(NAME_1, devices.getFirst().getName());
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
        assertEquals(NAME_2, deviceOptional.get().getName());
    }

    @Test
    @Order(5)
    @Commit
    public void updateDevice() {
        DeviceEntity entity = deviceRepository.findById(1L).get();
        createdAt = entity.getCreatedAt();
        DeviceEntity newEntity = new DeviceEntity(
                entity.getId(),
                UPDATED_NAME,
                entity.getBrand(),
                State.IN_USE.getValue()
        );
        assertDoesNotThrow(() -> deviceRepository.save(newEntity));
    }

    @Test
    @Order(6)
    @Commit
    public void testDeleteDevice() {
        DeviceEntity entity = deviceRepository.findById(1L).get();
        // Check the previous update operation was applied and committed to the database
        assertEquals(UPDATED_NAME, entity.getName());
        assertEquals(State.IN_USE.getValue(), entity.getState());
        assertEquals(createdAt, entity.getCreatedAt());

        assertDoesNotThrow(() -> deviceRepository.deleteById(1L));
    }

    @Test
    @Order(7)
    public void testGetAllAfterDelete() {
        List<DeviceEntity> allDevices = deviceRepository.findAll();
        assertEquals(1, allDevices.size());
    }

    private static Stream<Arguments> findByStateParameters() {
        return Stream.of(
                Arguments.of(State.INACTIVE, 2),
                Arguments.of(State.AVAILABLE, 0)
        );
    }
}
