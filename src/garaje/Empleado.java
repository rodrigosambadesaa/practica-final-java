package garaje;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import excepcionesgaraje.ExcepcionValorNoValido;

public class Empleado implements Serializable {

  private static final long serialVersionUID = 1L;

  private String codigo;
  private String nombre;
  private String apellidos;
  private BigDecimal salario = BigDecimal.ZERO;
  private BigDecimal comision = BigDecimal.ZERO;
  private boolean trabajaActualmente;
  private List<PeriodoLaboral> periodosLaborales = new ArrayList<>();

  public Empleado() {
  }

  public Empleado(String codigo, String nombre, String apellidos, BigDecimal salario, BigDecimal comision,
      boolean trabajaActualmente, List<PeriodoLaboral> periodosLaborales) throws ExcepcionValorNoValido {
    setCodigo(codigo);
    setNombre(nombre);
    setApellidos(apellidos);
    setSalario(salario);
    setComision(comision);
    this.trabajaActualmente = trabajaActualmente;
    setPeriodosLaborales(periodosLaborales);
  }

  public String getCodigo() {
    return codigo;
  }

  public void setCodigo(String codigo) {
    this.codigo = textoObligatorio(codigo, "codigo");
  }

  public String getNombre() {
    return nombre;
  }

  public void setNombre(String nombre) {
    this.nombre = textoObligatorio(nombre, "nombre");
  }

  public String getApellidos() {
    return apellidos;
  }

  public void setApellidos(String apellidos) {
    this.apellidos = textoObligatorio(apellidos, "apellidos");
  }

  public BigDecimal getSalario() {
    return salario;
  }

  public void setSalario(BigDecimal salario) throws ExcepcionValorNoValido {
    if (salario == null || salario.signum() <= 0) {
      throw new ExcepcionValorNoValido("Salario no valido");
    }
    this.salario = salario;
  }

  public BigDecimal getComision() {
    return comision;
  }

  public void setComision(BigDecimal comision) throws ExcepcionValorNoValido {
    if (comision == null || comision.signum() < 0) {
      throw new ExcepcionValorNoValido("Comision no valida");
    }
    this.comision = comision;
  }

  public boolean getTrabajaActualmente() {
    return trabajaActualmente;
  }

  public void setTrabajaActualmente(boolean trabajaActualmente) {
    this.trabajaActualmente = trabajaActualmente;
  }

  public List<PeriodoLaboral> getPeriodosLaborales() {
    return List.copyOf(periodosLaborales);
  }

  public void setPeriodosLaborales(List<PeriodoLaboral> periodosLaborales) {
    this.periodosLaborales = new ArrayList<>(Objects.requireNonNullElse(periodosLaborales, List.of()));
  }

  private static String textoObligatorio(String valor, String campo) {
    String normalizado = Objects.requireNonNull(valor, "El " + campo + " no puede ser nulo").trim();
    if (normalizado.isEmpty()) {
      throw new IllegalArgumentException("El " + campo + " no puede estar vacio");
    }
    return normalizado;
  }
}
