package garaje;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;

import excepcionesgaraje.ExcepcionTrabajoNoEncontrado;
import utilidades.GestorPersistenciaGaraje;

public class AplicacionGestionGarajeGUI extends JFrame {

    private static final long serialVersionUID = 1L;

    private final Garaje garaje;

    private final Contador contador;

    private final DefaultListModel<String> modeloLista;

    private final JList<String> listaTrabajos;

    private final JTextField campoFiltro;

    private final JLabel etiquetaResumen;

    private List<Trabajo> snapshotTrabajos;

    public AplicacionGestionGarajeGUI() {
        super("Gestion de Garaje - GUI");
        this.garaje = GestorPersistenciaGaraje.cargarGaraje();
        this.contador = GestorPersistenciaGaraje.cargarContador();
        this.modeloLista = new DefaultListModel<String>();
        this.listaTrabajos = new JList<String>(modeloLista);
        this.campoFiltro = new JTextField(24);
        this.etiquetaResumen = new JLabel(" ");
        this.snapshotTrabajos = new ArrayList<>();

        construirInterfaz();
        refrescarLista();
    }

    public static void lanzar() {
        SwingUtilities.invokeLater(() -> {
            AplicacionGestionGarajeGUI ventana = new AplicacionGestionGarajeGUI();
            ventana.setVisible(true);
        });
    }

    private void construirInterfaz() {
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(new Dimension(980, 520));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(8, 8));

        JPanel panelCabecera = new JPanel(new BorderLayout(8, 8));
        panelCabecera.setBorder(BorderFactory.createEmptyBorder(8, 8, 0, 8));
        panelCabecera.add(new JLabel("Trabajos registrados"), BorderLayout.WEST);

        JPanel panelFiltros = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton botonFiltrar = new JButton("Filtrar");
        JButton botonLimpiarFiltro = new JButton("Limpiar");
        panelFiltros.add(new JLabel("Buscar:"));
        panelFiltros.add(campoFiltro);
        panelFiltros.add(botonFiltrar);
        panelFiltros.add(botonLimpiarFiltro);
        panelCabecera.add(panelFiltros, BorderLayout.EAST);

        listaTrabajos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scroll = new JScrollPane(listaTrabajos);
        scroll.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelBotones.setBorder(BorderFactory.createEmptyBorder(0, 8, 8, 8));

        JButton botonNuevo = new JButton("Nuevo trabajo");
        JButton botonHoras = new JButton("Anadir horas");
        JButton botonMaterial = new JButton("Anadir material");
        JButton botonFinalizar = new JButton("Finalizar");
        JButton botonEliminar = new JButton("Eliminar");
        JButton botonGuardar = new JButton("Guardar");
        JButton botonExportarCsv = new JButton("Exportar CSV");
        JButton botonRefrescar = new JButton("Refrescar");

        botonNuevo.addActionListener(e -> crearTrabajoDialogo());
        botonHoras.addActionListener(e -> anadirHorasDialogo());
        botonMaterial.addActionListener(e -> anadirMaterialDialogo());
        botonFinalizar.addActionListener(e -> finalizarTrabajoSeleccionado());
        botonEliminar.addActionListener(e -> eliminarTrabajoSeleccionado());
        botonGuardar.addActionListener(e -> guardarDatos());
        botonExportarCsv.addActionListener(e -> exportarCsvDialogo());
        botonRefrescar.addActionListener(e -> refrescarLista());
        botonFiltrar.addActionListener(e -> refrescarLista());
        botonLimpiarFiltro.addActionListener(e -> {
            campoFiltro.setText("");
            refrescarLista();
        });

        panelBotones.add(botonNuevo);
        panelBotones.add(botonHoras);
        panelBotones.add(botonMaterial);
        panelBotones.add(botonFinalizar);
        panelBotones.add(botonEliminar);
        panelBotones.add(botonGuardar);
        panelBotones.add(botonExportarCsv);
        panelBotones.add(botonRefrescar);

        add(panelCabecera, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
        add(etiquetaResumen, BorderLayout.WEST);
        add(panelBotones, BorderLayout.SOUTH);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                guardarDatos();
            }
        });
    }

    private void crearTrabajoDialogo() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        JComboBox<String> tipos = new JComboBox<String>(
                new String[] { "Reparacion mecanica", "Reparacion chapa y pintura", "Revision" });
        JTextField descripcion = new JTextField();

        JPanel fila1 = new JPanel(new BorderLayout(8, 8));
        fila1.add(new JLabel("Tipo"), BorderLayout.WEST);
        fila1.add(tipos, BorderLayout.CENTER);

        JPanel fila2 = new JPanel(new BorderLayout(8, 8));
        fila2.add(new JLabel("Descripcion"), BorderLayout.WEST);
        fila2.add(descripcion, BorderLayout.CENTER);

        panel.add(fila1, BorderLayout.NORTH);
        panel.add(fila2, BorderLayout.CENTER);

        int resultado = JOptionPane.showConfirmDialog(this, panel, "Nuevo trabajo", JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);

        if (resultado != JOptionPane.OK_OPTION) {
            return;
        }

        String textoDescripcion = descripcion.getText() == null ? "" : descripcion.getText().trim();
        if (textoDescripcion.isEmpty()) {
            mostrarError("La descripcion no puede estar vacia");
            return;
        }

        Trabajo nuevo;
        int tipo = tipos.getSelectedIndex();
        if (tipo == 0) {
            nuevo = new ReparacionMecanica(textoDescripcion);
        } else if (tipo == 1) {
            nuevo = new ReparacionChapaYPintura(textoDescripcion);
        } else {
            nuevo = new Revision(textoDescripcion);
        }

        try {
            long idAsignado = contador.siguiente();
            nuevo.setId(idAsignado);
            garaje.registrarTrabajo(nuevo);
            refrescarLista();
            mostrarInfo("Trabajo registrado con ID " + idAsignado);
        } catch (Exception e) {
            mostrarError("No se pudo registrar el trabajo: " + e.getMessage());
        }
    }

    private void anadirHorasDialogo() {
        Trabajo trabajo = getTrabajoSeleccionado();
        if (trabajo == null) {
            return;
        }

        String entrada = JOptionPane.showInputDialog(this, "Horas a anadir", "0");
        if (entrada == null) {
            return;
        }

        try {
            BigDecimal horas = new BigDecimal(entrada.trim());
            trabajo.acumulaHorasNecesariasRealizacion(horas);
            refrescarLista();
        } catch (Exception e) {
            mostrarError("No se pudieron anadir horas: " + e.getMessage());
        }
    }

    private void anadirMaterialDialogo() {
        Trabajo trabajo = getTrabajoSeleccionado();
        if (trabajo == null) {
            return;
        }

        if (!(trabajo instanceof Reparacion)) {
            mostrarError("El trabajo seleccionado no es una reparacion");
            return;
        }

        try {
            if (trabajo instanceof ReparacionMecanica) {
                String entrada = JOptionPane.showInputDialog(this, "Importe piezas", "0");
                if (entrada == null) {
                    return;
                }
                BigDecimal importe = new BigDecimal(entrada.trim());
                ((ReparacionMecanica) trabajo).acumulaPrecioPiezasUtilizadas(importe);
            } else if (trabajo instanceof ReparacionChapaYPintura) {
                ReparacionChapaYPintura chapa = (ReparacionChapaYPintura) trabajo;
                Object seleccion = JOptionPane.showInputDialog(this, "Tipo de material", "Material",
                        JOptionPane.QUESTION_MESSAGE, null, new String[] { "chapa", "pintura" }, "chapa");
                if (seleccion == null) {
                    return;
                }

                String entrada = JOptionPane.showInputDialog(this, "Importe", "0");
                if (entrada == null) {
                    return;
                }
                BigDecimal importe = new BigDecimal(entrada.trim());
                if ("chapa".equals(seleccion.toString())) {
                    chapa.acumulaPrecioChapa(importe);
                } else {
                    chapa.acumulaPrecioPintura(importe);
                }
            }

            refrescarLista();
        } catch (Exception e) {
            mostrarError("No se pudo anadir material: " + e.getMessage());
        }
    }

    private void finalizarTrabajoSeleccionado() {
        Trabajo trabajo = getTrabajoSeleccionado();
        if (trabajo == null) {
            return;
        }

        if (trabajo.getEliminado()) {
            mostrarError("No se puede finalizar un trabajo eliminado");
            return;
        }

        trabajo.setFinalizado(true);
        refrescarLista();
    }

    private void eliminarTrabajoSeleccionado() {
        Trabajo trabajo = getTrabajoSeleccionado();
        if (trabajo == null) {
            return;
        }

        try {
            garaje.eliminarTrabajo(trabajo.getId());
            refrescarLista();
        } catch (ExcepcionTrabajoNoEncontrado e) {
            mostrarError(e.getMessage());
        }
    }

    private Trabajo getTrabajoSeleccionado() {
        int indice = listaTrabajos.getSelectedIndex();
        if (indice < 0 || indice >= snapshotTrabajos.size()) {
            mostrarError("Selecciona primero un trabajo");
            return null;
        }
        return snapshotTrabajos.get(indice);
    }

    private void refrescarLista() {
        modeloLista.clear();
        snapshotTrabajos = new ArrayList<>();
        String filtro = campoFiltro.getText() == null ? "" : campoFiltro.getText().trim().toLowerCase(Locale.ROOT);

        for (Trabajo trabajo : garaje.getTrabajos()) {
            if (!filtro.isEmpty()) {
                String descripcion = trabajo.getDescripcion() == null ? "" : trabajo.getDescripcion().toString();
                String tipo = trabajo.getClass().getSimpleName();
                if (!descripcion.toLowerCase(Locale.ROOT).contains(filtro)
                        && !tipo.toLowerCase(Locale.ROOT).contains(filtro)
                        && !String.valueOf(trabajo.getId()).contains(filtro)) {
                    continue;
                }
            }
            snapshotTrabajos.add(trabajo);
            modeloLista.addElement(formatearTrabajo(trabajo));
        }
        actualizarResumen();
    }

    private String formatearTrabajo(Trabajo trabajo) {
        String tipo = trabajo.getClass().getSimpleName();
        String descripcion = trabajo.getDescripcion() == null ? "" : trabajo.getDescripcion().toString();
        String estado;

        if (trabajo.getEliminado()) {
            estado = "eliminado";
        } else if (trabajo.getFinalizado()) {
            estado = "finalizado";
        } else {
            estado = "activo";
        }

        String precio;
        try {
            precio = trabajo.precioACobrar().toPlainString();
        } catch (Exception e) {
            precio = "N/A";
        }

        return "ID=" + trabajo.getId() + " | " + tipo + " | " + descripcion + " | horas="
                + trabajo.getHorasNecesariasRealizacion() + " | plazo=" + trabajo.plazoMaximo() + " dias | estado="
                + estado + " | precio=" + precio;
    }

    private void actualizarResumen() {
        int total = snapshotTrabajos.size();
        int activos = 0;
        int finalizados = 0;
        int eliminados = 0;
        BigDecimal totalFacturacion = BigDecimal.ZERO;

        for (Trabajo trabajo : snapshotTrabajos) {
            if (trabajo.getEliminado()) {
                eliminados++;
                continue;
            }

            if (trabajo.getFinalizado()) {
                finalizados++;
            } else {
                activos++;
            }
            totalFacturacion = totalFacturacion.add(trabajo.precioACobrar());
        }

        etiquetaResumen.setText("Resumen: total=" + total + " | activos=" + activos + " | finalizados="
                + finalizados + " | eliminados=" + eliminados + " | facturacion potencial=" + totalFacturacion);
    }

    private void exportarCsvDialogo() {
        JFileChooser selector = new JFileChooser();
        selector.setDialogTitle("Exportar trabajos a CSV");
        selector.setSelectedFile(new File("trabajos_garaje.csv"));
        int resultado = selector.showSaveDialog(this);
        if (resultado != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File destino = selector.getSelectedFile();
        try {
            exportarCsv(destino);
            mostrarInfo("CSV exportado en: " + destino.getAbsolutePath());
        } catch (IOException e) {
            mostrarError("No se pudo exportar el CSV: " + e.getMessage());
        }
    }

    private void exportarCsv(File fichero) throws IOException {
        List<Trabajo> trabajos = garaje.getTrabajos();
        try (FileWriter escritor = new FileWriter(fichero, false)) {
            escritor.write("id,tipo,descripcion,horas,plazo_dias,finalizado,eliminado,precio\n");
            for (Trabajo trabajo : trabajos) {
                String descripcion = trabajo.getDescripcion() == null ? "" : trabajo.getDescripcion().toString();
                descripcion = descripcion.replace('"', '\'');
                escritor.write(trabajo.getId() + "," + trabajo.getClass().getSimpleName() + ",\""
                        + descripcion + "\"," + trabajo.getHorasNecesariasRealizacion() + "," + trabajo.plazoMaximo()
                        + "," + trabajo.getFinalizado() + "," + trabajo.getEliminado() + "," + trabajo.precioACobrar()
                        + "\n");
            }
        }
    }

    private void guardarDatos() {
        try {
            GestorPersistenciaGaraje.guardar(garaje, contador);
        } catch (Exception e) {
            mostrarError("No se pudieron guardar los datos: " + e.getMessage());
        }
    }

    private void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void mostrarInfo(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Informacion", JOptionPane.INFORMATION_MESSAGE);
    }
}
