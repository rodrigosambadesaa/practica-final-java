package garaje;

import java.math.BigDecimal;
import java.util.List;

import excepcionesgaraje.ExcepcionValorNoValido;

public class Jefe extends Empleado {

  private static final long serialVersionUID = 1L;

  public Jefe() {
    super();
  }

  public Jefe(String codigo, String nombre, String apellidos, BigDecimal salario, BigDecimal comision,
      boolean trabajaActualmente, List<PeriodoLaboral> periodosLaborales) throws ExcepcionValorNoValido {
    super(codigo, nombre, apellidos, salario, comision, trabajaActualmente, periodosLaborales);
  }
}
