package com.saas_tienda.backend.repository;

import com.saas_tienda.backend.domain.MovimientoCaja;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MovimientoCajaRepository extends JpaRepository<MovimientoCaja, Long> {
    List<MovimientoCaja> findByTurnoCajaIdOrderByFechaAsc(Long turnoCajaId);
}
