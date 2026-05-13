package com.saas_tienda.backend.web;

import com.saas_tienda.backend.dto.CajaDtos;
import com.saas_tienda.backend.service.CajaService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/caja")
public class CajaController {
    private final CajaService cajaService;

    public CajaController(CajaService cajaService) {
        this.cajaService = cajaService;
    }

    @PostMapping("/turnos/abrir")
    public CajaDtos.TurnoCajaResponse abrir(@Valid @RequestBody CajaDtos.AbrirTurnoRequest request) {
        return cajaService.toResponse(cajaService.abrir(request), true);
    }

    @PostMapping("/turnos/{id}/cerrar")
    public CajaDtos.TurnoCajaResponse cerrar(@PathVariable Long id, @Valid @RequestBody CajaDtos.CerrarTurnoRequest request) {
        return cajaService.toResponse(cajaService.cerrar(id, request), true);
    }

    @GetMapping("/turnos/{id}")
    public CajaDtos.TurnoCajaResponse buscar(@PathVariable Long id) {
        return cajaService.toResponse(cajaService.buscar(id), true);
    }

    @GetMapping("/turnos/actual")
    public CajaDtos.TurnoCajaResponse actual(@RequestParam Long tiendaId, @RequestParam Long usuarioId) {
        return cajaService.toResponse(cajaService.actual(tiendaId, usuarioId), true);
    }

    @GetMapping("/turnos")
    public List<CajaDtos.TurnoCajaResponse> listar(@RequestParam Long tiendaId) {
        return cajaService.listarPorTienda(tiendaId).stream()
                .map(turno -> cajaService.toResponse(turno, false))
                .toList();
    }

    @PostMapping("/movimientos")
    public CajaDtos.MovimientoCajaResponse movimiento(@Valid @RequestBody CajaDtos.MovimientoCajaRequest request) {
        return cajaService.toResponse(cajaService.registrarMovimiento(request));
    }

    @GetMapping("/turnos/{id}/movimientos")
    public List<CajaDtos.MovimientoCajaResponse> movimientos(@PathVariable Long id) {
        return cajaService.movimientos(id).stream().map(cajaService::toResponse).toList();
    }
}
