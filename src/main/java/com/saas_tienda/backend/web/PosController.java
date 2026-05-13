package com.saas_tienda.backend.web;

import com.saas_tienda.backend.dto.VentaDtos;
import com.saas_tienda.backend.service.ImpresionTicketService;
import com.saas_tienda.backend.service.VentaService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/pos")
public class PosController {
    private final VentaService ventaService;

    public PosController(VentaService ventaService) {
        this.ventaService = ventaService;
    }

    @PostMapping("/ventas")
    public VentaDtos.VentaResponse registrar(@Valid @RequestBody VentaDtos.VentaRequest request) {
        return ventaService.toResponse(ventaService.registrarPos(request));
    }

    @PostMapping("/cajon/abrir")
    public ImpresionTicketService.ResultadoImpresion abrirCajon() {
        return ventaService.abrirCajon();
    }
}
