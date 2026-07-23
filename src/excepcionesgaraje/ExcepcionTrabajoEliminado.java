package excepcionesgaraje;

public class ExcepcionTrabajoEliminado extends Exception {

  private static final long serialVersionUID = 1L;

  public ExcepcionTrabajoEliminado(String mensaje) {
    super(mensaje);
  }
}
