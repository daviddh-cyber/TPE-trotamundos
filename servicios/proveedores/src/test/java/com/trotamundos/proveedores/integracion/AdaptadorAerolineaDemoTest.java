package com.trotamundos.proveedores.integracion;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Map;
import org.junit.jupiter.api.Test;

/** Prueba que el adaptador traduzca el formato de la aerolínea al modelo común (ADR 007). */
class AdaptadorAerolineaDemoTest {

    private final AdaptadorAerolineaDemo adaptador = new AdaptadorAerolineaDemo();

    @Test
    void traduceUnCambioDeHorario() {
        CambioReserva cambio = adaptador.traducir(Map.of(
                "pnr", "ABC123",
                "evento", "SCHEDULE_CHANGE",
                "nuevaSalida", "2026-11-10T09:30:00Z",
                "puerta", "B12"));

        assertThat(cambio.codigo()).isEqualTo("ABC123");
        assertThat(cambio.tipo()).isEqualTo(CambioReserva.Tipo.ACTUALIZACION);
        assertThat(cambio.cambios())
                .containsEntry("fechaInicio", "2026-11-10T09:30:00Z")
                .containsEntry("puerta", "B12");
    }

    @Test
    void traduceUnaCancelacion() {
        CambioReserva cambio = adaptador.traducir(Map.of(
                "pnr", "ABC123", "evento", "CANCELLED", "motivo", "Tormenta"));

        assertThat(cambio.tipo()).isEqualTo(CambioReserva.Tipo.CANCELACION);
        assertThat(cambio.motivo()).isEqualTo("Tormenta");
    }

    @Test
    void rechazaUnaNotificacionSinCodigo() {
        assertThatThrownBy(() -> adaptador.traducir(Map.of("evento", "CANCELLED")))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
