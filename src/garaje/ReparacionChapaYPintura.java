package garaje;

import java.math.BigDecimal;

import excepcionesgaraje.ExcepcionTrabajoEliminado;
import excepcionesgaraje.ExcepcionTrabajoFinalizado;
import excepcionesgaraje.ExcepcionValorNoValido;
import interfaces.Plazo;

public class ReparacionChapaYPintura extends Reparacion implements Plazo {

  private static final long serialVersionUID = 1L;
  private static final BigDecimal MARGEN_MATERIAL = new BigDecimal("1.30");

  private BigDecimal precioPintura = BigDecimal.ZERO;
  private BigDecimal precioChapa = BigDecimal.ZERO;

  public ReparacionChapaYPintura() {
  }

  public ReparacionChapaYPintura(String descripcion) {
    super(descripcion);
  }

  public void acumulaPrecioPintura(BigDecimal importe)
      throws ExcepcionValorNoValido, ExcepcionTrabajoFinalizado, ExcepcionTrabajoEliminado {
    validarTrabajoEditable();
    validarImportePositivo(importe, "El precio de la pintura debe ser positivo");
    precioPintura = getPrecioPintura().add(importe);
  }

  public void acumulaPrecioChapa(BigDecimal importe)
      throws ExcepcionValorNoValido, ExcepcionTrabajoFinalizado, ExcepcionTrabajoEliminado {
    validarTrabajoEditable();
    validarImportePositivo(importe, "El precio de la chapa debe ser positivo");
    precioChapa = getPrecioChapa().add(importe);
  }

  public BigDecimal getPrecioPintura() {
    return precioPintura == null ? BigDecimal.ZERO : precioPintura;
  }

  public BigDecimal getPrecioChapa() {
    return precioChapa == null ? BigDecimal.ZERO : precioChapa;
  }

  @Override
  public BigDecimal getPrecioPiezasUtilizadas() {
    return super.getPrecioPiezasUtilizadas().add(getPrecioPintura()).add(getPrecioChapa());
  }

  @Override
  public int plazoMaximo() {
    return 21;
  }

  @Override
  public BigDecimal precioACobrar() {
    return super.precioACobrar().add(getPrecioPiezasUtilizadas().multiply(MARGEN_MATERIAL));
  }

  @Override
  public String toString() {
    return "Tipo: Reparacion de chapa y pintura\n" + super.toString()
        + "\nCoste chapa: " + getPrecioChapa()
        + "\nCoste pintura: " + getPrecioPintura();
  }
}
