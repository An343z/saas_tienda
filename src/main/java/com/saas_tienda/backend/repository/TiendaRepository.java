package com.saas_tienda.backend.repository;

import com.saas_tienda.backend.domain.Tienda;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TiendaRepository extends JpaRepository<Tienda, Long> {
}
