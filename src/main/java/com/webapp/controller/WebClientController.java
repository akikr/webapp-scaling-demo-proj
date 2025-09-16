package com.webapp.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.Map;
import java.util.Objects;

@Controller
@ResponseBody
@RequestMapping(path = "/v2")
public class WebClientController
{
    private static final Logger log = LoggerFactory.getLogger(WebClientController.class);

    @Value("${http-bin.server.url}")
    private String serverUrl;

    private final WebClient webClient;

    public WebClientController(WebClient webClient) {
        this.webClient = webClient;
    }

    @GetMapping
    public ResponseEntity<?> getMessage()
    {
        return ResponseEntity.ok()
                .body(Map.of("message", "Hello! Welcome to this Webapp"));
    }

    @GetMapping(path = "/get")
    public ResponseEntity<?> httpGetEndpoint()
    {
        log.info("Received request on [/v2/get] on [{}]", Thread.currentThread());
        try
        {
            ResponseEntity<String> responseEntity = webClient.get()
                    .uri(serverUrl + "/get")
                    .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, ClientResponse::createException)
                    .toEntity(String.class)
                    .block();

            log.info("http-bin server responded: {} on [{}]", Objects.nonNull(responseEntity)
                    ? responseEntity.getStatusCode() : "null response", Thread.currentThread());

            if (Objects.nonNull(responseEntity) && responseEntity.getStatusCode().is2xxSuccessful())
                return ResponseEntity.ok()
                        .body(Map.of("message", "success"));
            else
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("message", "failed"));
        }
        catch (WebClientResponseException we)
        {
            return ResponseEntity.status(we.getStatusCode())
                    .body(Map.of("message", we.getMessage()));
        }
        catch (Exception e)
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping(path = "/post")
    public ResponseEntity<?> httpPostEndpoint()
    {
        log.info("Received request on [/v2/post] on [{}]", Thread.currentThread());
        try
        {
            ResponseEntity<String> responseEntity = webClient.post()
                    .uri(serverUrl + "/post")
                    .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                    .bodyValue(Map.of("sample", "data"))
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, ClientResponse::createException)
                    .toEntity(String.class)
                    .block();

            log.info("http-bin server responded: {} on [{}]", Objects.nonNull(responseEntity)
                    ? responseEntity.getStatusCode() : "null response", Thread.currentThread());

            if (Objects.nonNull(responseEntity) && responseEntity.getStatusCode().is2xxSuccessful())
                return ResponseEntity.ok()
                        .body(Map.of("message", "success"));
            else
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("message", "failed"));
        }
        catch (WebClientResponseException we)
        {
            return ResponseEntity.status(we.getStatusCode())
                    .body(Map.of("message", we.getMessage()));
        }
        catch (Exception e)
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping(path = "/delay/{seconds}")
    public ResponseEntity<?> httpDelayEndpoint(@PathVariable(name = "seconds") String seconds)
    {
        log.info("Received request on [/v2/delay/{}] on [{}]", seconds, Thread.currentThread());
        try
        {
            ResponseEntity<String> responseEntity = webClient.get()
                    .uri(serverUrl + "/delay/" + seconds)
                    .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, ClientResponse::createException)
                    .toEntity(String.class)
                    .block();

            log.info("http-bin server responded: {} on [{}]", Objects.nonNull(responseEntity)
                    ? responseEntity.getStatusCode() : "null response", Thread.currentThread());

            if (Objects.nonNull(responseEntity) && responseEntity.getStatusCode().is2xxSuccessful())
                return ResponseEntity.ok()
                        .body(Map.of("message", "success"));
            else
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("message", "failed"));
        }
        catch (WebClientResponseException we)
        {
            return ResponseEntity.status(we.getStatusCode())
                    .body(Map.of("message", we.getMessage()));
        }
        catch (Exception e)
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping(path = "/status/{statusCode}")
    public ResponseEntity<?> httpStatusEndpoint(@PathVariable(name = "statusCode") String statusCode)
    {
        log.info("Received request on [/v2/status/{}] on [{}]", statusCode, Thread.currentThread());
        try
        {
            ResponseEntity<String> responseEntity = webClient.get()
                    .uri(serverUrl + "/status/" + statusCode)
                    .header(HttpHeaders.ACCEPT, MediaType.TEXT_PLAIN_VALUE)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, ClientResponse::createException)
                    .toEntity(String.class)
                    .block();

            log.info("http-bin server responded: {} on [{}]", Objects.nonNull(responseEntity)
                    ? responseEntity.getStatusCode() : "null response", Thread.currentThread());

            if (Objects.nonNull(responseEntity) && responseEntity.getStatusCode().is2xxSuccessful())
                return ResponseEntity.ok()
                        .body(Map.of("message", "success"));
            else
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("message", "failed"));
        }
        catch (WebClientResponseException we)
        {
            return ResponseEntity.status(we.getStatusCode())
                    .body(Map.of("message", we.getMessage()));
        }
        catch (Exception e)
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping(path = "/response-headers")
    public ResponseEntity<?> httpResponseHeadersEndpoint(@RequestParam(name = "data") String data)
    {
        log.info("Received request on [/v2/response-headers?data={}] on [{}]", data, Thread.currentThread());
        try
        {
            ResponseEntity<String> responseEntity = webClient.get()
                    .uri(serverUrl + "/response-headers", uriBuilder -> uriBuilder
                            .queryParam("freeform", data)
                            .build())
                    .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, ClientResponse::createException)
                    .toEntity(String.class)
                    .block();

            log.info("http-bin server responded: {} on [{}]", Objects.nonNull(responseEntity)
                    ? responseEntity.getStatusCode() : "null response", Thread.currentThread());

            if (Objects.nonNull(responseEntity) && responseEntity.getStatusCode().is2xxSuccessful())
                return ResponseEntity.ok()
                        .body(Map.of("message", "success"));
            else
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("message", "failed"));
        }
        catch (WebClientResponseException we)
        {
            return ResponseEntity.status(we.getStatusCode())
                    .body(Map.of("message", we.getMessage()));
        }
        catch (Exception e)
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", e.getMessage()));
        }
    }
}
