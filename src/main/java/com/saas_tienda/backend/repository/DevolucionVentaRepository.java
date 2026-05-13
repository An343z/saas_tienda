package com.saas_tienda.backend.repository;

import com.saas_tienda.backend.domain.DevolucionVenta;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DevolucionVentaRepository extends JpaRepository<DevolucionVenta, Long> {
    List<DevolucionVenta> findByVentaIdOrderByFechaDesc(Long ventaId);
}
