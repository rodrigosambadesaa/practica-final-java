package com.rodrigosambadesaa.garajeapi.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public record AumentarHorasRequest(
    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    BigDecimal horas) {
}
