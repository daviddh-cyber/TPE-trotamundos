package com.trotamundos.asistente.integracion;

import java.util.Map;

/** Una herramienta que el modelo propone usar, con sus parámetros. Es solo una propuesta (ADR 015). */
public record LlamadaHerramienta(String herramienta, Map<String, Object> parametros) {
}
