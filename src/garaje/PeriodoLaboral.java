package garaje;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

public record PeriodoLaboral(LocalDate inicio, LocalDate fin) implements Serializable {

  private static final long serialVersionUID = 1L;

  public PeriodoLaboral {
    Objects.requireNonNull(inicio, "La fecha de inicio no puede ser nula");
    if (fin != null && fin.isBefore(inicio)) {
      throw new IllegalArgumentException("La fecha de fin no puede ser anterior a la de inicio");
    }
  }

  public boolean estaActivo() {
    return fin == null;
  }
}
