package com.saas_tienda.backend.dto;

import com.saas_tienda.backend.domain.Rol;
import jakarta.validation.constraints.NotBlank;

public final class AuthDtos {
    private AuthDtos() {
    }

    public record LoginRequest(@NotBlank String username, @NotBlank String password) {
    }

    public record LoginResponse(String token, Long usuarioId, String nombre, Rol rol, Long tiendaId, String tiendaNombre) {
    }
}
