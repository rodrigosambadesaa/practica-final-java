package garaje;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Scanner;

import utilidades.GestorPersistenciaGaraje;

public final class AplicacionGestionGarajeConsola {

  private final Scanner entrada;
  private final Garaje garaje;
  private final Contador contador;

  private AplicacionGestionGarajeConsola(Scanner entrada, Garaje garaje, Contador contador) {
    this.entrada = entrada;
    this.garaje = garaje;
    this.contador = contador;
  }

  public static void iniciar() {
    AplicacionGestionGarajeConsola aplicacion = new AplicacionGestionGarajeConsola(
        new Scanner(System.in),
        GestorPersistenciaGaraje.cargarGaraje(),
        GestorPersistenciaGaraje.cargarContador());
    aplicacion.ejecutar();
  }

  private void ejecutar() {
    System.out.println("GESTION DEL GARAJE");
    System.out.println("Horario configurado: " + garaje.getDescripcionHorario());
    boolean continuar = true;
    while (continuar) {
      mostrarMenu();
      try {
        continuar = procesarOpcion(leerEntero("Opcion: "));
      } catch (Exception excepcion) {
        System.out.println("Error: " + excepcion.getMessage());
      }
    }
    guardar();
  }

  private void mostrarMenu() {
    System.out.println("""

        1. Registrar trabajo
        2. Anadir horas
        3. Anadir material
        4. Finalizar trabajo
        5. Mostrar trabajos
        6. Eliminar trabajo
        7. Guardar y salir
        """);
  }

  private boolean procesarOpcion(int opcion) throws Exception {
    return switch (opcion) {
      case 1 -> {
        registrarTrabajo();
        yield true;
      }
      case 2 -> {
        anadirHoras();
        yield true;
      }
      case 3 -> {
        anadirMaterial();
        yield true;
      }
      case 4 -> {
        obtenerTrabajo().setFinalizado(true);
        yield true;
      }
      case 5 -> {
        System.out.println(garaje);
        yield true;
      }
      case 6 -> {
        garaje.eliminarTrabajo(leerLong("ID: "));
        yield true;
      }
      case 7 -> false;
      default -> throw new IllegalArgumentException("Opcion no valida");
    };
  }

  private void registrarTrabajo() throws Exception {
    int tipo = leerEntero("Tipo (1=mecanica, 2=chapa y pintura, 3=revision): ");
    String descripcion = leerTexto("Descripcion: ");
    Trabajo trabajo = switch (tipo) {
      case 1 -> new ReparacionMecanica(descripcion);
      case 2 -> new ReparacionChapaYPintura(descripcion);
      case 3 -> new Revision(descripcion);
      default -> throw new IllegalArgumentException("Tipo de trabajo no valido");
    };
    trabajo.setId(contador.siguiente());
    garaje.registrarTrabajo(trabajo);
    System.out.println("Trabajo registrado con ID " + trabajo.getId());
  }

  private void anadirHoras() throws Exception {
    obtenerTrabajo().acumulaHorasNecesariasRealizacion(leerDecimal("Horas: "));
  }

  private void anadirMaterial() throws Exception {
    Trabajo trabajo = obtenerTrabajo();
    BigDecimal importe = leerDecimal("Importe: ");
    if (trabajo instanceof ReparacionMecanica mecanica) {
      mecanica.acumulaPrecioPiezasUtilizadas(importe);
      return;
    }
    if (trabajo instanceof ReparacionChapaYPintura chapa) {
      String material = leerTexto("Material (chapa/pintura): ");
      if ("chapa".equalsIgnoreCase(material)) {
        chapa.acumulaPrecioChapa(importe);
      } else if ("pintura".equalsIgnoreCase(material)) {
        chapa.acumulaPrecioPintura(importe);
      } else {
        throw new IllegalArgumentException("Material no valido");
      }
      return;
    }
    throw new IllegalArgumentException("Una revision no admite material");
  }

  private Trabajo obtenerTrabajo() throws Exception {
    return garaje.obtenerTrabajo(leerLong("ID: "));
  }

  private int leerEntero(String mensaje) {
    return Integer.parseInt(leerTexto(mensaje));
  }

  private long leerLong(String mensaje) {
    return Long.parseLong(leerTexto(mensaje));
  }

  private BigDecimal leerDecimal(String mensaje) {
    return new BigDecimal(leerTexto(mensaje));
  }

  private String leerTexto(String mensaje) {
    System.out.print(mensaje);
    return entrada.nextLine().trim();
  }

  private void guardar() {
    try {
      GestorPersistenciaGaraje.guardar(garaje, contador);
      System.out.println("Datos guardados");
    } catch (IOException excepcion) {
      System.err.println("No se pudieron guardar los datos: " + excepcion.getMessage());
    }
  }
}
