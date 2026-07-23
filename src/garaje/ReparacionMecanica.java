package garaje;

import java.math.BigDecimal;

import interfaces.Plazo;

public class ReparacionMecanica extends Reparacion implements Plazo {

  private static final long serialVersionUID = 1L;
  private static final BigDecimal MARGEN_MATERIAL = new BigDecimal("1.10");

  public ReparacionMecanica() {
  }

  public ReparacionMecanica(String descripcion) {
    super(descripcion);
  }

  @Override
  public int plazoMaximo() {
    return 14;
  }

  @Override
  public BigDecimal precioACobrar() {
    return super.precioACobrar().add(getPrecioPiezasUtilizadas().multiply(MARGEN_MATERIAL));
  }

  @Override
  public String toString() {
    return "Tipo: Reparacion mecanica\n" + super.toString();
  }
}
