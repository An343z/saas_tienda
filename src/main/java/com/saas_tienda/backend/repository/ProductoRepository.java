package com.saas_tienda.backend.repository;

import com.saas_tienda.backend.domain.Producto;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductoRepository extends JpaRepository<Producto, Long> {
    List<Producto> findByActivoTrueOrderByNombre();

    @Query("""
            select p from Producto p
            where p.activo = true
              and (lower(p.sku) = lower(:codigo) or p.codigoBarras = :codigo)
            """)
    Optional<Producto> findActivoByCodigoExacto(@Param("codigo") String codigo);

    @Query("""
            select p from Producto p
            where p.activo = true
              and (
                    lower(p.sku) like lower(concat('%', :texto, '%'))
                 or lower(p.nombre) like lower(concat('%', :texto, '%'))
                 or p.codigoBarras like concat('%', :texto, '%')
              )
            order by p.nombre
            """)
    List<Producto> buscarActivos(@Param("texto") String texto);
}
