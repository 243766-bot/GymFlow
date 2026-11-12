package com.gymflow.api.repository;

import com.gymflow.api.entity.Producto;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface ProductoRepository extends JpaRepository<Producto, Long> {

    // Trae los productos y los BLOQUEA hasta que termine la transacción.
    // Así, dos ventas al mismo tiempo no pueden vender la misma última pieza.
    // Se ordenan por id para evitar bloqueos cruzados entre ventas.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Producto p where p.id in :ids order by p.id")
    List<Producto> findAllByIdForUpdate(@Param("ids") Collection<Long> ids);
}