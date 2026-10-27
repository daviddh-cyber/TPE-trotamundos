package com.trotamundos.proveedores.config;

import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** Claves de los webhooks por proveedor (application.yml: trotamundos.webhooks.claves). */
@ConfigurationProperties(prefix = "trotamundos.webhooks")
public record ClavesProveedoresProperties(Map<String, String> claves) {

    public ClavesProveedoresProperties {
        claves = claves == null ? Map.of() : Map.copyOf(claves);
    }
}
