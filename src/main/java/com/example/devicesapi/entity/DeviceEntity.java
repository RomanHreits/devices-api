package com.example.devicesapi.entity;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.ReadOnlyProperty;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;

@Table("devices")
public class DeviceEntity {
    @Id
    private long id;
    private String name;
    private String brand;
    private String state;
    @CreatedDate
    @ReadOnlyProperty
    private Instant createdAt;

    public DeviceEntity() {
    }

    public DeviceEntity(String name, String brand, String state) {
        this.name = name;
        this.brand = brand;
        this.state = state;
    }

    public DeviceEntity(long id, String name, String brand, String state) {
        this.id = id;
        this.name = name;
        this.brand = brand;
        this.state = state;
    }

    // Constructor including createdAt - using for testing purposes
    public DeviceEntity(long id, String name, String brand, String state, Instant createdAt) {
        this.id = id;
        this.name = name;
        this.brand = brand;
        this.state = state;
        this.createdAt = createdAt;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getBrand() {
        return brand;
    }

    public String getState() {
        return state;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
