package com.trotamundos.asistente.herramientas;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Una capacidad del asistente (Iteración 6, sección 5.2; versión simplificada para el prototipo).
 * Cada herramienta declara sus reglas y el orquestador las aplica igual para todas:
 * sumar una herramienta no toca el núcleo (EV-5) y hereda la misma protección (ADR 019).
 */
public interface Herramienta {

    int LARGO_MAXIMO_PARAMETRO = 500;

    String nombre();

    /** Para qué sirve: es lo que ve el modelo para decidir si la propone. */
    String descripcion();

    /** Parámetros que acepta. Nunca incluye al usuario: lo completa el orquestador desde el token. */
    Set<String> parametrosPermitidos();

    /** Campos de su resultado que pueden enviarse al modelo de lenguaje (SE-6). */
    Set<String> camposPermitidos();

    /** Si modifica algo, no se ejecuta sin la confirmación explícita del usuario (RF-28, SE-5). */
    boolean requiereConfirmacion();

    /** Texto de la confirmación, armado por el código y no por el modelo. */
    default String describirAccion(Map<String, Object> parametros) {
        return "Voy a ejecutar " + nombre() + ".";
    }

    ResultadoHerramienta ejecutar(Map<String, Object> parametros, ContextoEjecucion contexto);

    /** Valida lo que propone el modelo antes de ejecutar nada (ADR 019, barrera 6). */
    default List<String> validar(Map<String, Object> parametros) {
        List<String> errores = new ArrayList<>();
        parametros.forEach((nombreParametro, valor) -> {
            if (!parametrosPermitidos().contains(nombreParametro)) {
                errores.add("parámetro no permitido en " + nombre() + ": " + nombreParametro);
            } else if (!(valor instanceof String texto) || texto.length() > LARGO_MAXIMO_PARAMETRO) {
                errores.add("valor inválido en " + nombre() + ": " + nombreParametro);
            }
        });
        return errores;
    }
}
