package com.rodrigosambadesaa.garajeapi.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

public record AumentarMaterialRequest(
    @NotNull TipoMaterial tipoMaterial,
    @NotNull @DecimalMin("0") @Digits(integer = 10, fraction = 2) BigDecimal coste) {
}
