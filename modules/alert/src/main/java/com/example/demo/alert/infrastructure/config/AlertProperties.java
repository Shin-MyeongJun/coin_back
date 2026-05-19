package com.example.demo.alert.infrastructure.config;

import com.example.demo.alert.domain.domain.Channel;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.LinkedHashSet;
import java.util.Set;

@ConfigurationProperties(prefix = "app.alert")
public record AlertProperties(
        Set<Channel> defaultChannels
) {
    public AlertProperties {
        defaultChannels = defaultChannels == null
                ? Set.of()
                : Set.copyOf(new LinkedHashSet<>(defaultChannels));
    }
}
