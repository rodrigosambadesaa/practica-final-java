package garaje;

import java.math.BigDecimal;

import interfaces.Plazo;

public class Revision extends Trabajo implements Plazo {

  private static final long serialVersionUID = 1L;
  private static final BigDecimal EXTRA = new BigDecimal("20");

  public Revision() {
  }

  public Revision(String descripcion) {
    super(descripcion);
  }

  @Override
  public int plazoMaximo() {
    return 7;
  }

  @Override
  public BigDecimal precioACobrar() {
    return super.precioACobrar().add(EXTRA);
  }

  public static BigDecimal getExtra() {
    return EXTRA;
  }

  @Override
  public String toString() {
    return "Tipo: Revision\n" + super.toString() + "\nExtra revision: " + EXTRA;
  }
}
