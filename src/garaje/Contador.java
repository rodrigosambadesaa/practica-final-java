
package garaje;

import java.io.Serializable;

import excepcionesgaraje.ExcepcionValorNoValido;

public class Contador implements Serializable {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  private long valor;

  public Contador() {
    valor = -1;
  }

  public long getValor() {
    return valor;
  }

  public void setValor(long valor) throws ExcepcionValorNoValido {
    if (valor >= -1) {
      this.valor = valor;
      return;
    }
    throw new ExcepcionValorNoValido("El contador no puede ser menor que -1");
  }

  public long siguiente() throws ExcepcionValorNoValido {
    if (valor == Long.MAX_VALUE) {
      throw new ExcepcionValorNoValido("Se ha agotado el rango de identificadores");
    }
    return ++valor;
  }

}
