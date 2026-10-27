package com.trotamundos.proveedores.integracion;

import java.util.Map;

/**
 * Patrón Adaptador (ADR 007, táctica "adaptar la interfaz" de Bass, cap. 7).
 * Agregar un proveedor nuevo = agregar una clase que implemente esta interfaz (IN-1).
 */
public interface AdaptadorProveedor {

    /** Identificador del proveedor; coincide con el de la URL del webhook. */
    String proveedor();

    /** Traduce una notificación en el formato del proveedor al modelo común. */
    CambioReserva traducir(Map<String, Object> notificacion);
}
