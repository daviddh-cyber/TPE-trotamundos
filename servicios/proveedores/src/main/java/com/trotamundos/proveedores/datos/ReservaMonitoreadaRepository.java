package com.trotamundos.proveedores.datos;

import com.trotamundos.proveedores.dominio.ReservaMonitoreada;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservaMonitoreadaRepository extends JpaRepository<ReservaMonitoreada, UUID> {

    List<ReservaMonitoreada> findByProveedorAndCodigo(String proveedor, String codigo);
}
