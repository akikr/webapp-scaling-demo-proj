package com.webapp;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class WebappTests
{
    @Autowired
    private ApplicationContext applicationContext;

	@Test
	void contextLoads()
	{
        // Verify Spring application context is not null
        assertThat(applicationContext).isNotNull();

        // Verify the main application class is loaded
        assertThat(applicationContext.getBean("webapp")).isInstanceOf(Webapp.class);
	}
}
