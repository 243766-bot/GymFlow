package com.gymflow.api.repository;

import com.gymflow.api.entity.Venta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface VentaRepository extends JpaRepository<Venta, Long> {

    List<Venta> findAllByOrderByFechaDesc();

    // Ventas de un día: desde las 00:00 (incluida) hasta las 00:00 del día siguiente (excluida)
    List<Venta> findByFechaGreaterThanEqualAndFechaLessThanOrderByFechaAsc(
            LocalDateTime desde, LocalDateTime hasta);
}