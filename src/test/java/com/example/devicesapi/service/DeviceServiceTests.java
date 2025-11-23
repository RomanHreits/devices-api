package com.example.devicesapi.service;

import com.example.devicesapi.entity.DeviceEntity;
import com.example.devicesapi.exception.BlockedResourceException;
import com.example.devicesapi.exception.DuplicatedDataException;
import com.example.devicesapi.exception.ResourceNotFoundException;
import com.example.devicesapi.mapper.DeviceMapper;
import com.example.devicesapi.model.CreateDeviceRequest;
import com.example.devicesapi.model.DeviceResponse;
import com.example.devicesapi.model.PartialUpdateDeviceRequest;
import com.example.devicesapi.model.State;
import com.example.devicesapi.repository.DeviceRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DeviceServiceTests {
    private static final String DEVICE_NAME = "Device1";
    private static final String DEVICE_BRAND = "BrandA";
    private static final Instant CREATED_AT = Instant.now();
    private static final Long DEVICE_ID = 1L;

    @Mock
    private DeviceRepository deviceRepository;

    @Mock
    private DeviceMapper deviceMapper;

    @InjectMocks
    private DeviceService deviceService;

    @Test
    public void testCreateDevice() {
        CreateDeviceRequest createDeviceRequest =
                new CreateDeviceRequest(DEVICE_NAME, DEVICE_BRAND, null);
        DeviceEntity savedEntity = getDeviceEntity(State.INACTIVE);

        when(deviceMapper.createDeviceRequestToEntity(createDeviceRequest)).thenCallRealMethod();
        when(deviceRepository.save(any(DeviceEntity.class))).thenReturn(savedEntity);
        when(deviceMapper.fromEntityToDeviceResponse(savedEntity)).thenCallRealMethod();

        DeviceResponse response = deviceService.createDevice(createDeviceRequest);

        assertNotNull(response);
        assertDeviceResponse(response, State.INACTIVE);
    }

    @Test
    public void testCreateDeviceWhenEntityWasCreatedBefore() {
        CreateDeviceRequest createDeviceRequest =
                new CreateDeviceRequest(DEVICE_NAME, DEVICE_BRAND, null);
        when(deviceMapper.createDeviceRequestToEntity(createDeviceRequest)).thenCallRealMethod();
        when(deviceRepository.save(any(DeviceEntity.class)))
                .thenThrow(new DataIntegrityViolationException("Data integrity violation"));

        assertThrows(DuplicatedDataException.class, () -> deviceService.createDevice(createDeviceRequest));
    }

    @Test
    public void testGetDeviceById() {
        DeviceEntity entity = getDeviceEntity(State.IN_USE);

        when(deviceRepository.findById(DEVICE_ID)).thenReturn(Optional.of(entity));
        when(deviceMapper.fromEntityToDeviceResponse(entity)).thenCallRealMethod();

        DeviceResponse response = deviceService.getDeviceById(DEVICE_ID);

        assertNotNull(response);
        assertDeviceResponse(response, State.IN_USE);
    }

    @Test
    public void testGetByIdWhenNotFound() {
        when(deviceRepository.findById(DEVICE_ID)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> deviceService.getDeviceById(DEVICE_ID));
    }

    @Test
    public void testUpdateDevice() {
        String updatedDeviceName = "UpdatedDeviceName";
        String updatedDeviceBrand = "UpdatedBrand";

        CreateDeviceRequest updateRequest =
                new CreateDeviceRequest(updatedDeviceName, updatedDeviceBrand, State.IN_USE);
        DeviceEntity existingEntity = getDeviceEntity(State.AVAILABLE);
        DeviceEntity updatedEntity =
                new DeviceEntity(DEVICE_ID, updatedDeviceName, updatedDeviceBrand, State.IN_USE.getValue(), CREATED_AT);


        when(deviceRepository.findById(DEVICE_ID)).thenReturn(Optional.of(existingEntity));
        when(deviceMapper.createDeviceRequestToEntity(updateRequest, DEVICE_ID))
                .thenCallRealMethod();
        when(deviceRepository.save(any(DeviceEntity.class)))
                .thenReturn(updatedEntity);
        when(deviceMapper.fromEntityToDeviceResponse(any(DeviceEntity.class)))
                .thenCallRealMethod();

        DeviceResponse deviceResponse = deviceService.updateDevice(DEVICE_ID, updateRequest);

        assertNotNull(deviceResponse);
        assertEquals(DEVICE_ID, deviceResponse.id());
        assertEquals(updatedDeviceName, deviceResponse.name());
        assertEquals(updatedDeviceBrand, deviceResponse.brand());
        assertEquals(State.IN_USE.getValue(), deviceResponse.state());
    }

    @Test
    public void testUpdateDeviceWhenNotFound() {
        CreateDeviceRequest updateRequest = mock(CreateDeviceRequest.class);

        when(deviceRepository.findById(DEVICE_ID)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class,
                () -> deviceService.updateDevice(DEVICE_ID, updateRequest));
    }

    @Test
    public void testUpdateDeviceWhenInUse() {
        CreateDeviceRequest updateRequest = mock(CreateDeviceRequest.class);
        DeviceEntity existingEntity = getDeviceEntity(State.IN_USE);

        when(deviceRepository.findById(DEVICE_ID)).thenReturn(Optional.of(existingEntity));
        assertThrows(BlockedResourceException.class,
                () -> deviceService.updateDevice(DEVICE_ID, updateRequest));
    }

    @Test
    public void testUpdateDeviceWhenDataIntegrityViolation() {
        CreateDeviceRequest updateRequest =
                new CreateDeviceRequest("updatedName", "updatedBrand", State.AVAILABLE);

        DeviceEntity existingEntity = getDeviceEntity(State.INACTIVE);

        when(deviceRepository.findById(DEVICE_ID)).thenReturn(Optional.of(existingEntity));
        when(deviceMapper.createDeviceRequestToEntity(updateRequest, DEVICE_ID))
                .thenCallRealMethod();
        when(deviceRepository.save(any(DeviceEntity.class)))
                .thenThrow(new DataIntegrityViolationException("Data integrity violation"));

        assertThrows(DuplicatedDataException.class,
                () -> deviceService.updateDevice(DEVICE_ID, updateRequest));
    }

    @Test
    public void testDeleteDevice() {
        DeviceEntity existingEntity = getDeviceEntity(State.AVAILABLE);

        when(deviceRepository.findById(DEVICE_ID)).thenReturn(Optional.of(existingEntity));

        assertDoesNotThrow(() -> deviceService.deleteDevice(DEVICE_ID));
    }

    @Test
    public void testDeleteDeviceWhenNotFound() {
        when(deviceRepository.findById(DEVICE_ID)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class,
                () -> deviceService.deleteDevice(DEVICE_ID));
    }

    @Test
    public void testDeleteDeviceWhenInUse() {
        DeviceEntity existingEntity = getDeviceEntity(State.IN_USE);

        when(deviceRepository.findById(DEVICE_ID)).thenReturn(Optional.of(existingEntity));
        assertThrows(BlockedResourceException.class,
                () -> deviceService.deleteDevice(DEVICE_ID));
    }

    @Test
    public void testPartialUpdateDevice() {

        ArgumentCaptor<DeviceEntity> entityCaptor =
                ArgumentCaptor.forClass(DeviceEntity.class);

        PartialUpdateDeviceRequest updateRequest =
                new PartialUpdateDeviceRequest(null, State.IN_USE, null);
        DeviceEntity existingEntity = getDeviceEntity(State.AVAILABLE);
        DeviceEntity savedEntity = getDeviceEntity(State.IN_USE);

        when(deviceRepository.findById(DEVICE_ID)).thenReturn(Optional.of(existingEntity));
        when(deviceMapper.partialUpdateRequestToEntity(updateRequest, existingEntity)).thenCallRealMethod();
        when(deviceRepository.save(entityCaptor.capture())).thenReturn(savedEntity);
        when(deviceMapper.fromEntityToDeviceResponse(savedEntity)).thenCallRealMethod();

        DeviceResponse deviceResponse = deviceService.partialUpdateDevice(DEVICE_ID, updateRequest);

        assertNotNull(deviceResponse);
        assertDeviceResponse(deviceResponse, State.IN_USE);

        DeviceEntity captured = entityCaptor.getValue();

        assertEquals(savedEntity.getBrand(), captured.getBrand());
        assertEquals(savedEntity.getName(), captured.getName());
        assertEquals(savedEntity.getState(), captured.getState());
    }

    @Test
    public void testPartialUpdateDeviceWhenNotFound() {
        PartialUpdateDeviceRequest updateRequest = mock(PartialUpdateDeviceRequest.class);

        when(deviceRepository.findById(DEVICE_ID)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class,
                () -> deviceService.partialUpdateDevice(DEVICE_ID, updateRequest));
    }

    @Test
    public void testPartialUpdateDeviceWhenInUseAndUpdatingNameOrBrand() {

        PartialUpdateDeviceRequest updateRequest =
                new PartialUpdateDeviceRequest("UpdatedName", null, null);
        DeviceEntity existingEntity = getDeviceEntity(State.IN_USE);

        when(deviceRepository.findById(DEVICE_ID)).thenReturn(Optional.of(existingEntity));
        assertThrows(BlockedResourceException.class,
                () -> deviceService.partialUpdateDevice(DEVICE_ID, updateRequest));
    }

    @Test
    public void testPartialUpdateDeviceWhenDataIntegrityViolation() {

        PartialUpdateDeviceRequest updateRequest =
                new PartialUpdateDeviceRequest("updatedName", State.AVAILABLE, "updatedBrand");
        DeviceEntity existingEntity = getDeviceEntity(State.INACTIVE);

        when(deviceRepository.findById(DEVICE_ID)).thenReturn(Optional.of(existingEntity));
        when(deviceMapper.partialUpdateRequestToEntity(updateRequest, existingEntity)).thenCallRealMethod();
        when(deviceRepository.save(any(DeviceEntity.class)))
                .thenThrow(new DataIntegrityViolationException("Data integrity violation"));

        assertThrows(DuplicatedDataException.class,
                () -> deviceService.partialUpdateDevice(DEVICE_ID, updateRequest));
    }
    
    @Test
    public void testGetDevicesByBrandAndState() {
        DeviceEntity entity = getDeviceEntity(State.IN_USE);
        when(deviceRepository.findByBrandAndState(DEVICE_BRAND, State.IN_USE.getValue()))
                .thenReturn(List.of(entity));

        when(deviceMapper.fromEntityToDeviceResponse(entity)).thenCallRealMethod();

        List<DeviceResponse> devices = deviceService.getDevices(DEVICE_BRAND, State.IN_USE);
        assertNotNull(devices);
        assertEquals(1, devices.size());
        assertDeviceResponse(devices.getFirst(), State.IN_USE);
    }

    @Test
    public void testGetDevicesWithoutFilters() {
        DeviceEntity entity = getDeviceEntity(State.IN_USE);

        when(deviceRepository.findAll()).thenReturn(List.of(entity));
        when(deviceMapper.fromEntityToDeviceResponse(entity)).thenCallRealMethod();

        List<DeviceResponse> devices = deviceService.getDevices(null, null);
        assertNotNull(devices);
        assertEquals(1, devices.size());
        assertDeviceResponse(devices.getFirst(), State.IN_USE);
    }

    @Test
    public void testGetDevicesByBrandOnly() {
        DeviceEntity entity = getDeviceEntity(State.IN_USE);
        when(deviceRepository.findByBrand(DEVICE_BRAND)).thenReturn(List.of(entity));

        when(deviceMapper.fromEntityToDeviceResponse(entity)).thenCallRealMethod();

        List<DeviceResponse> devices = deviceService.getDevices(DEVICE_BRAND, null);
        assertNotNull(devices);
        assertEquals(1, devices.size());
        assertDeviceResponse(devices.getFirst(), State.IN_USE);
    }

    @Test
    public void testGetDevicesByStateOnly() {
        DeviceEntity entity = getDeviceEntity(State.IN_USE);

        when(deviceRepository.findByState(State.IN_USE.getValue())).thenReturn(List.of(entity));
        when(deviceMapper.fromEntityToDeviceResponse(entity)).thenCallRealMethod();

        List<DeviceResponse> devices = deviceService.getDevices(null, State.IN_USE);
        assertNotNull(devices);
        assertEquals(1, devices.size());
        assertDeviceResponse(devices.getFirst(), State.IN_USE);
    }

    private void assertDeviceResponse(DeviceResponse response, State state) {
        assertNotNull(response);
        assertEquals(DEVICE_ID, response.id());
        assertEquals(DEVICE_NAME, response.name());
        assertEquals(DEVICE_BRAND, response.brand());
        assertEquals(state.getValue(), response.state());
        assertEquals(CREATED_AT, response.createdAt().toInstant());
    }

    private DeviceEntity getDeviceEntity(State state) {
        return new DeviceEntity(DEVICE_ID, DEVICE_NAME, DEVICE_BRAND, state.getValue(), CREATED_AT);
    }
}
