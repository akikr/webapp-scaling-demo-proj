package com.webapp;

import org.springframework.boot.SpringApplication;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.WaitAllStrategy;

public class WebappRunAndTest
{
	public static void main(String[] args)
	{
		runWithVirtualThreads(false);
	}

	public static void runWithVirtualThreads(boolean virtualThreads)
	{
		var severPort = 8080;
		var httpbinServer = new GenericContainer<>("mccutchen/go-httpbin:latest")
				.withExposedPorts(severPort)
		       .waitingFor(new WaitAllStrategy());
		httpbinServer.start();

		System.setProperty("http-bin.server.url", "http://" + httpbinServer.getHost() + ":" + httpbinServer.getMappedPort(severPort));

		var availableCPUThreads = Integer.toString(Runtime.getRuntime().availableProcessors());
		System.setProperty("server.tomcat.threads.max", availableCPUThreads);
		System.setProperty("jdk.virtualThreadScheduler.maxPoolSize", availableCPUThreads);
		System.setProperty("spring.threads.virtual.enabled", Boolean.toString(virtualThreads));

		SpringApplication.run(Webapp.class);
	}
}
