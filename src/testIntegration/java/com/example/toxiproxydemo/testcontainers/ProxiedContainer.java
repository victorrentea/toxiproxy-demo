package com.example.toxiproxydemo.testcontainers;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.ToxiproxyContainer;

import java.util.function.Consumer;


public record ProxiedContainer(GenericContainer<?> container, Consumer<ToxiproxyContainer.ContainerProxy> proxySetup) {

    public int getPort() {
        return container.getExposedPorts().get(0);
    }
}
