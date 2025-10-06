package com.webapp;

import com.webapp.config.AppFilterProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import com.webapp.config.ClientLoggingProperties;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import java.util.Arrays;

@EnableConfigurationProperties(value = {ClientLoggingProperties.class, AppFilterProperties.class})
@SpringBootApplication
public class Webapp
{
	private static final Logger log = LoggerFactory.getLogger(Webapp.class);

	public static void main(String... args)
	{
		var availableThreads = Integer.toString(Runtime.getRuntime().availableProcessors());
		System.setProperty("server.tomcat.threads.max", availableThreads);
		System.setProperty("jdk.virtualThreadScheduler.maxPoolSize", availableThreads);
        log.info("Starting webapp with server-tomcat-threads-max and max-pool-size as:[{}]", availableThreads);

		SpringApplication.run(Webapp.class, args);

		log.info("Completed executing 'main' method");
		Runtime.getRuntime().addShutdownHook(new Thread(() -> log.info("Shutting down webapp !!")));
	}

    @Bean
    public CommandLineRunner commandLineRunner(ServerProperties serverProperties) {
        return args -> {
            log.info("Starting app with arguments: {}", Arrays.asList(args));
            log.info("Tomcat Max Connections Accepted:[{}]", serverProperties.getTomcat().getMaxConnections());
            log.info("Tomcat Max Worker Threads Queue Size:[{}]", serverProperties.getTomcat().getThreads().getMaxQueueCapacity());
            log.info("Tomcat Max Worker Threads:[{}]", serverProperties.getTomcat().getThreads().getMax());
            log.info("Tomcat Min Worker Threads:[{}]", serverProperties.getTomcat().getThreads().getMinSpare());
        };
    }
}
