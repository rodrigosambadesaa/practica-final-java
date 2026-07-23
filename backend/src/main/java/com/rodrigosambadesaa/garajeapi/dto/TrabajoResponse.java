package com.rodrigosambadesaa.garajeapi.dto;

import java.math.BigDecimal;
import java.util.Objects;

import com.rodrigosambadesaa.garajeapi.domain.Trabajo;
import com.rodrigosambadesaa.garajeapi.domain.TrabajoTipo;

public record TrabajoResponse(
    long id,
    TrabajoTipo tipo,
    String descripcion,
    BigDecimal horas,
    BigDecimal costePiezas,
    BigDecimal costePintura,
    BigDecimal costeChapa,
    boolean finalizado,
    boolean eliminado,
    int plazoDias,
    BigDecimal precio) {

  public static TrabajoResponse from(Trabajo trabajo) {
    return new TrabajoResponse(
        Objects.requireNonNull(trabajo.getId(), "El trabajo debe estar persistido"),
        trabajo.getTipo(),
        trabajo.getDescripcion(),
        trabajo.getHoras(),
        trabajo.getCostePiezas(),
        trabajo.getCostePintura(),
        trabajo.getCosteChapa(),
        trabajo.isFinalizado(),
        trabajo.isEliminado(),
        trabajo.getPlazoDias(),
        trabajo.calcularPrecio());
  }
}
