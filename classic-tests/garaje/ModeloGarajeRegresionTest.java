package garaje;

import java.math.BigDecimal;
import java.util.List;

import excepcionesgaraje.ExcepcionTrabajoEliminado;
import excepcionesgaraje.ExcepcionTrabajoFinalizado;
import excepcionesgaraje.ExcepcionValorNoValido;
import utilidades.GestorPersistenciaGaraje;

/** Pruebas sin dependencias externas de las invariantes de la practica y ampliaciones. */
public final class ModeloGarajeRegresionTest {

  private ModeloGarajeRegresionTest() {
  }

  public static void main(String[] args) throws Exception {
    importesYHorasAceptanCeroPeroNoNegativos();
    trabajoFinalizadoNoPuedeModificarseNiReabrirse();
    identificadoresSonUnicosEInmutables();
    seConservaElOrdenPorDescripcion();
    contadorSeRecuperaDesdeElGaraje();
    csvConservaComillasYNeutralizaFormulas();
    System.out.println("Pruebas adicionales del modelo clasico: OK");
  }

  private static void importesYHorasAceptanCeroPeroNoNegativos() throws Exception {
    ReparacionMecanica mecanica = new ReparacionMecanica("Motor");
    mecanica.acumulaHorasNecesariasRealizacion(BigDecimal.ZERO);
    mecanica.setHorasNecesariasRealizacion(BigDecimal.ZERO);
    mecanica.acumulaPrecioPiezasUtilizadas(BigDecimal.ZERO);
    assertDecimal("0", mecanica.getHorasNecesariasRealizacion());
    assertDecimal("0", mecanica.getPrecioPiezasUtilizadas());

    ReparacionChapaYPintura chapa = new ReparacionChapaYPintura("Aleta");
    chapa.acumulaPrecioPintura(BigDecimal.ZERO);
    chapa.acumulaPrecioChapa(BigDecimal.ZERO);
    assertDecimal("0", chapa.getPrecioPiezasUtilizadas());

    rechaza(ExcepcionValorNoValido.class,
        () -> mecanica.acumulaHorasNecesariasRealizacion(new BigDecimal("-1")));
    rechaza(ExcepcionValorNoValido.class,
        () -> mecanica.setHorasNecesariasRealizacion(new BigDecimal("-1")));
    rechaza(ExcepcionValorNoValido.class,
        () -> mecanica.acumulaPrecioPiezasUtilizadas(new BigDecimal("-0.01")));
    rechaza(ExcepcionValorNoValido.class,
        () -> chapa.acumulaPrecioPintura(new BigDecimal("-1")));
    rechaza(ExcepcionValorNoValido.class,
        () -> chapa.acumulaPrecioChapa(new BigDecimal("-1")));
  }

  private static void trabajoFinalizadoNoPuedeModificarseNiReabrirse() throws Exception {
    ReparacionMecanica mecanica = new ReparacionMecanica("Motor");
    mecanica.acumulaHorasNecesariasRealizacion(new BigDecimal("2.5"));
    mecanica.acumulaPrecioPiezasUtilizadas(new BigDecimal("10"));
    mecanica.setFinalizado(true);
    rechaza(ExcepcionTrabajoFinalizado.class,
        () -> mecanica.acumulaHorasNecesariasRealizacion(BigDecimal.ONE));
    rechaza(ExcepcionTrabajoFinalizado.class,
        () -> mecanica.setHorasNecesariasRealizacion(BigDecimal.ZERO));
    rechaza(ExcepcionTrabajoFinalizado.class,
        () -> mecanica.acumulaPrecioPiezasUtilizadas(BigDecimal.ZERO));
    rechaza(IllegalStateException.class, () -> mecanica.setFinalizado(false));
    assertDecimal("2.5", mecanica.getHorasNecesariasRealizacion());
    assertDecimal("10", mecanica.getPrecioPiezasUtilizadas());

    Revision revision = new Revision("ITV");
    revision.setEliminado(true);
    rechaza(ExcepcionTrabajoEliminado.class,
        () -> revision.acumulaHorasNecesariasRealizacion(BigDecimal.ONE));
    rechaza(ExcepcionTrabajoEliminado.class,
        () -> revision.setHorasNecesariasRealizacion(BigDecimal.ONE));
    rechaza(IllegalStateException.class, () -> revision.setFinalizado(true));
  }

  private static void identificadoresSonUnicosEInmutables() throws Exception {
    Revision revision = new Revision("ITV");
    revision.setId(5);
    ReparacionMecanica mecanica = new ReparacionMecanica("Motor");
    mecanica.setId(5);

    if (!revision.equals(mecanica) || !mecanica.equals(revision)
        || revision.hashCode() != mecanica.hashCode()) {
      throw new AssertionError("La identidad depende del ID, no del subtipo");
    }
    rechaza(ExcepcionValorNoValido.class, () -> revision.setId(6));

    Garaje garaje = new Garaje();
    garaje.registrarTrabajo(revision);
    rechaza(IllegalArgumentException.class, () -> garaje.registrarTrabajo(mecanica));
    rechaza(IllegalArgumentException.class,
        () -> garaje.registrarTrabajo(new Revision("Sin identificador")));
    rechaza(IllegalArgumentException.class, () -> garaje.setTrabajos(List.of(revision, mecanica)));
    if (garaje.getTrabajos().size() != 1) {
      throw new AssertionError("El registro duplicado no debe modificar el garaje");
    }
  }

  private static void seConservaElOrdenPorDescripcion() throws Exception {
    Revision zeta = new Revision("zeta");
    zeta.setId(1);
    Revision alfa = new Revision("Alfa");
    alfa.setId(2);
    Garaje garaje = new Garaje(List.of(zeta, alfa), java.time.LocalTime.of(7, 0),
        java.time.LocalTime.of(17, 0));
    if (garaje.getTrabajos().get(0) != alfa) {
      throw new AssertionError("Debe ordenar alfabéticamente sin distinguir mayúsculas");
    }
    zeta.setDescripcion("Aardvark");
    if (garaje.getTrabajos().get(0) != zeta) {
      throw new AssertionError("Una descripcion modificada no debe desordenar la consulta");
    }
    rechaza(UnsupportedOperationException.class, () -> garaje.getTrabajos().clear());
  }

  private static void contadorSeRecuperaDesdeElGaraje() throws Exception {
    Revision revision = new Revision("R");
    revision.setId(40);
    Garaje garaje = new Garaje();
    garaje.registrarTrabajo(revision);

    Contador recuperado = GestorPersistenciaGaraje.reconciliarContador(garaje, new Contador());
    if (recuperado.siguiente() != 41L) {
      throw new AssertionError("El contador no debe reutilizar un ID ya persistido");
    }
  }

  private static void csvConservaComillasYNeutralizaFormulas() {
    if (!"\"Cambio de \"\"pieza\"\"\"".equals(
        AplicacionGestionGarajeGUI.escaparCsv("Cambio de \"pieza\""))) {
      throw new AssertionError("Las comillas del CSV deben conservarse");
    }
    if (!"\"'=1+1\"".equals(AplicacionGestionGarajeGUI.escaparCsv("=1+1"))) {
      throw new AssertionError("El CSV debe neutralizar formulas");
    }
  }

  private static void assertDecimal(String esperado, BigDecimal actual) {
    if (new BigDecimal(esperado).compareTo(actual) != 0) {
      throw new AssertionError("Esperado " + esperado + ", recibido " + actual);
    }
  }

  @FunctionalInterface
  private interface Accion {
    void ejecutar() throws Exception;
  }

  private static void rechaza(Class<? extends Throwable> tipo, Accion accion) throws Exception {
    try {
      accion.ejecutar();
    } catch (Throwable error) {
      if (tipo.isInstance(error)) {
        return;
      }
      throw new AssertionError("Excepcion inesperada; se esperaba " + tipo.getSimpleName(), error);
    }
    throw new AssertionError("Se esperaba la excepcion " + tipo.getSimpleName());
  }
}
