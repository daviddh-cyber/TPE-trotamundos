package com.trotamundos.proveedores.dominio;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

/** Asocia proveedor + código de reserva con el usuario dueño (Iteración 2, sección 5.2). */
@Entity
@Table(name = "reservas_monitoreadas", indexes = @Index(name = "idx_monitoreada_proveedor_codigo", columnList = "proveedor, codigo"))
public class ReservaMonitoreada {

    /** Mismo identificador que la reserva en el servicio Viajes. */
    @Id
    private UUID reservaId;

    @Column(nullable = false)
    private String usuarioId;

    @Column(nullable = false)
    private String proveedor;

    @Column(nullable = false)
    private String codigo;

    private Instant registradaEn;

    protected ReservaMonitoreada() {
        // requerido por JPA
    }

    public ReservaMonitoreada(UUID reservaId, String usuarioId, String proveedor, String codigo) {
        this.reservaId = reservaId;
        this.usuarioId = usuarioId;
        this.proveedor = proveedor;
        this.codigo = codigo;
        this.registradaEn = Instant.now();
    }

    public UUID getReservaId() {
        return reservaId;
    }

    public String getUsuarioId() {
        return usuarioId;
    }

    public String getProveedor() {
        return proveedor;
    }

    public String getCodigo() {
        return codigo;
    }

    public Instant getRegistradaEn() {
        return registradaEn;
    }
}
