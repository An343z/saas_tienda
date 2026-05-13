package com.saas_tienda.backend.web;

import com.saas_tienda.backend.dto.DevolucionDtos;
import com.saas_tienda.backend.service.DevolucionService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/pos")
public class DevolucionController {
    private final DevolucionService devolucionService;

    public DevolucionController(DevolucionService devolucionService) {
        this.devolucionService = devolucionService;
    }

    @PostMapping("/devoluciones")
    public DevolucionDtos.DevolucionResponse devolver(@Valid @RequestBody DevolucionDtos.DevolucionRequest request) {
        return devolucionService.toResponse(devolucionService.devolver(request));
    }

    @PostMapping("/ventas/{ventaId}/cancelar")
    public DevolucionDtos.DevolucionResponse cancelar(@PathVariable Long ventaId, @Valid @RequestBody DevolucionDtos.CancelarVentaRequest request) {
        return devolucionService.toResponse(devolucionService.cancelar(ventaId, request));
    }

    @GetMapping("/ventas/{ventaId}/devoluciones")
    public List<DevolucionDtos.DevolucionResponse> listar(@PathVariable Long ventaId) {
        return devolucionService.listarPorVenta(ventaId).stream().map(devolucionService::toResponse).toList();
    }
}
