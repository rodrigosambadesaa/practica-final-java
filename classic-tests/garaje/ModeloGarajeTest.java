package garaje;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import excepcionesgaraje.ExcepcionValorNoValido;

public final class ModeloGarajeTest {

  private ModeloGarajeTest() {
  }

  public static void main(String[] args) throws Exception {
    pruebaIdentificadoresLong();
    pruebaTiposPrimitivosYTextoInmutable();
    pruebaImportesExactos();
    pruebaHorarioConLocalTime();
    pruebaHorarioInvalido();
    pruebaPeriodosLaborales();
    pruebaIntegridadDeEstadosYTipos();
    pruebaOrdenacionEIdentificadoresUnicos();
    pruebaImportesCeroYNegativos();
    System.out.println("Pruebas del modelo clasico: OK");
  }

  private static void pruebaIdentificadoresLong() throws Exception {
    Contador contador = new Contador();
    assertEquals(0L, contador.siguiente(), "El primer identificador debe ser cero");
    assertEquals(1L, contador.siguiente(), "El contador debe avanzar una unidad");

    Revision revision = new Revision("Pre-ITV");
    revision.setId(contador.siguiente());
    assertEquals(2L, revision.getId(), "El trabajo debe conservar el identificador long");
  }

  private static void pruebaTiposPrimitivosYTextoInmutable() throws Exception {
    Revision revision = new Revision("  Revision anual  ");
    revision.setId(10);

    assertEquals("Revision anual", revision.getDescripcion(), "La descripcion debe normalizarse");
    assertFalse(revision.getFinalizado(), "El estado inicial no puede ser nulo");
    assertFalse(revision.getEliminado(), "El estado inicial no puede ser nulo");

    Revision mismaRevision = new Revision("Otra instancia");
    mismaRevision.setId(10);
    assertTrue(revision.equals(mismaRevision), "La igualdad debe basarse en el identificador");
    assertEquals(revision.hashCode(), mismaRevision.hashCode(), "equals y hashCode deben ser coherentes");
  }

  private static void pruebaImportesExactos() throws Exception {
    ReparacionMecanica mecanica = new ReparacionMecanica("Cambio de correa");
    mecanica.acumulaHorasNecesariasRealizacion(new BigDecimal("2"));
    mecanica.acumulaPrecioPiezasUtilizadas(new BigDecimal("100"));
    assertBigDecimal("170", mecanica.precioACobrar(), "Precio mecanica");

    ReparacionChapaYPintura chapa = new ReparacionChapaYPintura("Aleta");
    chapa.acumulaHorasNecesariasRealizacion(new BigDecimal("3"));
    chapa.acumulaPrecioChapa(new BigDecimal("100"));
    chapa.acumulaPrecioPintura(new BigDecimal("50"));
    assertBigDecimal("285", chapa.precioACobrar(), "Precio chapa y pintura");
  }

  private static void pruebaHorarioConLocalTime() {
    Garaje garaje = new Garaje();
    assertTrue(garaje.abierto(LocalTime.of(7, 0)), "Debe abrir a las 07:00");
    assertFalse(garaje.abierto(LocalTime.of(17, 0)), "Debe cerrar a las 17:00");
    garaje.establecerHorario(LocalTime.of(8, 30), LocalTime.of(16, 30));
    assertEquals("08:30-16:30", garaje.getDescripcionHorario(), "Debe describir el horario configurado");
  }

  private static void pruebaHorarioInvalido() {
    try {
      new Garaje().establecerHorario(LocalTime.of(17, 0), LocalTime.of(7, 0));
      throw new AssertionError("Un horario invertido debe rechazarse");
    } catch (IllegalArgumentException expected) {
      // Comportamiento esperado.
    }
  }

  private static void pruebaPeriodosLaborales() {
    PeriodoLaboral activo = new PeriodoLaboral(LocalDate.of(2026, 1, 1), null);
    assertTrue(activo.estaActivo(), "Un periodo sin fecha final debe estar activo");

    try {
      new PeriodoLaboral(LocalDate.of(2026, 2, 1), LocalDate.of(2026, 1, 1));
      throw new AssertionError("Una fecha final anterior debe rechazarse");
    } catch (IllegalArgumentException expected) {
      // Comportamiento esperado.
    }
  }

  private static void pruebaIntegridadDeEstadosYTipos() throws Exception {
    Revision revision = new Revision("Revision");
    revision.setId(20);
    revision.setHorasNecesariasRealizacion(new BigDecimal("2"));
    revision.setFinalizado(true);
    assertThrows(() -> revision.setHorasNecesariasRealizacion(BigDecimal.TEN),
        "No se deben modificar horas de un trabajo finalizado usando el setter");
    assertThrows(() -> revision.setFinalizado(false), "No se deben reabrir trabajos terminados");
    assertThrows(() -> revision.setId(22), "El identificador no puede reasignarse");

    ReparacionMecanica mecanica = new ReparacionMecanica("Mecanica");
    mecanica.setId(30);
    mecanica.setFinalizado(true);
    assertThrows(() -> mecanica.acumulaPrecioPiezasUtilizadas(BigDecimal.ONE),
        "No se debe modificar el material de una reparacion terminada");
    mecanica.setEliminado(true);
    assertThrows(() -> mecanica.setEliminado(false), "No se debe deshacer el borrado logico");

    Revision mismoId = new Revision("Otro tipo");
    mismoId.setId(30);
    assertTrue(mismoId.equals(mecanica), "La igualdad de trabajos depende de su ID, no de su subtipo");
    assertEquals(mismoId.hashCode(), mecanica.hashCode(), "Igualdad y hash deben ser consistentes");
  }

  private static void pruebaOrdenacionEIdentificadoresUnicos() throws Exception {
    Garaje garaje = new Garaje();
    Revision zeta = new Revision("Zeta");
    zeta.setId(0);
    Revision alfa = new Revision("Alfa");
    alfa.setId(1);
    garaje.registrarTrabajo(zeta);
    garaje.registrarTrabajo(alfa);
    assertEquals(alfa, garaje.getTrabajos().getFirst(), "El listado debe estar ordenado");
    zeta.setDescripcion("Aardvark");
    assertEquals(zeta, garaje.getTrabajos().getFirst(), "Debe reordenarse tras cambiar la descripcion");
    assertThrows(() -> garaje.registrarTrabajo(zeta), "No se permiten IDs duplicados");
    assertThrows(() -> garaje.setTrabajos(List.of(zeta, zeta)),
        "La carga de una lista tambien debe validar IDs duplicados");
    garaje.setTrabajos(List.of(alfa, zeta));
    assertEquals(zeta, garaje.getTrabajos().getFirst(), "setTrabajos debe ordenar los elementos");
  }

  private static void pruebaImportesCeroYNegativos() throws Exception {
    ReparacionChapaYPintura chapa = new ReparacionChapaYPintura("Chapa");
    chapa.acumulaPrecioChapa(BigDecimal.ZERO);
    chapa.acumulaPrecioPintura(BigDecimal.ZERO);
    chapa.acumulaPrecioPiezasUtilizadas(BigDecimal.ZERO);
    assertBigDecimal("0", chapa.getPrecioPiezasUtilizadas(), "Se admiten materiales de coste cero");
    assertThrows(() -> chapa.acumulaPrecioChapa(new BigDecimal("-0.01")),
        "Se deben rechazar importes de material negativos");
    assertThrows(() -> chapa.acumulaHorasNecesariasRealizacion(new BigDecimal("-1")),
        "Se deben rechazar horas negativas");
  }

  @FunctionalInterface
  private interface Operacion {
    void ejecutar() throws Exception;
  }

  private static void assertThrows(Operacion operacion, String mensaje) throws Exception {
    try {
      operacion.ejecutar();
    } catch (Exception expected) {
      return;
    }
    throw new AssertionError(mensaje);
  }

  private static void assertBigDecimal(String esperado, BigDecimal actual, String mensaje) {
    if (actual.compareTo(new BigDecimal(esperado)) != 0) {
      throw new AssertionError(mensaje + ": esperado=" + esperado + ", actual=" + actual);
    }
  }

  private static void assertTrue(boolean valor, String mensaje) {
    if (!valor) {
      throw new AssertionError(mensaje);
    }
  }

  private static void assertFalse(boolean valor, String mensaje) {
    assertTrue(!valor, mensaje);
  }

  private static void assertEquals(Object esperado, Object actual, String mensaje) {
    if (!esperado.equals(actual)) {
      throw new AssertionError(mensaje + ": esperado=" + esperado + ", actual=" + actual);
    }
  }
}
