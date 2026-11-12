package com.gymflow.api.repository;

import com.gymflow.api.entity.PlantillaFacial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface PlantillaFacialRepository extends JpaRepository<PlantillaFacial, Long> {

    Optional<PlantillaFacial> findByUsuarioId(Long usuarioId);

    boolean existsByUsuarioId(Long usuarioId);

    // Solo clientes activos: un usuario baneado no puede entrar con el rostro
    List<PlantillaFacial> findByUsuarioActivoTrue();

    // IDs de los usuarios que ya tienen rostro (sin cargar los descriptores)
    @Query("select p.usuario.id from PlantillaFacial p")
    List<Long> findUsuarioIdsConRostro();

    void deleteByUsuarioId(Long usuarioId);
}