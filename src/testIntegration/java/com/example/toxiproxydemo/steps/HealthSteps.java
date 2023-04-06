package com.example.toxiproxydemo.steps;


import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;


public class HealthSteps {

    final int port;
    private final RestTemplate restTemplate = new RestTemplate();

    public HealthSteps(@LocalServerPort int port) {
        this.port = port;
    }

    @Given("the application is healthy")
    public void healthCheckIsHealthy() {
        final var resp = restTemplate.exchange(
                "http://localhost:" + port + "/health",
                HttpMethod.GET,
                new HttpEntity<>(""),
                String.class);
        assertThat(resp.getBody()).contains("\"db\":{\"status\":\"UP\"");
    }

    @Then("the application becomes unhealthy")
    public void healthCheckIsUnHealthy() {
        try {
            restTemplate.exchange(
                    "http://localhost:" + port + "/health",
                    HttpMethod.GET,
                    new HttpEntity<>(""),
                    String.class);
            fail("Healh check should of been down");
        } catch (HttpStatusCodeException ex) {
            assertThat(ex.getResponseBodyAsString()).contains("\"db\":{\"status\":\"DOWN\"");
        }
    }


}
