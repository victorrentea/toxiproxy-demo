package com.example.toxiproxydemo.steps;

import com.example.toxiproxydemo.testcontainers.TestContainerManager;
import eu.rekawek.toxiproxy.model.ToxicDirection;
import io.cucumber.java.en.And;
import io.cucumber.java.en.When;

import java.io.IOException;

public class ToxiProxySteps {

    private final TestContainerManager testContainerManager = TestContainerManager.getInstance();
    @And("connection to db fails")
    public void connectionToDbFails() throws IOException {
        final var toxics = testContainerManager.getProxy("postgres").get().toxics();
        toxics.limitData(String.format("%s-limit", "postgres"), ToxicDirection.DOWNSTREAM, 0);
        toxics.bandwidth(String.format("%s-bandwidth", "postgres"), ToxicDirection.DOWNSTREAM, 0);
        toxics.timeout(String.format("%s-timeout", "postgres"), ToxicDirection.UPSTREAM, 10000);
    }

    @When("the db connection is fixed")
    public void theDbConnectionIsFixed() {
        testContainerManager.clearAllToxics();
    }

}
