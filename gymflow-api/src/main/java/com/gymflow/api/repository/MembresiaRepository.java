package com.gymflow.api.repository;

import com.gymflow.api.entity.EstadoMembresia;
import com.gymflow.api.entity.Membresia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface MembresiaRepository extends JpaRepository<Membresia, Long> {

    // La membresía más reciente de un cliente (la que cuenta para la caja)
    Optional<Membresia> findFirstByUsuarioIdOrderByFechaVencimientoDesc(Long usuarioId);

    List<Membresia> findAllByOrderByIdDesc();

    List<Membresia> findByUsuarioIdOrderByIdDesc(Long usuarioId);

    boolean existsByPlanId(Long planId);

    boolean existsByUsuarioId(Long usuarioId);

    // ¿El cliente ya tiene una membresía activa y vigente?
    boolean existsByUsuarioIdAndEstadoAndFechaVencimientoGreaterThanEqual(
            Long usuarioId, EstadoMembresia estado, LocalDate fecha);
}