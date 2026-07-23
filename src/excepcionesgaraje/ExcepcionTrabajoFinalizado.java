package excepcionesgaraje;

public class ExcepcionTrabajoFinalizado extends Exception {

  private static final long serialVersionUID = 1L;

  public ExcepcionTrabajoFinalizado(String mensaje) {
    super(mensaje);
  }
}
