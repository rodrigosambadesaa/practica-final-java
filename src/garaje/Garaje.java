package garaje;

import java.io.Serializable;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import excepcionesgaraje.ExcepcionGarajeLleno;
import excepcionesgaraje.ExcepcionTrabajoNoEncontrado;

public class Garaje implements Serializable {

  private static final long serialVersionUID = 1L;
  private static final int MAX_JEFES = 2;
  private static final int MAX_MECANICOS = 10;
  private static final DateTimeFormatter FORMATO_HORA = DateTimeFormatter.ofPattern("HH:mm");

  private List<Trabajo> trabajos;
  private List<Empleado> empleados;
  private LocalTime horaApertura;
  private LocalTime horaCierre;

  public Garaje() {
    this(List.of(), List.of(), LocalTime.of(7, 0), LocalTime.of(17, 0));
  }

  public Garaje(List<Trabajo> trabajos, LocalTime horaApertura, LocalTime horaCierre) {
    this(trabajos, List.of(), horaApertura, horaCierre);
  }

  public Garaje(List<Trabajo> trabajos, List<Empleado> empleados, LocalTime horaApertura,
      LocalTime horaCierre) {
    setTrabajos(trabajos);
    setEmpleados(empleados);
    establecerHorario(horaApertura, horaCierre);
  }

  public Trabajo buscarTrabajo(long id) {
    return trabajos.stream()
        .filter(trabajo -> trabajo.getId() == id)
        .findFirst()
        .orElse(null);
  }

  public Trabajo obtenerTrabajo(long id) throws ExcepcionTrabajoNoEncontrado {
    Trabajo trabajo = buscarTrabajo(id);
    if (trabajo == null) {
      throw new ExcepcionTrabajoNoEncontrado("No hay ningun trabajo con ID: " + id);
    }
    return trabajo;
  }

  public void eliminarTrabajo(long id) throws ExcepcionTrabajoNoEncontrado {
    obtenerTrabajo(id).setEliminado(true);
  }

  public void registrarTrabajo(Trabajo trabajo) {
    Objects.requireNonNull(trabajo, "El trabajo no puede ser nulo");
    if (trabajo.getId() < 0) {
      throw new IllegalArgumentException("El trabajo debe tener un identificador asignado");
    }
    if (existeTrabajo(trabajo.getId())) {
      throw new IllegalArgumentException("Ya existe un trabajo con ID: " + trabajo.getId());
    }
    trabajos.add(trabajo);
    trabajos.sort(new ComparadorDescripcion());
  }

  public boolean existeTrabajo(long id) {
    return buscarTrabajo(id) != null;
  }

  public List<Trabajo> getTrabajos() {
    List<Trabajo> ordenados = new ArrayList<>(trabajos);
    ordenados.sort(new ComparadorDescripcion());
    return List.copyOf(ordenados);
  }

  public void setTrabajos(List<Trabajo> trabajos) {
    List<Trabajo> copia = new ArrayList<>(Objects.requireNonNullElse(trabajos, List.of()));
    java.util.Set<Long> identificadores = new java.util.HashSet<>();
    for (Trabajo trabajo : copia) {
      Objects.requireNonNull(trabajo, "La lista no puede contener trabajos nulos");
      if (trabajo.getId() < 0 || !identificadores.add(trabajo.getId())) {
        throw new IllegalArgumentException("Los identificadores deben estar asignados y ser unicos");
      }
    }
    copia.sort(new ComparadorDescripcion());
    this.trabajos = copia;
  }

  public List<Empleado> getEmpleados() {
    return List.copyOf(empleados);
  }

  public void setEmpleados(List<Empleado> empleados) {
    this.empleados = new ArrayList<>(Objects.requireNonNullElse(empleados, List.of()));
  }

  public void anhadirEmpleado(Empleado empleado) throws ExcepcionGarajeLleno {
    Objects.requireNonNull(empleado, "El empleado no puede ser nulo");
    long empleadosDelTipo = empleados.stream()
        .filter(existente -> existente.getClass().equals(empleado.getClass()))
        .count();
    int limite = empleado instanceof Jefe ? MAX_JEFES : MAX_MECANICOS;
    if (empleadosDelTipo >= limite) {
      throw new ExcepcionGarajeLleno("Se ha alcanzado el limite de " + limite + " empleados");
    }
    empleados.add(empleado);
  }

  public LocalTime getHoraApertura() {
    return horaApertura;
  }

  public void setHoraApertura(LocalTime horaApertura) {
    establecerHorario(horaApertura, this.horaCierre);
  }

  public LocalTime getHoraCierre() {
    return horaCierre;
  }

  public void setHoraCierre(LocalTime horaCierre) {
    establecerHorario(this.horaApertura, horaCierre);
  }

  public void establecerHorario(LocalTime horaApertura, LocalTime horaCierre) {
    Objects.requireNonNull(horaApertura, "La hora de apertura no puede ser nula");
    Objects.requireNonNull(horaCierre, "La hora de cierre no puede ser nula");
    if (!horaApertura.isBefore(horaCierre)) {
      throw new IllegalArgumentException("La hora de apertura debe ser anterior a la de cierre");
    }
    this.horaApertura = horaApertura;
    this.horaCierre = horaCierre;
  }

  public boolean abierto() {
    return abierto(LocalTime.now());
  }

  public boolean abierto(LocalTime ahora) {
    Objects.requireNonNull(ahora, "La hora actual no puede ser nula");
    return !ahora.isBefore(horaApertura) && ahora.isBefore(horaCierre);
  }

  public String getDescripcionHorario() {
    return horaApertura.format(FORMATO_HORA) + "-" + horaCierre.format(FORMATO_HORA);
  }

  public static int getMaxMecanicos() {
    return MAX_MECANICOS;
  }

  @Override
  public String toString() {
    if (trabajos.isEmpty()) {
      return "TRABAJOS\n(No hay trabajos registrados)";
    }
    StringBuilder resultado = new StringBuilder("TRABAJOS");
    getTrabajos().forEach(trabajo -> resultado.append("\n--------------------\n").append(trabajo));
    return resultado.toString();
  }
}
