package com.rodrigosambadesaa.garajeapi.dto;

import java.util.Locale;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum TipoMaterial {
  PIEZAS,
  PINTURA,
  CHAPA;

  @JsonCreator
  public static TipoMaterial fromJson(String valor) {
    return valueOf(valor.trim().toUpperCase(Locale.ROOT));
  }

  @JsonValue
  public String toJson() {
    return name().toLowerCase(Locale.ROOT);
  }
}
