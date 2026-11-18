package com.trotamundos.asistente.herramientas;

import java.util.Map;

/**
 * Resultado de una herramienta.
 * @param datos datos para que el modelo redacte la respuesta (pasan por el filtro de campos permitidos)
 * @param mensajeParaUsuario texto fijo para el usuario, en las acciones confirmadas
 */
public record ResultadoHerramienta(Map<String, Object> datos, String mensajeParaUsuario) {

    public static ResultadoHerramienta conDatos(Map<String, Object> datos) {
        return new ResultadoHerramienta(datos, null);
    }

    public static ResultadoHerramienta conMensaje(String mensaje) {
        return new ResultadoHerramienta(Map.of(), mensaje);
    }
}
