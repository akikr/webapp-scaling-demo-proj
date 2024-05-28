package com.webapp;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestClient;

@SpringBootApplication
public class Webapp
{

	public static void main(String[] args)
	{
		var availableThreads = Integer.toString(Runtime.getRuntime().availableProcessors());
		System.setProperty("server.tomcat.threads.max", availableThreads);
		System.setProperty("jdk.virtualThreadScheduler.maxPoolSize", availableThreads);
		SpringApplication.run(Webapp.class, args);
	}

	@Bean
	public RestClient restClient(RestClient.Builder builder, @Value("${http-bin.server.url}") String serverUrl)
	{
		return builder.baseUrl(serverUrl).build();
	}
}
