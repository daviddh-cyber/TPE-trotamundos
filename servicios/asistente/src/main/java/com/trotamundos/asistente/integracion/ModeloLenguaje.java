package com.trotamundos.asistente.integracion;

import java.util.List;
import java.util.Map;

/**
 * Único punto de contacto con el modelo de lenguaje (ADR 016; Iteración 6, sección 5.2).
 * Implementaciones: AdaptadorBedrock (producción, fuera del prototipo) y MotorReglas.
 */
public interface ModeloLenguaje {

    /** Interpreta el último mensaje y propone qué herramientas usar (modelo liviano). */
    PropuestaHerramientas interpretar(ContextoConversacion contexto, List<DescripcionHerramienta> disponibles);

    /**
     * Redacta la respuesta a partir de los datos que devolvieron las herramientas (modelo principal).
     * En producción se entrega por partes; en el prototipo, completa.
     */
    String redactar(ContextoConversacion contexto, Map<String, Object> datosHerramientas);
}
