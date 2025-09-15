package com.webapp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import com.webapp.config.RestClientLoggingProperties;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import java.util.Arrays;

@EnableConfigurationProperties(RestClientLoggingProperties.class)

@SpringBootApplication
public class Webapp
{
	private static final Logger log = LoggerFactory.getLogger(Webapp.class);

	public static void main(String... args)
	{
		log.info("Started executing 'main' method with arguments: {}", Arrays.asList(args));

		var availableThreads = Integer.toString(Runtime.getRuntime().availableProcessors());
		System.setProperty("server.tomcat.threads.max", availableThreads);
		System.setProperty("jdk.virtualThreadScheduler.maxPoolSize", availableThreads);

		SpringApplication.run(Webapp.class, args);

		log.info("Completed executing 'main' method");
		Runtime.getRuntime().addShutdownHook(new Thread(() -> log.info("Shutting down webapp !!")));
	}
}
