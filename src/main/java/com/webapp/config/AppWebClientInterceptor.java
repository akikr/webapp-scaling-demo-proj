package com.webapp.config;

import io.micrometer.tracing.Tracer;
import org.eclipse.jetty.client.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

public interface AppWebClientInterceptor {

    Logger log = LoggerFactory.getLogger(AppWebClientInterceptor.class);

    static Request httpClientInterceptor(Request request, Tracer tracer, RestClientLoggingProperties properties) {
        if (!properties.enabled()) {
            return request; // No logging if disabled
        }

        var requestData = new StringBuilder();
        Optional.ofNullable(tracer.currentSpan())
                .ifPresent(span -> requestData.append("""
                Id: [%s:%s]
                """.formatted(span.context().traceId(), span.context().spanId())));
        request.onRequestBegin(req -> requestData.append("""
                -----------------------Request Start---------------------
                Request: %s %s
                """.formatted(req.getMethod(), req.getURI())
        ));
        request.onRequestHeaders(req -> {
            var headers = properties.includeRequestHeaders()
                    ? req.getHeaders().stream().parallel().toList()
                    : "[Headers not logged]";
            requestData.append("""
                    Headers: %s
                    """.formatted(headers));
        });
        var requestBody = new StringBuilder();
        request.onRequestContent((req, content) -> {
            var body = "[Body not logged]";
            if (properties.includeRequestBody()) {
                body = getBody(content);
                if (body.length() > properties.maxBodyLength()) {
                    body = body.substring(0, properties.maxBodyLength()) + "...[truncated]";
                }
            }
            requestBody.append(body);
        });

        var responseData = new StringBuilder();
        Optional.ofNullable(tracer.currentSpan())
                .ifPresent(span -> responseData.append("""
                Id: [%s:%s]
                """.formatted(span.context().traceId(), span.context().spanId())));
        request.onResponseBegin(res -> responseData.append("""
                -----------------------Response Start---------------------
                Status: %s
                """.formatted(HttpStatus.valueOf(res.getStatus()))
        ));
        request.onResponseHeaders(res -> {
            var headers = properties.includeResponseHeaders()
                    ? res.getHeaders().stream().parallel().toList()
                    : "[Headers not logged]";
            responseData.append("""
                Headers: %s
                """.formatted(headers));
        });
        var responseBody = new StringBuilder();
        request.onResponseContent((res, content) -> {
            var body = "[Body not logged]";
            if (properties.includeResponseBody()) {
                body = getBody(content);
                if (body.length() > properties.maxBodyLength()) {
                    body = body.substring(0, properties.maxBodyLength()) + "...[truncated]";
                }
            }
            responseBody.append(body);
        });

        //Actual logging of request data
        request.onRequestSuccess(req -> {
            var body = StringUtils.hasText(requestBody) ? requestBody : "[No-Body]";
            requestData.append("""
                    Body: %s
                    -----------------------Request End-----------------------"""
                    .formatted(body));
            log.debug("{}", requestData);
        });

        //Actual logging of response data
        request.onResponseSuccess(res -> {
            var body = StringUtils.hasText(responseBody) ? responseBody : "[No-Body]";
            responseData.append("""
                    Body: %s
                    -----------------------Response End-----------------------"""
                    .formatted(body));
            log.debug("{}", responseData);
        });

        return request;
    }

    private static String getBody(ByteBuffer content) {
        //Always decode the content into CharBuffer, otherwise reading response-body will throw UnsupportedOperationException
        CharBuffer charBuffer = StandardCharsets.UTF_8.decode(content);
        return charBuffer.toString();
    }
}
