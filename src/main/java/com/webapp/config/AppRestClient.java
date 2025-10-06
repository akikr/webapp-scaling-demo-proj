package com.webapp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.RestClient;

import java.io.IOException;

import static com.webapp.config.RestClientInterceptor.logRequest;
import static com.webapp.config.RestClientInterceptor.logResponse;

@Configuration
public class AppRestClient {

    private final ClientLoggingProperties properties;

    public AppRestClient(ClientLoggingProperties properties) {
        this.properties = properties;
    }

    @Bean
    public RestClient restClient(RestClient.Builder builder) {
        return builder
                .requestInterceptor(this::restClientInterceptor)
                .build();
    }

    /// Intercepts outgoing REST requests to log request and response details
    ///
    /// @param request   the HTTP request to execute
    /// @param body      the body of the request
    /// @param execution the request execution
    ///
    /// @return the HTTP response after execution
    /// @throws IOException if an I/O error occurs
    ///
    /// @apiNote This method uses static logging methods from [RestClientInterceptor]
    private ClientHttpResponse restClientInterceptor(
            HttpRequest request,
            byte[] body,
            ClientHttpRequestExecution execution) throws IOException {
        if (properties.enabled()) {
            logRequest(request, body, properties);
        }
        var response = execution.execute(request, body);
        if (properties.enabled()) {
            logResponse(response, properties);
        }
        return response;
    }
}
