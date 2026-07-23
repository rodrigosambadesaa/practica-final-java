package garaje;

import java.math.BigDecimal;

import excepcionesgaraje.ExcepcionTrabajoEliminado;
import excepcionesgaraje.ExcepcionTrabajoFinalizado;
import excepcionesgaraje.ExcepcionValorNoValido;

public abstract class Reparacion extends Trabajo {

  private static final long serialVersionUID = 1L;

  private BigDecimal precioPiezasUtilizadas = BigDecimal.ZERO;

  protected Reparacion() {
  }

  protected Reparacion(String descripcion) {
    super(descripcion);
  }

  public void acumulaPrecioPiezasUtilizadas(BigDecimal importe)
      throws ExcepcionValorNoValido, ExcepcionTrabajoFinalizado, ExcepcionTrabajoEliminado {
    validarTrabajoEditable();
    validarImportePositivo(importe, "El precio del material debe ser positivo");
    precioPiezasUtilizadas = getPrecioPiezasUtilizadas().add(importe);
  }

  public BigDecimal getPrecioPiezasUtilizadas() {
    return precioPiezasUtilizadas == null ? BigDecimal.ZERO : precioPiezasUtilizadas;
  }

  @Override
  public String toString() {
    return super.toString() + "\nCoste material: " + getPrecioPiezasUtilizadas();
  }
}
