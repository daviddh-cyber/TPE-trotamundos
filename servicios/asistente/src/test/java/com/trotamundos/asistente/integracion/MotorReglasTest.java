package com.trotamundos.asistente.integracion;

import static org.assertj.core.api.Assertions.assertThat;

import com.trotamundos.asistente.dominio.Autor;
import com.trotamundos.asistente.dominio.Mensaje;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Prueba la clasificación de mensajes del MotorReglas (camino rápido, ADR 017). */
class MotorReglasTest {

    private static final List<DescripcionHerramienta> HERRAMIENTAS = List.of(
            new DescripcionHerramienta("consultarViajes", ""),
            new DescripcionHerramienta("derivarCaso", ""));

    private final MotorReglas motor = new MotorReglas();

    @Test
    void reconoceConsultasDeEstado() {
        assertThat(motor.esConsultaDeEstado("¿Cómo está mi vuelo?")).isTrue();
        assertThat(motor.esConsultaDeEstado("¿A qué hora sale?")).isTrue();
    }

    @Test
    void unPedidoDeDerivacionNoVaPorElCaminoRapido() {
        assertThat(motor.esConsultaDeEstado("Mi vuelo se canceló, quiero hablar con la agencia")).isFalse();
    }

    @Test
    void proponeDerivarCuandoSePideLaAgencia() {
        PropuestaHerramientas propuesta = motor.interpretar(contexto("Necesito un reembolso"), HERRAMIENTAS);

        assertThat(propuesta.llamadas()).hasSize(1);
        assertThat(propuesta.llamadas().get(0).herramienta()).isEqualTo("derivarCaso");
    }

    @Test
    void soloProponeHerramientasDisponibles() {
        PropuestaHerramientas propuesta = motor.interpretar(contexto("Necesito un reembolso"),
                List.of(new DescripcionHerramienta("consultarViajes", "")));

        assertThat(propuesta.llamadas()).isEmpty();
    }

    private static ContextoConversacion contexto(String texto) {
        return new ContextoConversacion(List.of(new Mensaje(Autor.USUARIO, texto, Instant.now())));
    }
}
