package com.gymflow.api.repository;

import com.gymflow.api.entity.PlantillaFacial;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PlantillaFacialRepository extends JpaRepository<PlantillaFacial, Long> {

    Optional<PlantillaFacial> findByUsuarioId(Long usuarioId);

    // Solo clientes activos: un usuario baneado no puede entrar con el rostro
    List<PlantillaFacial> findByUsuarioActivoTrue();

    void deleteByUsuarioId(Long usuarioId);
}