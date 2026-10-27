package com.trotamundos.viajes.dominio;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * Reserva con núcleo común + detalles por tipo (ADR 006).
 * Las fechas se guardan en UTC.
 */
@Entity
@Table(name = "reservas", indexes = @Index(name = "idx_reserva_proveedor_codigo", columnList = "proveedor, codigo"))
public class Reserva {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "viaje_id")
    private Viaje viaje;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoReserva tipo;

    @Column(nullable = false)
    private String proveedor;

    @Column(nullable = false)
    private String codigo;

    @Column(nullable = false)
    private LocalDateTime fechaInicio;

    @Column(nullable = false)
    private LocalDateTime fechaFin;

    private String origen;

    private String destino;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoReserva estado;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FuenteReserva fuente;

    /** Datos propios de cada tipo de reserva (número de vuelo, puerta, tipo de habitación...). */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> detalles = new HashMap<>();

    private Instant ultimaActualizacion;

    protected Reserva() {
        // requerido por JPA
    }

    public Reserva(TipoReserva tipo, String proveedor, String codigo,
                   LocalDateTime fechaInicio, LocalDateTime fechaFin,
                   String origen, String destino, FuenteReserva fuente,
                   Map<String, Object> detalles) {
        if (fechaFin.isBefore(fechaInicio)) {
            throw new IllegalArgumentException("La fecha de fin no puede ser anterior a la de inicio");
        }
        this.id = UUID.randomUUID();
        this.tipo = tipo;
        this.proveedor = proveedor;
        this.codigo = codigo;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.origen = origen;
        this.destino = destino;
        this.fuente = fuente;
        this.detalles = detalles == null ? new HashMap<>() : new HashMap<>(detalles);
        this.estado = EstadoReserva.CONFIRMADA;
        this.ultimaActualizacion = Instant.now();
    }

    /**
     * Aplica los cambios informados por un proveedor. Los campos del núcleo se actualizan
     * directamente; el resto (por ejemplo, "puerta") se guarda en los detalles.
     */
    public void aplicarActualizacion(Map<String, Object> cambios) {
        if (estado == EstadoReserva.CANCELADA) {
            return;
        }
        Map<String, Object> nuevosDetalles = new HashMap<>(detalles);
        cambios.forEach((campo, valor) -> {
            switch (campo) {
                case "fechaInicio" -> this.fechaInicio = parsearFecha(valor);
                case "fechaFin" -> this.fechaFin = parsearFecha(valor);
                case "origen" -> this.origen = String.valueOf(valor);
                case "destino" -> this.destino = String.valueOf(valor);
                default -> nuevosDetalles.put(campo, valor);
            }
        });
        this.detalles = nuevosDetalles;
        this.ultimaActualizacion = Instant.now();
    }

    public void cancelar() {
        this.estado = EstadoReserva.CANCELADA;
        this.ultimaActualizacion = Instant.now();
    }

    /** Acepta fechas con zona ("2026-11-10T09:30:00Z") o sin zona, interpretadas como UTC. */
    static LocalDateTime parsearFecha(Object valor) {
        String texto = String.valueOf(valor);
        try {
            return OffsetDateTime.parse(texto).withOffsetSameInstant(ZoneOffset.UTC).toLocalDateTime();
        } catch (DateTimeParseException e) {
            return LocalDateTime.parse(texto);
        }
    }

    void asignarViaje(Viaje viaje) {
        this.viaje = viaje;
    }

    public UUID getId() {
        return id;
    }

    public Viaje getViaje() {
        return viaje;
    }

    public TipoReserva getTipo() {
        return tipo;
    }

    public String getProveedor() {
        return proveedor;
    }

    public String getCodigo() {
        return codigo;
    }

    public LocalDateTime getFechaInicio() {
        return fechaInicio;
    }

    public LocalDateTime getFechaFin() {
        return fechaFin;
    }

    public String getOrigen() {
        return origen;
    }

    public String getDestino() {
        return destino;
    }

    public EstadoReserva getEstado() {
        return estado;
    }

    public FuenteReserva getFuente() {
        return fuente;
    }

    public Map<String, Object> getDetalles() {
        return detalles;
    }

    public Instant getUltimaActualizacion() {
        return ultimaActualizacion;
    }
}
