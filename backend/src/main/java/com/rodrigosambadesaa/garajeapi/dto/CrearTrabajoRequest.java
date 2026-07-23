package com.rodrigosambadesaa.garajeapi.dto;

import com.rodrigosambadesaa.garajeapi.domain.TrabajoTipo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CrearTrabajoRequest(
    @NotNull TrabajoTipo tipo,
    @NotBlank @Size(max = 300) String descripcion) {
}
