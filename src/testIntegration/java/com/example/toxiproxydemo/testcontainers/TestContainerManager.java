package com.example.toxiproxydemo.testcontainers;

import io.vavr.control.Try;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.ToxiproxyContainer;
import org.testcontainers.lifecycle.TestLifecycleAware;
import org.testcontainers.utility.DockerImageName;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

public class TestContainerManager implements TestLifecycleAware {
    private static final Logger LOGGER = LoggerFactory.getLogger(TestContainerManager.class);
    private static final String TOXIPROXY_NETWORK_ALIAS = "toxiproxy";
    private static TestContainerManager container;

    public static TestContainerManager getInstance() {
        if (container == null) {
            container = new TestContainerManager();
        }
        return container;
    }

    private final Map<String, ProxiedContainer> containers = Map.of(
                    "postgres",
                    new ProxiedContainer(PostgresTestContainer.getInstance(),
                            proxy -> {
                                System.setProperty(
                                        "spring.datasource.url",
                                        String.format(
                                                "jdbc:postgresql://%s:%s/postgres",
                                                proxy.getContainerIpAddress(), proxy.getProxyPort()));
                                LOGGER.info(
                                        "PostgreSQL started on {}:{}",
                                        proxy.getContainerIpAddress(),
                                        proxy.getProxyPort());
                            })

           );

    private final Network network = Network.newNetwork();
    private final ToxiproxyContainer toxiproxyContainer = new ToxiproxyContainer(DockerImageName.parse(
                            "shopify/toxiproxy@sha256:a6b080af39986b863a1f7c5a3b9bacf2afeb48abab8f0eb7e243f8f7ad38c645")
                    .asCompatibleSubstituteFor("shopify/toxiproxy"))
            .withNetwork(network)
            .withNetworkAliases(TOXIPROXY_NETWORK_ALIAS);

    private TestContainerManager() {
        containers().forEach(container -> container.withNetwork(network));
    }

    public Optional<ToxiproxyContainer.ContainerProxy> getProxy(final String containerName) {
        return Optional.ofNullable(containers.get(containerName))
                .map(container -> toxiproxyContainer.getProxy(container.container(), container.getPort()));
    }

    private Stream<GenericContainer<?>> containers() {
        return containers.values().stream().map(ProxiedContainer::container);
    }

    public void start() {
        toxiproxyContainer.start();
        containers.forEach((name, container) -> {
            final GenericContainer<?> testContainer = container.container();
            testContainer.start();
            container.proxySetup().accept(toxiproxyContainer.getProxy(testContainer, container.getPort()));
        });
    }

    public void clearAllToxics() {
        containers.keySet().stream().map(this::clearAllToxicsForContainer).forEach(Try::get);
    }

    public Try<Void> clearAllToxicsForContainer(final String containerName) {
        return Try.of(() -> getProxy(containerName).get().toxics().getAll()).flatMap(toxics -> toxics.stream()
                .map(toxic -> Try.run(toxic::remove))
                .reduce(Try.success(null), (acc, t) -> acc.flatMap(u -> t)));
    }
}
