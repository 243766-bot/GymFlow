package com.gymflow.api.repository;

import com.gymflow.api.entity.Pago;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface PagoRepository extends JpaRepository<Pago, Long> {

    List<Pago> findByUsuarioIdOrderByFechaDesc(Long usuarioId);

    // Pagos de un día: desde las 00:00 (incluida) hasta las 00:00 del día siguiente (excluida)
    List<Pago> findByFechaGreaterThanEqualAndFechaLessThanOrderByFechaAsc(
            LocalDateTime desde, LocalDateTime hasta);
}