package com.gymflow.api.repository;

import com.gymflow.api.entity.MovimientoCaja;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface MovimientoCajaRepository extends JpaRepository<MovimientoCaja, Long> {

    List<MovimientoCaja> findByFechaGreaterThanEqualAndFechaLessThanOrderByFechaAsc(
            LocalDateTime desde, LocalDateTime hasta);
}