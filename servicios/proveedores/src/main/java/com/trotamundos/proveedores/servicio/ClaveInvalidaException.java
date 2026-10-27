package com.trotamundos.proveedores.servicio;

public class ClaveInvalidaException extends RuntimeException {

    public ClaveInvalidaException(String proveedor) {
        super("Clave inválida para el proveedor '" + proveedor + "'");
    }
}
