package com.example.toxiproxydemo.testcontainers;


import com.google.common.io.Resources;
import io.vavr.control.Try;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;

import java.nio.charset.StandardCharsets;
import java.sql.DriverManager;
import java.util.List;

public class PostgresTestContainer extends GenericContainer<PostgresTestContainer> {


    private static final Logger LOGGER = LoggerFactory.getLogger(PostgresTestContainer.class);

    private static final String USERNAME = "postgres";
    private static final String PASSWORD = "password";

    public static final int MAPPED_PORT = 5432;
    private static final String IMAGE_VERSION =
            "postgres@sha256:2b87b5bb55589540f598df6ec5855e5c15dd13628230a689d46492c1d433c4df";

    private static PostgresTestContainer container;

    private PostgresTestContainer() {
        super(IMAGE_VERSION);
    }

    public static PostgresTestContainer getInstance() {
        if (container == null) {
            container = new PostgresTestContainer();
            container.setExposedPorts(List.of(MAPPED_PORT));
            container.addEnv("POSTGRES_PASSWORD", PASSWORD);
            container.addEnv("POSTGRES_USER", USERNAME);
        }
        return container;
    }

    @Override
    public void start() {
        super.start();
        final var jdbcUrl = String.format(
                "jdbc:postgresql://%s:%s/postgres", container.getHost(), container.getMappedPort(MAPPED_PORT));
        System.setProperty(
                "spring.datasource.url",jdbcUrl
                );
        LOGGER.info("PostgreSQL started on {}:{}", container.getHost(), container.getMappedPort(MAPPED_PORT));
        initializeTables(jdbcUrl);
    }

    private void initializeTables(final String jdbcUrl) {
        LOGGER.info("Initializing tables");
        Try.run(() -> {
                    final var connection = DriverManager.getConnection(jdbcUrl, USERNAME, PASSWORD);
                    connection
                            .createStatement()
                            .executeUpdate(
                                    Resources.toString(Resources.getResource("init.sql"), StandardCharsets.UTF_8));
                })
                .get();
    }

    @Override
    public void stop() {
        // do nothing, JVM handles shut down
    }
}
