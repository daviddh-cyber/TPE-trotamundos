package com.trotamundos.viajes.servicio;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;

/** Prueba la regla de agrupación de reservas en viajes (RF-13, supuesto S-14). */
class AgrupacionServiceTest {

    private static final LocalDate INICIO_VIAJE = LocalDate.of(2026, 11, 10);
    private static final LocalDate FIN_VIAJE = LocalDate.of(2026, 11, 15);

    @Test
    void reservaDentroDelViajeCorresponde() {
        assertThat(AgrupacionService.corresponde(INICIO_VIAJE, FIN_VIAJE,
                LocalDate.of(2026, 11, 12), LocalDate.of(2026, 11, 13))).isTrue();
    }

    @Test
    void reservaPegadaAlFinalDentroDelMargenCorresponde() {
        assertThat(AgrupacionService.corresponde(INICIO_VIAJE, FIN_VIAJE,
                LocalDate.of(2026, 11, 17), LocalDate.of(2026, 11, 18))).isTrue();
    }

    @Test
    void reservaLejanaNoCorresponde() {
        assertThat(AgrupacionService.corresponde(INICIO_VIAJE, FIN_VIAJE,
                LocalDate.of(2026, 12, 20), LocalDate.of(2026, 12, 22))).isFalse();
    }
}
