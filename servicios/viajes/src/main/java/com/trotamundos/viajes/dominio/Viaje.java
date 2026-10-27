package com.trotamundos.viajes.dominio;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Agrupación de reservas con un destino y un rango de fechas (RF-13). */
@Entity
@Table(name = "viajes")
public class Viaje {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String usuarioId;

    @Column(nullable = false)
    private String nombre;

    private String destino;

    @Column(nullable = false)
    private LocalDate fechaInicio;

    @Column(nullable = false)
    private LocalDate fechaFin;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoViaje estado;

    @OneToMany(mappedBy = "viaje", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("fechaInicio ASC")
    private List<Reserva> reservas = new ArrayList<>();

    protected Viaje() {
        // requerido por JPA
    }

    public Viaje(String usuarioId, String nombre, String destino, LocalDate fechaInicio, LocalDate fechaFin) {
        this.id = UUID.randomUUID();
        this.usuarioId = usuarioId;
        this.nombre = nombre;
        this.destino = destino;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.estado = EstadoViaje.ACTIVO;
    }

    public void agregarReserva(Reserva reserva) {
        reservas.add(reserva);
        reserva.asignarViaje(this);
        recalcularFechas();
    }

    public void quitarReserva(Reserva reserva) {
        reservas.remove(reserva);
        recalcularFechas();
    }

    /** Ajusta las fechas del viaje a las de sus reservas no canceladas. */
    public void recalcularFechas() {
        List<Reserva> vigentes = reservas.stream()
                .filter(r -> r.getEstado() != EstadoReserva.CANCELADA)
                .toList();
        if (vigentes.isEmpty()) {
            return;
        }
        this.fechaInicio = vigentes.stream()
                .map(r -> r.getFechaInicio().toLocalDate())
                .min(LocalDate::compareTo)
                .orElse(fechaInicio);
        this.fechaFin = vigentes.stream()
                .map(r -> r.getFechaFin().toLocalDate())
                .max(LocalDate::compareTo)
                .orElse(fechaFin);
    }

    /** RF-15: un viaje finalizado sale de la vista principal. */
    public void archivar() {
        this.estado = EstadoViaje.ARCHIVADO;
    }

    public UUID getId() {
        return id;
    }

    public String getUsuarioId() {
        return usuarioId;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDestino() {
        return destino;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public LocalDate getFechaFin() {
        return fechaFin;
    }

    public EstadoViaje getEstado() {
        return estado;
    }

    public List<Reserva> getReservas() {
        return reservas;
    }
}
