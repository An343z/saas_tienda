package com.saas_tienda.backend.repository;

import com.saas_tienda.backend.domain.InventarioTienda;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import jakarta.persistence.LockModeType;

public interface InventarioTiendaRepository extends JpaRepository<InventarioTienda, Long> {
    List<InventarioTienda> findByTiendaIdOrderByProductoNombre(Long tiendaId);

    Optional<InventarioTienda> findByTiendaIdAndProductoId(Long tiendaId, Long productoId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<InventarioTienda> findLockedByTiendaIdAndProductoId(Long tiendaId, Long productoId);
}
