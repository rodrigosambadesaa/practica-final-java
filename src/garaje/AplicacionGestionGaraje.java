
/**
 * @author Rodrigo Sambade Sa�
 *
 */

package garaje;

import java.awt.GraphicsEnvironment;

public class AplicacionGestionGaraje {

    public static void main(String[] args) {
        if (contieneArgumento(args, "--consola")) {
            AplicacionGestionGarajeConsola.iniciar();
            return;
        }

        if (GraphicsEnvironment.isHeadless()) {
            System.out.println("Entorno sin interfaz grafica. Iniciando modo consola.");
            AplicacionGestionGarajeConsola.iniciar();
            return;
        }

        AplicacionGestionGarajeGUI.lanzar();
    }

    private static boolean contieneArgumento(String[] args, String esperado) {
        if (args == null || esperado == null) {
            return false;
        }
        for (String arg : args) {
            if (esperado.equalsIgnoreCase(arg)) {
                return true;
            }
        }
        return false;
    }
}
