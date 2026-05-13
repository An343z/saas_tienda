package com.saas_tienda.backend.service;

import com.saas_tienda.backend.domain.Producto;
import com.saas_tienda.backend.domain.UnidadVenta;
import com.saas_tienda.backend.dto.ProductoDtos;
import com.saas_tienda.backend.repository.ProductoRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductoService {
    private final ProductoRepository productoRepository;

    public ProductoService(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
    }

    @Transactional(readOnly = true)
    public List<Producto> listarActivos() {
        return productoRepository.findByActivoTrueOrderByNombre();
    }

    @Transactional(readOnly = true)
    public List<Producto> buscar(String texto) {
        if (texto == null || texto.isBlank()) {
            return listarActivos();
        }
        return productoRepository.buscarActivos(texto.trim());
    }

    @Transactional(readOnly = true)
    public Producto buscarPorCodigo(String codigo) {
        if (codigo == null || codigo.isBlank()) {
            throw new IllegalArgumentException("El codigo no puede estar vacio");
        }
        return productoRepository.findActivoByCodigoExacto(codigo.trim())
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe un producto activo con el codigo " + codigo));
    }

    @Transactional
    public Producto crear(ProductoDtos.ProductoRequest request) {
        Producto producto = new Producto(
                request.sku().trim(),
                normalizarCodigoBarras(request.codigoBarras()),
                request.nombre().trim(),
                request.costoBase(),
                request.precioBase(),
                unidadVenta(request.unidadVenta()),
                boolDefault(request.controlaInventario(), true));
        producto.setActivo(boolDefault(request.activo(), true));
        return productoRepository.save(producto);
    }

    @Transactional
    public Producto actualizar(Long id, ProductoDtos.ProductoRequest request) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto no encontrado"));
        producto.setSku(request.sku().trim());
        producto.setCodigoBarras(normalizarCodigoBarras(request.codigoBarras()));
        producto.setNombre(request.nombre().trim());
        producto.setCostoBase(request.costoBase());
        producto.setPrecioBase(request.precioBase());
        producto.setUnidadVenta(unidadVenta(request.unidadVenta()));
        producto.setControlaInventario(boolDefault(request.controlaInventario(), true));
        producto.setActivo(boolDefault(request.activo(), true));
        return producto;
    }

    private String normalizarCodigoBarras(String codigoBarras) {
        return codigoBarras == null || codigoBarras.isBlank() ? null : codigoBarras.trim();
    }

    private UnidadVenta unidadVenta(UnidadVenta unidadVenta) {
        return unidadVenta == null ? UnidadVenta.PIEZA : unidadVenta;
    }

    private boolean boolDefault(Boolean value, boolean defaultValue) {
        return value == null ? defaultValue : value;
    }
}
