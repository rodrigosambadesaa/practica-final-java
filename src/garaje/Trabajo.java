package garaje;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

import excepcionesgaraje.ExcepcionTrabajoEliminado;
import excepcionesgaraje.ExcepcionTrabajoFinalizado;
import excepcionesgaraje.ExcepcionValorNoValido;

public abstract class Trabajo implements Serializable {

  private static final long serialVersionUID = 1L;
  private static final BigDecimal PRECIO_HORA = new BigDecimal("30");

  private long id = -1;
  private String descripcion = "Sin descripcion";
  private BigDecimal horasNecesariasRealizacion = BigDecimal.ZERO;
  private boolean finalizado;
  private boolean eliminado;

  protected Trabajo() {
  }

  protected Trabajo(String descripcion) {
    setDescripcion(descripcion);
  }

  public void acumulaHorasNecesariasRealizacion(BigDecimal horas)
      throws ExcepcionTrabajoFinalizado, ExcepcionValorNoValido, ExcepcionTrabajoEliminado {
    validarTrabajoEditable();
    validarImporteNoNegativo(horas, "Las horas no pueden ser negativas");
    horasNecesariasRealizacion = horasNecesariasRealizacion.add(horas);
  }

  protected final void validarTrabajoEditable()
      throws ExcepcionTrabajoEliminado, ExcepcionTrabajoFinalizado {
    if (eliminado) {
      throw new ExcepcionTrabajoEliminado("El trabajo ha sido eliminado");
    }
    if (finalizado) {
      throw new ExcepcionTrabajoFinalizado("El trabajo ya esta finalizado");
    }
  }

  protected static void validarImportePositivo(BigDecimal valor, String mensaje)
      throws ExcepcionValorNoValido {
    if (valor == null || valor.signum() <= 0) {
      throw new ExcepcionValorNoValido(mensaje);
    }
  }

  private static void validarImporteNoNegativo(BigDecimal valor, String mensaje)
      throws ExcepcionValorNoValido {
    if (valor == null || valor.signum() < 0) {
      throw new ExcepcionValorNoValido(mensaje);
    }
  }

  public long getId() {
    return id;
  }

  public void setId(long id) throws ExcepcionValorNoValido {
    if (id < 0) {
      throw new ExcepcionValorNoValido("El ID no puede ser negativo");
    }
    this.id = id;
  }

  public String getDescripcion() {
    return descripcion;
  }

  public void setDescripcion(String descripcion) {
    String valor = Objects.requireNonNull(descripcion, "La descripcion no puede ser nula").trim();
    if (valor.isEmpty()) {
      throw new IllegalArgumentException("La descripcion no puede estar vacia");
    }
    this.descripcion = valor;
  }

  public BigDecimal getHorasNecesariasRealizacion() {
    return horasNecesariasRealizacion;
  }

  public void setHorasNecesariasRealizacion(BigDecimal horas) throws ExcepcionValorNoValido {
    validarImporteNoNegativo(horas, "Las horas no pueden ser negativas");
    this.horasNecesariasRealizacion = horas;
  }

  public boolean getFinalizado() {
    return finalizado;
  }

  public void setFinalizado(boolean finalizado) {
    if (eliminado && finalizado) {
      throw new IllegalStateException("No se puede finalizar un trabajo eliminado");
    }
    this.finalizado = finalizado;
  }

  public boolean getEliminado() {
    return eliminado;
  }

  public void setEliminado(boolean eliminado) {
    this.eliminado = eliminado;
  }

  public BigDecimal precioACobrar() {
    return PRECIO_HORA.multiply(horasNecesariasRealizacion);
  }

  public abstract int plazoMaximo();

  public static BigDecimal getPrecioParteFija() {
    return PRECIO_HORA;
  }

  @Override
  public boolean equals(Object objeto) {
    if (this == objeto) {
      return true;
    }
    if (objeto == null || getClass() != objeto.getClass()) {
      return false;
    }
    Trabajo otro = (Trabajo) objeto;
    return id >= 0 && id == otro.id;
  }

  @Override
  public int hashCode() {
    return id >= 0 ? Long.hashCode(id) : System.identityHashCode(this);
  }

  @Override
  public String toString() {
    return "ID: " + id
        + "\nDescripcion: " + descripcion
        + "\nHoras: " + horasNecesariasRealizacion
        + "\nPrecio: " + precioACobrar()
        + "\nFinalizado: " + (finalizado ? "Si" : "No")
        + "\nEliminado: " + (eliminado ? "Si" : "No")
        + "\nPlazo maximo: " + plazoMaximo() + " dias";
  }
}
