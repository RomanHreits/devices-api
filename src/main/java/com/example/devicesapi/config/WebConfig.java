package com.example.devicesapi.config;

import com.example.devicesapi.converter.StateConverter;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;

@Configuration
public class WebConfig {

    public WebConfig(StateConverter stateConverter,
                     FormatterRegistry formatterRegistry) {
        formatterRegistry.addConverter(stateConverter);
    }
}
