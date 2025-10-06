package com.webapp.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.util.Collection;
import java.util.Collections;
import java.util.function.Function;
import java.util.stream.Collectors;

public interface AppFilterLogger {

    Logger log = LoggerFactory.getLogger(AppFilterLogger.class);

    static void logRequest(ContentCachingRequestWrapper request, AppFilterProperties appFilterProperties) {
        log.debug("REQUEST: {} {}", request.getMethod(), request.getRequestURI());

        var headers = "[Headers not logged]";
        if (appFilterProperties.includeRequestHeaders()) {
            headers = getHeaders(Collections.list(request.getHeaderNames()), request::getHeader);
        }
        log.debug("REQUEST HEADERS: {}", headers);

        var body = "[Not Logged]";
        if (appFilterProperties.includeRequestBody()) {
            body = new String(request.getContentAsByteArray());
            if (body.length() > appFilterProperties.maxBodyLength()) {
                body = body.substring(0, appFilterProperties.maxBodyLength()) + "...[truncated]";
            }
        }
        log.debug("REQUEST BODY: {}", (body.isEmpty()) ? "[No-Body]" : body);
    }

    static void logResponse(ContentCachingResponseWrapper response, AppFilterProperties appFilterProperties) {
        log.debug("RESPONSE STATUS: {}", response.getStatus());

        var headers = "[Headers not logged]";
        if (appFilterProperties.includeResponseHeaders()) {
            headers = getHeaders(response.getHeaderNames(), response::getHeader);
        }
        log.debug("RESPONSE HEADERS: {}", headers);

        var body = "[Not Logged]";
        if (appFilterProperties.includeResponseBody()) {
            body = new String(response.getContentAsByteArray());
            if (body.length() > appFilterProperties.maxBodyLength()) {
                body = body.substring(0, appFilterProperties.maxBodyLength()) + "...[truncated]";
            }
        }
        log.debug("RESPONSE BODY: {}", (body.isEmpty()) ? "[No-Body]" : body);
    }

    private static String getHeaders(Collection<String> headerNames, Function<String, String> getHeader) {
        return headerNames.stream()
                .filter(StringUtils::hasText)
                .collect(Collectors.toMap(Function.identity(), getHeader))
                .toString();
    }
}
