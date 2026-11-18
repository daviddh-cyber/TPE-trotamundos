package com.trotamundos.asistente.integracion;

import java.util.List;

/** Resultado de interpretar un mensaje: las herramientas que el modelo propone usar. */
public record PropuestaHerramientas(List<LlamadaHerramienta> llamadas) {

    public static PropuestaHerramientas ninguna() {
        return new PropuestaHerramientas(List.of());
    }
}
