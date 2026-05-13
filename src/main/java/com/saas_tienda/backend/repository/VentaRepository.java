package com.saas_tienda.backend.repository;

import com.saas_tienda.backend.domain.Venta;
import jakarta.persistence.LockModeType;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

public interface VentaRepository extends JpaRepository<Venta, Long> {
    List<Venta> findByTiendaIdAndFechaBetweenOrderByFechaDesc(Long tiendaId, Instant desde, Instant hasta);

    List<Venta> findByFechaBetweenOrderByFechaDesc(Instant desde, Instant hasta);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Venta> findLockedById(Long id);
}
