package utilidades;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import garaje.Contador;
import garaje.Garaje;

public final class GestorPersistenciaGaraje {

    private static final String FICHERO_GARAJE = "garaje.bin";
    private static final String FICHERO_CONTADOR = "contador.bin";

    private GestorPersistenciaGaraje() {
        // Clase de utilidad
    }

    public static Garaje cargarGaraje() {
        try (ObjectInputStream entrada = new ObjectInputStream(new FileInputStream(FICHERO_GARAJE))) {
            Object objeto = entrada.readObject();
            if (objeto instanceof Garaje) {
                return (Garaje) objeto;
            }
        } catch (Exception e) {
            // Si no hay datos previos o hay formato incompatible, se inicializa vacio.
        }
        return new Garaje();
    }

    public static Contador cargarContador() {
        try (ObjectInputStream entrada = new ObjectInputStream(new FileInputStream(FICHERO_CONTADOR))) {
            Object objeto = entrada.readObject();
            if (objeto instanceof Contador) {
                return (Contador) objeto;
            }
        } catch (Exception e) {
            // Si no hay datos previos o hay formato incompatible, se inicializa contador
            // nuevo.
        }
        return new Contador();
    }

    public static void guardar(Garaje garaje, Contador contador) throws IOException {
        try (ObjectOutputStream salidaGaraje = new ObjectOutputStream(new FileOutputStream(FICHERO_GARAJE, false));
                ObjectOutputStream salidaContador = new ObjectOutputStream(
                        new FileOutputStream(FICHERO_CONTADOR, false))) {
            salidaGaraje.writeObject(garaje);
            salidaContador.writeObject(contador);
        }
    }
}
