package com.rodrigosambadesaa.garajeapi.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

class TrabajoTest {

  @Test
  void calculaPrecioDeReparacionMecanica() {
    Trabajo trabajo = new Trabajo();
    trabajo.setTipo(TrabajoTipo.REPARACION_MECANICA);
    trabajo.setHoras(new BigDecimal("2"));
    trabajo.setCostePiezas(new BigDecimal("100"));

    assertThat(trabajo.calcularPrecio()).isEqualByComparingTo("170");
    assertThat(trabajo.getPlazoDias()).isEqualTo(14);
  }

  @Test
  void calculaPrecioDeChapaYPintura() {
    Trabajo trabajo = new Trabajo();
    trabajo.setTipo(TrabajoTipo.REPARACION_CHAPA_PINTURA);
    trabajo.setHoras(new BigDecimal("3"));
    trabajo.setCosteChapa(new BigDecimal("100"));
    trabajo.setCostePintura(new BigDecimal("50"));

    assertThat(trabajo.calcularPrecio()).isEqualByComparingTo("285");
    assertThat(trabajo.getPlazoDias()).isEqualTo(21);
  }

  @Test
  void rechazaValoresInvalidosYEstadosIrreversibles() {
    Trabajo trabajo = new Trabajo();
    trabajo.setTipo(TrabajoTipo.REPARACION_MECANICA);
    assertThatThrownBy(() -> trabajo.setHoras(new BigDecimal("-0.01")))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> trabajo.setHoras(new BigDecimal("1.001")))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> trabajo.setCostePiezas(new BigDecimal("10000000000")))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> trabajo.setCostePintura(BigDecimal.ONE))
        .isInstanceOf(IllegalArgumentException.class);
    trabajo.setHoras(BigDecimal.ZERO);
    trabajo.setFinalizado(true);
    assertThatThrownBy(() -> trabajo.setHoras(BigDecimal.ONE))
        .isInstanceOf(IllegalStateException.class);
    assertThatThrownBy(() -> trabajo.setFinalizado(false))
        .isInstanceOf(IllegalStateException.class);
    trabajo.setEliminado(true);
    assertThatThrownBy(() -> trabajo.setEliminado(false))
        .isInstanceOf(IllegalStateException.class);
  }

  @Test
  void calculaPrecioDeRevision() {
    Trabajo trabajo = new Trabajo();
    trabajo.setTipo(TrabajoTipo.REVISION);
    trabajo.setHoras(new BigDecimal("1.5"));

    assertThat(trabajo.calcularPrecio()).isEqualByComparingTo("65");
    assertThat(trabajo.getPlazoDias()).isEqualTo(7);
  }
}
