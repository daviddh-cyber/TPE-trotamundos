package com.trotamundos.proveedores;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * Servicio Proveedores: integración con proveedores de viaje mediante adaptadores.
 * Diseño: Wiki, Iteración 1 (sección 5.1) e Iteración 2 (sección 5.2, ADR 007).
 */
@SpringBootApplication
@ConfigurationPropertiesScan
public class ProveedoresApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProveedoresApplication.class, args);
    }
}
