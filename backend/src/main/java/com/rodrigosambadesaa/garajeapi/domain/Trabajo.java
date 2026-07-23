package com.rodrigosambadesaa.garajeapi.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "trabajos")
public class Trabajo {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private TrabajoTipo tipo;

  @Column(nullable = false, length = 300)
  private String descripcion;

  @Column(nullable = false, precision = 12, scale = 2)
  private BigDecimal horas = BigDecimal.ZERO;

  @Column(nullable = false, precision = 12, scale = 2)
  private BigDecimal costePiezas = BigDecimal.ZERO;

  @Column(nullable = false, precision = 12, scale = 2)
  private BigDecimal costePintura = BigDecimal.ZERO;

  @Column(nullable = false, precision = 12, scale = 2)
  private BigDecimal costeChapa = BigDecimal.ZERO;

  @Column(nullable = false)
  private boolean finalizado;

  @Column(nullable = false)
  private boolean eliminado;

  @Column(nullable = false)
  private LocalDateTime creadoEn;

  @Column(nullable = false)
  private LocalDateTime actualizadoEn;

  @PrePersist
  public void onCreate() {
    LocalDateTime ahora = LocalDateTime.now();
    creadoEn = ahora;
    actualizadoEn = ahora;
    if ( horas == null ) {
      horas = BigDecimal.ZERO;
    }
    if ( costePiezas == null ) {
      costePiezas = BigDecimal.ZERO;
    }
    if ( costePintura == null ) {
      costePintura = BigDecimal.ZERO;
    }
    if ( costeChapa == null ) {
      costeChapa = BigDecimal.ZERO;
    }
  }

  @PreUpdate
  public void onUpdate() {
    actualizadoEn = LocalDateTime.now();
  }

  public BigDecimal calcularPrecio() {
    BigDecimal fijo = getHoras().multiply(new BigDecimal("30"));
    BigDecimal material = getCostePiezas().add(getCostePintura()).add(getCosteChapa());

    return switch (tipo) {
      case REPARACION_MECANICA -> fijo.add(material.multiply(new BigDecimal("1.1")));
      case REPARACION_CHAPA_PINTURA -> fijo.add(material.multiply(new BigDecimal("1.3")));
      case REVISION -> fijo.add(new BigDecimal("20"));
    };
  }

  public int getPlazoDias() {
    return switch (tipo) {
      case REPARACION_MECANICA -> 14;
      case REPARACION_CHAPA_PINTURA -> 21;
      case REVISION -> 7;
    };
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public TrabajoTipo getTipo() {
    return tipo;
  }

  public void setTipo(TrabajoTipo tipo) {
    this.tipo = tipo;
  }

  public String getDescripcion() {
    return descripcion;
  }

  public void setDescripcion(String descripcion) {
    this.descripcion = descripcion;
  }

  public BigDecimal getHoras() {
    return horas == null ? BigDecimal.ZERO : horas;
  }

  public void setHoras(BigDecimal horas) {
    this.horas = horas;
  }

  public BigDecimal getCostePiezas() {
    return costePiezas == null ? BigDecimal.ZERO : costePiezas;
  }

  public void setCostePiezas(BigDecimal costePiezas) {
    this.costePiezas = costePiezas;
  }

  public BigDecimal getCostePintura() {
    return costePintura == null ? BigDecimal.ZERO : costePintura;
  }

  public void setCostePintura(BigDecimal costePintura) {
    this.costePintura = costePintura;
  }

  public BigDecimal getCosteChapa() {
    return costeChapa == null ? BigDecimal.ZERO : costeChapa;
  }

  public void setCosteChapa(BigDecimal costeChapa) {
    this.costeChapa = costeChapa;
  }

  public boolean isFinalizado() {
    return finalizado;
  }

  public void setFinalizado(boolean finalizado) {
    this.finalizado = finalizado;
  }

  public boolean isEliminado() {
    return eliminado;
  }

  public void setEliminado(boolean eliminado) {
    this.eliminado = eliminado;
  }

  public LocalDateTime getCreadoEn() {
    return creadoEn;
  }

  public LocalDateTime getActualizadoEn() {
    return actualizadoEn;
  }
}
