package com.example.devicesapi.model;

import com.example.devicesapi.exception.InvalidInputPropertyException;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum State {
    AVAILABLE("available"), IN_USE("in-use"), INACTIVE("inactive");

    private final String value;

    State(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static State fromValue(String value) {
        if (value == null) {
            return null;
        }

        for (State state : State.values()) {
            if (state.value.equalsIgnoreCase(value)) {
                return state;
            }
        }
        throw new InvalidInputPropertyException("Unknown state value: " + value);
    }

    @Override
    public String toString() {
        return value;
    }
}
