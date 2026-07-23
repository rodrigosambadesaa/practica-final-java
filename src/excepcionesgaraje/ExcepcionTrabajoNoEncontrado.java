package excepcionesgaraje;

public class ExcepcionTrabajoNoEncontrado extends Exception {

  private static final long serialVersionUID = 1L;

  public ExcepcionTrabajoNoEncontrado(String mensaje) {
    super(mensaje);
  }
}
