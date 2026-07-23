/**
 *
 */

package garaje;

import java.util.Comparator;

/**
 * @author Usuario
 *
 */
public class ComparadorDescripcion implements Comparator<Trabajo> {

  public ComparadorDescripcion() {
  }

  @Override
  public int compare(Trabajo trabajo1, Trabajo trabajo2) {
    return String.CASE_INSENSITIVE_ORDER.compare(trabajo1.getDescripcion(), trabajo2.getDescripcion());
  }

}
