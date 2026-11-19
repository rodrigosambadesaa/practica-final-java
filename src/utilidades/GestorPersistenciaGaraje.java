package utilidades;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Objects;

import excepcionesgaraje.ExcepcionValorNoValido;
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

    /**
     * Repara el contador si falta su fichero o va retrasado respecto a los trabajos
     * guardados. Evita reutilizar un ID despues de reiniciar la aplicacion.
     */
    public static Contador reconciliarContador(Garaje garaje, Contador contador) {
        Objects.requireNonNull(garaje, "El garaje no puede ser nulo");
        Objects.requireNonNull(contador, "El contador no puede ser nulo");
        long maximo = garaje.getTrabajos().stream().mapToLong(trabajo -> trabajo.getId()).max().orElse(-1L);
        if (contador.getValor() < maximo) {
            try {
                contador.setValor(maximo);
            } catch (ExcepcionValorNoValido e) {
                throw new IllegalStateException("El garaje contiene un ID invalido", e);
            }
        }
        return contador;
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
