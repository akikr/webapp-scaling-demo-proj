package com.webapp.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "rest-client.logging")
public record RestClientLoggingProperties(
        @DefaultValue("false") boolean enabled,
        @DefaultValue("false") boolean includeRequestHeaders,
        @DefaultValue("false") boolean includeRequestBody,
        @DefaultValue("false") boolean includeResponseHeaders,
        @DefaultValue("false") boolean includeResponseBody,
        @DefaultValue("1000") int maxBodyLength) {
}
