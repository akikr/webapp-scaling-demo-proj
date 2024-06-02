package com.webapp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestClient;

import java.util.Arrays;

@SpringBootApplication
public class Webapp
{
	private static final Logger log = LoggerFactory.getLogger(Webapp.class);

	public static void main(String... args)
	{
		log.info("Started executing 'main' method with arguments: {}", Arrays.asList(args));
		try
		{
			var availableThreads = Integer.toString(Runtime.getRuntime().availableProcessors());
			System.setProperty("server.tomcat.threads.max", availableThreads);
			System.setProperty("jdk.virtualThreadScheduler.maxPoolSize", availableThreads);
			SpringApplication.run(Webapp.class, args);
		}
		catch (Exception e)
		{
			log.error("Staring 'main' method failed, due to: {}", e.getMessage());
		}
		finally
		{
			log.info("Completed executing 'main' method");
		}
	}

	@Bean
	public RestClient restClient(RestClient.Builder builder, @Value("${http-bin.server.url}") String serverUrl)
	{
		return builder.baseUrl(serverUrl).build();
	}
}
