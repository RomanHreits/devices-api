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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DeviceService {
    private static final Logger logger = LoggerFactory.getLogger(DeviceService.class);
    private final DeviceRepository deviceRepository;
    private final DeviceMapper deviceMapper;

    public DeviceService(DeviceRepository deviceRepository,
                         DeviceMapper deviceMapper) {
        this.deviceRepository = deviceRepository;
        this.deviceMapper = deviceMapper;
    }

    /**
     * Persist a new device and return its DTO.
     *
     * Steps:
     * - Map request to {@code DeviceEntity}
     * - Save entity via repository
     * - Map saved entity to {@code DeviceResponse}
     *
     * @param deviceRequest the request with device data
     * @return created {@code DeviceResponse}
     * @throws DuplicatedDataException when a device with the same name and brand exists
     */
    public DeviceResponse createDevice(CreateDeviceRequest deviceRequest) {
        logger.info("Creating new device with name: {} and brand: {}",
                deviceRequest.name(), deviceRequest.brand());
        try {
            DeviceEntity saved = deviceRepository.save(
                    deviceMapper.createDeviceRequestToEntity(deviceRequest));
            logger.info("Created new device with id: {}", saved.getId());
            return deviceMapper.fromEntityToDeviceResponse(saved);
        } catch (DataIntegrityViolationException e) {
            logger.error("Data integrity violation while creating device: {}", e.getMessage());
            throw new DuplicatedDataException("Device with the same name and brand already exists");
        }
    }

    /**
     * Fetch a device by id and return its DTO.
     *
     * Steps:
     * - Query repository by id
     * - Map entity to {@code DeviceResponse} if found
     *
     * @param id the device id
     * @return {@code DeviceResponse} for the given id
     * @throws ResourceNotFoundException when device not found
     */
    public DeviceResponse getDeviceById(long id) {
        return deviceRepository.findById(id)
                .map(deviceMapper::fromEntityToDeviceResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Device not found with id: " + id));
    }

    /**
     * Fully update an existing device and return updated DTO.
     *
     * Steps:
     * - Load existing entity by id
     * - Validate state (cannot update if IN_USE)
     * - Map incoming request to entity preserving id and save
     *
     * @param id the device id
     * @param deviceRequest the full device update request
     * @return updated {@code DeviceResponse}
     * @throws ResourceNotFoundException when device not found
     * @throws BlockedResourceException when device is IN_USE
     * @throws DuplicatedDataException when name+brand duplicates an existing device
     */
    public DeviceResponse updateDevice(long id, CreateDeviceRequest deviceRequest) {
        DeviceEntity existingDevice = deviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Device not found with id: {}" + id));

        if (existingDevice.getState().equals(State.IN_USE.getValue())) {
            throw new BlockedResourceException("Cannot update a device that is currently IN_USE");
        }

        existingDevice = deviceMapper.createDeviceRequestToEntity(deviceRequest, id);

        try {
            DeviceEntity updatedDevice = deviceRepository.save(existingDevice);
            return deviceMapper.fromEntityToDeviceResponse(updatedDevice);
        } catch (DataIntegrityViolationException e) {
            logger.error("Data integrity violation while updating device: {}", e.getMessage());
            throw new DuplicatedDataException("Device with the same name and brand already exists");
        }
    }

    /**
     * Delete a device by id and return the deleted device DTO (state before deletion).
     *
     * Steps:
     * - Load existing entity by id
     * - Validate state (cannot delete if IN_USE)
     * - Delete via repository and return previous representation
     *
     * @param id the device id
     * @return {@code DeviceResponse} representing the deleted device
     * @throws ResourceNotFoundException when device not found
     * @throws BlockedResourceException when device is IN_USE
     */
    public DeviceResponse deleteDevice(long id) {
        Optional<DeviceEntity> optionalEntity = deviceRepository.findById(id);
        if (optionalEntity.isEmpty()) {
            logger.error("Attempted to delete non-existing device with id: {}", id);
            throw new ResourceNotFoundException("Device not found with id: " + id);
        } else if (isInUse(optionalEntity.get().getState())) {
            logger.error("Attempted to delete device in IN_USE state with id: {}", id);
            throw new BlockedResourceException("Cannot delete a device that is currently IN_USE");
        }
        deviceRepository.deleteById(id);
        return deviceMapper.fromEntityToDeviceResponse(optionalEntity.get());
    }


    /**
     * Apply partial updates to a device and return updated DTO.
     *
     * Steps:
     * - Load existing entity by id
     * - Validate that name/brand are not changed when device is IN_USE
     * - Apply partial changes and save
     *
     * @param id the device id
     * @param updateRequest partial update request
     * @return updated {@code DeviceResponse}
     * @throws ResourceNotFoundException when device not found
     * @throws BlockedResourceException when forbidden fields are modified on IN_USE device
     * @throws DuplicatedDataException when name+brand duplicates an existing device
     */
    public DeviceResponse partialUpdateDevice(long id, PartialUpdateDeviceRequest updateRequest) {
        logger.info("Starting partial update for device with id: {}", id);
        DeviceEntity existingDevice = deviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Device not found with id: " + id));

        boolean inUse = isInUse(existingDevice.getState());

        if (inUse && (updateRequest.brand() != null || updateRequest.name() != null)) {
            throw new BlockedResourceException("Cannot update brand or name of a device that is currently IN_USE");
        }

        try {
            DeviceEntity savedDevice = deviceRepository.save(
                    deviceMapper.partialUpdateRequestToEntity(updateRequest, existingDevice));
            logger.info("Successfully completed partial update for device with id: {}", id);

            return deviceMapper.fromEntityToDeviceResponse(savedDevice);
        } catch (DataIntegrityViolationException e) {
            logger.error("Data integrity violation while partially updating device: {}", e.getMessage());
            throw new DuplicatedDataException("Device with the same name and brand already exists");
        }
    }

    /**
     * Retrieve a list of devices optionally filtered by brand and/or state.
     *
     * Steps:
     * - Choose repository query based on provided filters
     * - Map resulting entities to {@code DeviceResponse} list
     *
     * @param brand optional brand filter
     * @param state optional state filter
     * @return list of {@code DeviceResponse} matching the provided filters
     */
    public List<DeviceResponse> getDevices(String brand, State state) {

        List<DeviceEntity> entities;
        if (brand != null && state != null) {
            logger.info("Fetching devices with brand: {} and state: {}", brand, state);
            entities = deviceRepository.findByBrandAndState(brand, state.getValue());
        } else if (brand != null) {
            logger.info("Fetching devices with brand: {}", brand);
           entities = deviceRepository.findByBrand(brand);
        } else if (state != null) {
            logger.info("Fetching devices with state: {}", state);
           entities = deviceRepository.findByState(state.getValue());
        } else {
            logger.info("Fetching all devices");
            entities = deviceRepository.findAll();
        }

        logger.info("Found {} devices matching criteria", entities.size());

        return entities.stream()
                .map(deviceMapper::fromEntityToDeviceResponse)
                .toList();
    }

    private boolean isInUse(String state) {
        return State.IN_USE.getValue().equals(state);
    }
}
