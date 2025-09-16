package com.webapp.config;

import io.micrometer.tracing.Tracer;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.Request;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.JettyClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;

import java.net.URI;

import static com.webapp.config.AppWebClientInterceptor.httpClientInterceptor;

@Configuration
public class AppWebClient {

    private final Tracer tracer;
    private final RestClientLoggingProperties properties;

    public AppWebClient(Tracer tracer, RestClientLoggingProperties properties) {
        this.tracer = tracer;
        this.properties = properties;
    }

    @Bean
    public WebClient webClient(WebClient.Builder builder) {
        return builder
                .clientConnector(new JettyClientHttpConnector(getJettyHttpClient()))
                .build();
    }

    /**
     * Creates a custom Jetty {@link HttpClient} that intercepts HTTP requests
     * to apply tracing using the provided {@link Tracer}.
     *
     * @return a configured {@link HttpClient} instance with request interception
     *
     * @apiNote This method overrides the default request creation to include
     * tracing information in each request using the {@link AppWebClientInterceptor}.
     */
    private HttpClient getJettyHttpClient() {
        return new HttpClient() {
            @Override
            public Request newRequest(URI uri) {
                Request request = super.newRequest(uri);
                return httpClientInterceptor(request, tracer, properties);
            }
        };
    }
}
