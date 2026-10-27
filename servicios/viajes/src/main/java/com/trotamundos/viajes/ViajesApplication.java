package com.trotamundos.viajes;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Servicio Viajes: fuente de verdad de reservas y viajes.
 * Diseño: Wiki, Iteración 1 (sección 5.1) e Iteración 2 (sección 5.2).
 */
@SpringBootApplication
@EnableScheduling
public class ViajesApplication {

    public static void main(String[] args) {
        SpringApplication.run(ViajesApplication.class, args);
    }
}
