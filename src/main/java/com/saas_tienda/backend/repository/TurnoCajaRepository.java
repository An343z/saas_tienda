package com.saas_tienda.backend.repository;

import com.saas_tienda.backend.domain.EstadoTurnoCaja;
import com.saas_tienda.backend.domain.TurnoCaja;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import jakarta.persistence.LockModeType;

public interface TurnoCajaRepository extends JpaRepository<TurnoCaja, Long> {
    Optional<TurnoCaja> findFirstByTiendaIdAndUsuarioIdAndEstadoOrderByFechaAperturaDesc(Long tiendaId, Long usuarioId, EstadoTurnoCaja estado);

    List<TurnoCaja> findByTiendaIdOrderByFechaAperturaDesc(Long tiendaId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<TurnoCaja> findLockedById(Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<TurnoCaja> findFirstLockedByTiendaIdAndUsuarioIdAndEstadoOrderByFechaAperturaDesc(Long tiendaId, Long usuarioId, EstadoTurnoCaja estado);

    boolean existsByTiendaIdAndUsuarioIdAndEstado(Long tiendaId, Long usuarioId, EstadoTurnoCaja estado);
}
