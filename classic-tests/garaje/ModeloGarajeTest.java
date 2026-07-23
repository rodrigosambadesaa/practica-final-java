package garaje;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

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
