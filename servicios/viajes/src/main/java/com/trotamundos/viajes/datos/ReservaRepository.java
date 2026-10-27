package com.trotamundos.viajes.datos;

import com.trotamundos.viajes.dominio.Reserva;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservaRepository extends JpaRepository<Reserva, UUID> {

    Optional<Reserva> findFirstByProveedorAndCodigoAndViaje_UsuarioId(String proveedor, String codigo, String usuarioId);

    Optional<Reserva> findByIdAndViaje_UsuarioId(UUID id, String usuarioId);
}
