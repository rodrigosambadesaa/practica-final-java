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
    trabajos.add(Objects.requireNonNull(trabajo, "El trabajo no puede ser nulo"));
    trabajos.sort(new ComparadorDescripcion());
  }

  public boolean existeTrabajo(long id) {
    return buscarTrabajo(id) != null;
  }

  public List<Trabajo> getTrabajos() {
    return List.copyOf(trabajos);
  }

  public void setTrabajos(List<Trabajo> trabajos) {
    this.trabajos = new ArrayList<>(Objects.requireNonNullElse(trabajos, List.of()));
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
    trabajos.forEach(trabajo -> resultado.append("\n--------------------\n").append(trabajo));
    return resultado.toString();
  }
}
