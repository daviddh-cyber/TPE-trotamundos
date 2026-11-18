package com.trotamundos.asistente.integracion;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

/** Prueba el filtro de lo que se envía al modelo de lenguaje (SE-6; ADR 019, barreras 1 y 4). */
class FiltroDatosSensiblesTest {

    private final FiltroDatosSensibles filtro = new FiltroDatosSensibles();

    @Test
    void ocultaCorreosElectronicos() {
        assertThat(filtro.filtrar("Mi correo es juan.perez@gmail.com, avisame ahí"))
                .isEqualTo("Mi correo es [DATO OCULTO], avisame ahí");
    }

    @Test
    void ocultaTokens() {
        assertThat(filtro.filtrar("token eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIn0.abc123"))
                .doesNotContain("eyJ");
        assertThat(filtro.filtrar("Authorization: Bearer abc.def.ghi"))
                .doesNotContain("abc.def.ghi");
    }

    @Test
    void ocultaNumerosDeTarjeta() {
        assertThat(filtro.filtrar("Pagué con la 4111 1111 1111 1111"))
                .isEqualTo("Pagué con la [DATO OCULTO]");
    }

    @Test
    void noOcultaFechasNiHorarios() {
        assertThat(filtro.filtrar("Mi vuelo sale el 2026-11-10 a las 09:30"))
                .isEqualTo("Mi vuelo sale el 2026-11-10 a las 09:30");
    }

    @Test
    void dejaSoloLosCamposPermitidosEnCualquierNivel() {
        Map<String, Object> datos = Map.of("viajes", List.of(Map.of(
                "destino", "Bariloche",
                "reservas", List.of(Map.of("estado", "CONFIRMADA", "codigo", "ABC123",
                        "detalles", Map.of("pasajero", "Juan Pérez"))))));

        Map<String, Object> filtrados = filtro.soloCamposPermitidos(datos,
                Set.of("viajes", "destino", "reservas", "estado"));

        assertThat(filtrados.toString())
                .contains("Bariloche", "CONFIRMADA")
                .doesNotContain("ABC123", "Juan Pérez");
    }
}
