package com.webapp.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Controller
@ResponseBody
@RequestMapping(path = "/")
public class WebappController
{
	private static final Logger log = LoggerFactory.getLogger(WebappController.class);

	@Value("${http-bin.server.url}")
	private String serverUrl;

	private final RestClient restClient;

	public WebappController(RestClient restClient)
	{
		this.restClient = restClient;
	}

	@GetMapping
	public ResponseEntity<?> getMessage()
	{
		return ResponseEntity.ok()
				.body(Map.of("message", "Hello! Welcome to this Webapp"));
	}

	@GetMapping(path = "/delay/{seconds}")
	public ResponseEntity<?> httpDelayEndpoint(@PathVariable(name = "seconds") String seconds)
	{
		try
		{
			var requestToHttBin = restClient.get()
					.uri(serverUrl + "/delay/" + seconds)
                    .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
					.retrieve()
					.toEntity(String.class);

			log.info("http-bin server responded: {} on [{}]", requestToHttBin.getStatusCode(), Thread.currentThread());

			if (requestToHttBin.getStatusCode().is2xxSuccessful())
				return ResponseEntity.ok()
						.body(Map.of("message", "success"));
			else
				return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
						.body(Map.of("message", "failed"));
		}
		catch (Exception e)
		{
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body(Map.of("message", e.getMessage()));
		}
	}

    @GetMapping(path = "/get")
    public ResponseEntity<?> httpGetEndpoint()
    {
        try
        {
            var requestToHttBin = restClient.get()
                    .uri(serverUrl + "/get")
                    .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                    .retrieve()
                    .toEntity(String.class);

            log.info("http-bin server responded: {} on [{}]", requestToHttBin.getStatusCode(), Thread.currentThread());

            if (requestToHttBin.getStatusCode().is2xxSuccessful())
                return ResponseEntity.ok()
                        .body(Map.of("message", "success"));
            else
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("message", "failed"));
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
        try
        {
            var requestToHttBin = restClient.get()
                    .uri(serverUrl + "/status/" + statusCode)
                    .header(HttpHeaders.ACCEPT, MediaType.TEXT_PLAIN_VALUE)
                    .retrieve()
                    .toEntity(String.class);

            log.info("http-bin server responded: {} on [{}]", requestToHttBin.getStatusCode(), Thread.currentThread());

            if (requestToHttBin.getStatusCode().is2xxSuccessful())
                return ResponseEntity.ok()
                        .body(Map.of("message", "success"));
            else
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("message", "failed"));
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
        try
        {
            var requestToHttBin = restClient.get()
                    .uri(serverUrl + "/response-headers", uriBuilder -> uriBuilder
                            .queryParam("freeform", data)
                            .build()
                    )
                    .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                    .retrieve()
                    .toEntity(String.class);

            log.info("http-bin server responded: {} on [{}]", requestToHttBin.getStatusCode(), Thread.currentThread());

            if (requestToHttBin.getStatusCode().is2xxSuccessful())
                return ResponseEntity.ok()
                        .body(Map.of("message", "success"));
            else
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("message", "failed"));
        }
        catch (Exception e)
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", e.getMessage()));
        }
    }
}
