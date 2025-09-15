package com.webapp.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static java.util.Objects.nonNull;

public interface RestClientInterceptor {

    Logger log = LoggerFactory.getLogger(RestClientInterceptor.class);

    static void logRequest(HttpRequest request, byte[] requestBody, RestClientLoggingProperties properties) {
        var requestHeaders = properties.includeRequestHeaders() ? request.getHeaders().toString() : "[Headers not logged]";
        var body = "[Body not logged]";
        if (properties.includeRequestBody()) {
            body = (nonNull(requestBody) && requestBody.length > 0)
                    ? new String(requestBody, StandardCharsets.UTF_8) : "[No-Body]";
            if (body.length() > properties.maxBodyLength()) {
                body = body.substring(0, properties.maxBodyLength()) + "...[truncated]";
            }
        }

        var requestData = """
                -----------------------Request Start---------------------
                Request: %s %s
                Headers: %s
                Body: %s
                -----------------------Request End------------------------
                """.formatted(request.getMethod(), request.getURI(), requestHeaders, body);
        log.debug("{}", requestData);
    }

    static void logResponse(ClientHttpResponse response, RestClientLoggingProperties properties) throws IOException {
        var responseHeaders = properties.includeResponseHeaders() ? response.getHeaders().toString() : "[Headers not logged]";
        var body = "[Body not logged]";
        if (properties.includeResponseBody()) {
            byte[] responseBody = response.getBody().readAllBytes();
            body = (responseBody.length > 0) ? new String(responseBody, StandardCharsets.UTF_8) : "[No-Body]";
            if (body.length() > properties.maxBodyLength()) {
                body = body.substring(0, properties.maxBodyLength()) + "...[truncated]";
            }
        }

        var responseData = """
                -----------------------Response Start---------------------
                Status: %s
                Headers: %s
                Body: %s
                -----------------------Response End-----------------------
                """.formatted(response.getStatusCode(), responseHeaders, body);
        log.debug("{}", responseData);
    }
}
