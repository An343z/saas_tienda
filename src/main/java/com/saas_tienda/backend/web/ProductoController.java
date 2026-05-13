package com.saas_tienda.backend.web;

import com.saas_tienda.backend.dto.DtoMapper;
import com.saas_tienda.backend.dto.ProductoDtos;
import com.saas_tienda.backend.service.ProductoService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/productos")
public class ProductoController {
    private final ProductoService productoService;

    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    @GetMapping
    public List<ProductoDtos.ProductoResponse> listar(@RequestParam(required = false) String q) {
        return productoService.buscar(q).stream().map(DtoMapper::producto).toList();
    }

    @GetMapping("/codigo/{codigo}")
    public ProductoDtos.ProductoResponse buscarPorCodigo(@PathVariable String codigo) {
        return DtoMapper.producto(productoService.buscarPorCodigo(codigo));
    }

    @PostMapping
    public ProductoDtos.ProductoResponse crear(@Valid @RequestBody ProductoDtos.ProductoRequest request) {
        return DtoMapper.producto(productoService.crear(request));
    }

    @PutMapping("/{id}")
    public ProductoDtos.ProductoResponse actualizar(@PathVariable Long id, @Valid @RequestBody ProductoDtos.ProductoRequest request) {
        return DtoMapper.producto(productoService.actualizar(id, request));
    }
}
