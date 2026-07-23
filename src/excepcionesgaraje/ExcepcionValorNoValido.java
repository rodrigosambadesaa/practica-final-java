package excepcionesgaraje;

public class ExcepcionValorNoValido extends Exception {

  private static final long serialVersionUID = 1L;

  public ExcepcionValorNoValido(String mensaje) {
    super(mensaje);
  }
}
