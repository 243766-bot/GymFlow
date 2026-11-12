package com.gymflow.api.repository;

import com.gymflow.api.entity.DetalleVenta;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DetalleVentaRepository extends JpaRepository<DetalleVenta, Long> {

    boolean existsByProductoId(Long productoId);
}