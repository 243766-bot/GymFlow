package com.gymflow.api.repository;

import com.gymflow.api.entity.EstadoPago;
import com.gymflow.api.entity.MetodoPago;
import com.gymflow.api.entity.Pago;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PagoRepository extends JpaRepository<Pago, Long> {

    List<Pago> findByUsuarioIdOrderByFechaDesc(Long usuarioId);

    // Pagos de un día: desde las 00:00 (incluida) hasta las 00:00 del día siguiente (excluida)
    List<Pago> findByFechaGreaterThanEqualAndFechaLessThanOrderByFechaAsc(
            LocalDateTime desde, LocalDateTime hasta);

    // Pagos de un método y estado creados desde cierta fecha (para revisar los pendientes)
    List<Pago> findByMetodoAndEstadoAndFechaGreaterThanEqual(
            MetodoPago metodo, EstadoPago estado, LocalDateTime desde);

    // Trae el pago y lo BLOQUEA hasta que termine la transacción:
    // si dos consultas llegan al mismo tiempo, la segunda espera y no activa la membresía otra vez
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Pago p where p.id = :id")
    Optional<Pago> findByIdForUpdate(@Param("id") Long id);
}