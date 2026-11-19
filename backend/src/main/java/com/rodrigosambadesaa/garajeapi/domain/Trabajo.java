package com.rodrigosambadesaa.garajeapi.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

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

  private static final BigDecimal MAX_CANTIDAD = new BigDecimal("9999999999.99");
  private static final BigDecimal PRECIO_HORA = new BigDecimal("30");
  private static final BigDecimal EXTRA_REVISION = new BigDecimal("20");
  private static final BigDecimal MARGEN_MECANICA = new BigDecimal("1.10");
  private static final BigDecimal MARGEN_CHAPA_PINTURA = new BigDecimal("1.30");

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
    BigDecimal fijo = getHoras().multiply(PRECIO_HORA);
    BigDecimal material = getCostePiezas().add(getCostePintura()).add(getCosteChapa());

    return switch (tipo) {
      case REPARACION_MECANICA -> fijo.add(material.multiply(MARGEN_MECANICA));
      case REPARACION_CHAPA_PINTURA -> fijo.add(material.multiply(MARGEN_CHAPA_PINTURA));
      case REVISION -> fijo.add(EXTRA_REVISION);
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
    this.tipo = Objects.requireNonNull(tipo, "El tipo de trabajo es obligatorio");
  }

  public String getDescripcion() {
    return descripcion;
  }

  public void setDescripcion(String descripcion) {
    String normalizada = Objects.requireNonNull(descripcion, "La descripcion es obligatoria").trim();
    if (normalizada.isEmpty() || normalizada.length() > 300) {
      throw new IllegalArgumentException("La descripcion debe tener entre 1 y 300 caracteres");
    }
    this.descripcion = normalizada;
  }

  public BigDecimal getHoras() {
    return horas == null ? BigDecimal.ZERO : horas;
  }

  public void setHoras(BigDecimal horas) {
    validarEditable();
    this.horas = validarCantidad(horas);
  }

  public BigDecimal getCostePiezas() {
    return costePiezas == null ? BigDecimal.ZERO : costePiezas;
  }

  public void setCostePiezas(BigDecimal costePiezas) {
    validarEditable();
    BigDecimal valor = validarCantidad(costePiezas);
    if (tipo == TrabajoTipo.REVISION && valor.signum() > 0) {
      throw new IllegalArgumentException("Una revision no admite material");
    }
    this.costePiezas = valor;
  }

  public BigDecimal getCostePintura() {
    return costePintura == null ? BigDecimal.ZERO : costePintura;
  }

  public void setCostePintura(BigDecimal costePintura) {
    validarEditable();
    BigDecimal valor = validarCantidad(costePintura);
    if (tipo != TrabajoTipo.REPARACION_CHAPA_PINTURA && valor.signum() > 0) {
      throw new IllegalArgumentException("La pintura solo corresponde a chapa y pintura");
    }
    this.costePintura = valor;
  }

  public BigDecimal getCosteChapa() {
    return costeChapa == null ? BigDecimal.ZERO : costeChapa;
  }

  public void setCosteChapa(BigDecimal costeChapa) {
    validarEditable();
    BigDecimal valor = validarCantidad(costeChapa);
    if (tipo != TrabajoTipo.REPARACION_CHAPA_PINTURA && valor.signum() > 0) {
      throw new IllegalArgumentException("La chapa solo corresponde a chapa y pintura");
    }
    this.costeChapa = valor;
  }

  public boolean isFinalizado() {
    return finalizado;
  }

  public void setFinalizado(boolean finalizado) {
    if (this.finalizado && !finalizado) {
      throw new IllegalStateException("No se puede reabrir un trabajo finalizado");
    }
    if (eliminado && finalizado) {
      throw new IllegalStateException("No se puede finalizar un trabajo eliminado");
    }
    this.finalizado = finalizado;
  }

  public boolean isEliminado() {
    return eliminado;
  }

  public void setEliminado(boolean eliminado) {
    if (this.eliminado && !eliminado) {
      throw new IllegalStateException("No se puede restaurar un trabajo eliminado");
    }
    this.eliminado = eliminado;
  }

  private void validarEditable() {
    if (eliminado || finalizado) {
      throw new IllegalStateException("No se puede modificar un trabajo eliminado o finalizado");
    }
  }

  private static BigDecimal validarCantidad(BigDecimal cantidad) {
    if (cantidad == null || cantidad.signum() < 0
        || cantidad.stripTrailingZeros().scale() > 2 || cantidad.compareTo(MAX_CANTIDAD) > 0) {
      throw new IllegalArgumentException("Cantidad no valida: debe estar entre 0 y 9999999999.99, con dos decimales");
    }
    return cantidad;
  }

  public LocalDateTime getCreadoEn() {
    return creadoEn;
  }

  public LocalDateTime getActualizadoEn() {
    return actualizadoEn;
  }
}
