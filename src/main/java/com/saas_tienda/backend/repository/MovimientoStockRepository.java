package com.saas_tienda.backend.repository;

import com.saas_tienda.backend.domain.MovimientoStock;
import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MovimientoStockRepository extends JpaRepository<MovimientoStock, Long> {
    List<MovimientoStock> findByTiendaIdAndFechaBetweenOrderByFechaDesc(Long tiendaId, Instant desde, Instant hasta);

    List<MovimientoStock> findByFechaBetweenOrderByFechaDesc(Instant desde, Instant hasta);
}
