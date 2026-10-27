package com.trotamundos.proveedores.servicio;

public class ProveedorDesconocidoException extends RuntimeException {

    public ProveedorDesconocidoException(String proveedor) {
        super("No hay un adaptador para el proveedor '" + proveedor + "'");
    }
}
