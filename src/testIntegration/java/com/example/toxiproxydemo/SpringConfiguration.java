package com.example.toxiproxydemo;


import com.example.toxiproxydemo.testcontainers.TestContainerManager;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;

import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;



@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@CucumberContextConfiguration
@ActiveProfiles("test")
@Testcontainers
@ContextConfiguration(initializers = SpringConfiguration.Initializer.class)
public class SpringConfiguration {
    private static final String BASE_URI = "http://localhost";
    private final int port;

    @Container
    private static final TestContainerManager testContainerManager = TestContainerManager.getInstance();

    public SpringConfiguration(@LocalServerPort final int port) {
        this.port = port;
    }

    static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

        public void initialize(final ConfigurableApplicationContext configurableApplicationContext) {
            System.setProperty("spring.profiles.active", "test");
            // Used for starting test containers
            testContainerManager.start();
        }
    }
}
