package com.saas_tienda.backend.repository;

import com.saas_tienda.backend.domain.Egreso;
import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EgresoRepository extends JpaRepository<Egreso, Long> {
    List<Egreso> findByTiendaIdAndFechaBetweenOrderByFechaDesc(Long tiendaId, Instant desde, Instant hasta);

    List<Egreso> findByFechaBetweenOrderByFechaDesc(Instant desde, Instant hasta);
}
