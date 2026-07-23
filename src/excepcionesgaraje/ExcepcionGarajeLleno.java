package excepcionesgaraje;

public class ExcepcionGarajeLleno extends Exception {

  private static final long serialVersionUID = 1L;

  public ExcepcionGarajeLleno(String mensaje) {
    super(mensaje);
  }
}
