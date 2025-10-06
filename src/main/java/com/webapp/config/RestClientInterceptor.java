package com.webapp.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static java.util.Objects.nonNull;

public interface RestClientInterceptor {

    Logger log = LoggerFactory.getLogger(RestClientInterceptor.class);

    static void logRequest(HttpRequest request, byte[] requestBody, ClientLoggingProperties properties) {
        log.info("Client-Request: {} {}", request.getMethod(), request.getURI());

        var requestHeaders = properties.includeRequestHeaders() ? request.getHeaders().toString() : "[Headers not logged]";
        log.debug("Client-Request-Headers: {}", requestHeaders);

        var body = "[Body not logged]";
        if (properties.includeRequestBody()) {
            body = (nonNull(requestBody) && requestBody.length > 0)
                    ? new String(requestBody, StandardCharsets.UTF_8) : "[No-Body]";
            if (body.length() > properties.maxBodyLength()) {
                body = body.substring(0, properties.maxBodyLength()) + "...[truncated]";
            }
        }
        log.debug("Client-Request-Body: {}", body);
    }

    static void logResponse(ClientHttpResponse response, ClientLoggingProperties properties) throws IOException {
        log.debug("Client-Response-Status: {}", response.getStatusCode());

        var responseHeaders = properties.includeResponseHeaders() ? response.getHeaders().toString() : "[Headers not logged]";
        log.debug("Client-Response-Headers: {}", responseHeaders);

        var body = "[Body not logged]";
        if (properties.includeResponseBody()) {
            byte[] responseBody = response.getBody().readAllBytes();
            body = (responseBody.length > 0) ? new String(responseBody, StandardCharsets.UTF_8) : "[No-Body]";
            if (body.length() > properties.maxBodyLength()) {
                body = body.substring(0, properties.maxBodyLength()) + "...[truncated]";
            }
        }
        log.debug("Client-Response-Body: {}", body);
    }
}
