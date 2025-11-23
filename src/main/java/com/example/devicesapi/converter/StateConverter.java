package com.example.devicesapi.converter;

import com.example.devicesapi.model.State;
import org.jspecify.annotations.Nullable;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class StateConverter implements Converter<String, State> {
    @Override
    public @Nullable State convert(String source) {
        if (source == null || source.isEmpty()) {
            return null;
        }
        return State.fromValue(source);
    }
}
