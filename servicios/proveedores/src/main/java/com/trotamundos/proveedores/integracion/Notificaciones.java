package com.trotamundos.proveedores.integracion;

import java.util.Map;

/** Utilidades comunes para leer las notificaciones de los proveedores. */
final class Notificaciones {

    private Notificaciones() {
    }

    static String requerido(Map<String, Object> notificacion, String campo) {
        Object valor = notificacion.get(campo);
        if (valor == null) {
            throw new IllegalArgumentException("Falta el campo '" + campo + "' en la notificación");
        }
        return String.valueOf(valor);
    }

    /** Copia un campo de la notificación al mapa de cambios, con el nombre del modelo común. */
    static void copiar(Map<String, Object> notificacion, String campoProveedor,
                       Map<String, Object> cambios, String campoComun) {
        Object valor = notificacion.get(campoProveedor);
        if (valor != null) {
            cambios.put(campoComun, valor);
        }
    }
}
