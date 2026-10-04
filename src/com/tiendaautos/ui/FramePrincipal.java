package com.tiendaautos.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.JDesktopPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import com.tiendaautos.model.Inventario;
import com.tiendaautos.model.Vehiculo;

/**
 * Ventana principal. MDI con JDesktopPane, barra de menús
 * (JMenuBar/JMenu/JMenuItem), barra de estado y ventanas internas.
 */
public class FramePrincipal extends JFrame {

    private static final long serialVersionUID = 1L;

    private final JDesktopPane escritorio = new JDesktopPane();
    private final Inventario inventario;
    private final JLabel barraEstado = new JLabel(" ");

    private InternalListado listado;
    private InternalAgregar agregarUI;

    public FramePrincipal() {
        super("AutoCenter J-Swing — Plataforma de gestión para tienda de autos");
        inventario = abrirInventario();
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

        // Tamaño acorde al área visible real de la pantalla:
        // la ventana nunca será más grande que el monitor (pasa con
        // pantallas pequeñas o con escalado fraccional en Hyprland/Sway).
        Rectangle areaPantalla = GraphicsEnvironment.getLocalGraphicsEnvironment()
                .getMaximumWindowBounds();
        int anchoVentana = Math.min(1280, Math.max(areaPantalla.width - 40, 640));
        int altoVentana = Math.min(800, Math.max(areaPantalla.height - 60, 480));
        setSize(anchoVentana, altoVentana);
        setMinimumSize(new Dimension(640, 480));
        setLocationRelativeTo(null);

        setLayout(new BorderLayout());
        escritorio.setBackground(new Color(45, 49, 66));
        add(escritorio, BorderLayout.CENTER);
        barraEstado.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(90, 90, 100)),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)));
        add(barraEstado, BorderLayout.SOUTH);

        setJMenuBar(crearBarraMenus());

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                salir();
            }
        });

        actualizarBarraEstado();

        SwingUtilities.invokeLater(this::abrirListado);
    }

    /* ================= Menús ================= */

    private JMenuBar crearBarraMenus() {
        JMenuBar barra = new JMenuBar();

        JMenu mSistema = new JMenu("Sistema");
        JMenuItem miExportar = new JMenuItem("Exportar respaldo (CSV)…");
        miExportar.setAccelerator(KeyStroke.getKeyStroke("ctrl E"));
        miExportar.addActionListener(e -> exportarCsv());
        JMenuItem miImportar = new JMenuItem("Importar respaldo (CSV)…");
        miImportar.addActionListener(e -> importarCsv());
        JMenuItem miSalir = new JMenuItem("Salir");
        miSalir.setAccelerator(KeyStroke.getKeyStroke("ctrl Q"));
        miSalir.addActionListener(e -> salir());
        mSistema.add(miExportar);
        mSistema.add(miImportar);
        mSistema.addSeparator();
        mSistema.add(miSalir);

        JMenu mVehiculos = new JMenu("Vehículos");
        JMenuItem miListar = new JMenuItem("Ver listado y filtros");
        miListar.setAccelerator(KeyStroke.getKeyStroke("ctrl L"));
        miListar.addActionListener(e -> abrirListado());
        JMenuItem miAgregar = new JMenuItem("Agregar nuevo vehículo…");
        miAgregar.setAccelerator(KeyStroke.getKeyStroke("ctrl N"));
        miAgregar.addActionListener(e -> abrirAgregar(null));
        mVehiculos.add(miListar);
        mVehiculos.add(miAgregar);

        JMenu mVentanas = new JMenu("Ventanas");
        JMenuItem miCascada = new JMenuItem("Organizar en cascada");
        miCascada.addActionListener(e -> organizarCascada());
        JMenuItem miMosaico = new JMenuItem("Organizar en mosaico");
        miMosaico.addActionListener(e -> organizarMosaico());
        JMenuItem miCerrarTodas = new JMenuItem("Cerrar todas");
        miCerrarTodas.addActionListener(e -> cerrarTodas());
        mVentanas.add(miCascada);
        mVentanas.add(miMosaico);
        mVentanas.addSeparator();
        mVentanas.add(miCerrarTodas);

        JMenu mAyuda = new JMenu("Ayuda");
        JMenuItem miAcerca = new JMenuItem("Acerca de…");
        miAcerca.addActionListener(e -> mostrarAcercaDe());
        mAyuda.add(miAcerca);

        barra.add(mSistema);
        barra.add(mVehiculos);
        barra.add(mVentanas);
        barra.add(mAyuda);
        return barra;
    }

    /* ================= Acciones del menú ================= */

    private void exportarCsv() {
        JFileChooser selector = new JFileChooser();
        selector.setSelectedFile(new File("respaldo_vehiculos.csv"));
        if (selector.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        try {
            inventario.exportarCsv(selector.getSelectedFile());
            JOptionPane.showMessageDialog(this,
                    "Respaldo exportado correctamente.",
                    "Exportar", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "No se pudo exportar el respaldo:\n" + ex, "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void importarCsv() {
        JFileChooser selector = new JFileChooser();
        if (selector.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        int r = JOptionPane.showConfirmDialog(this,
                "Se agregarán al inventario los vehículos del archivo seleccionado.\n¿Continuar?",
                "Importar respaldo", JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
        if (r != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            int importados = inventario.importarCsv(selector.getSelectedFile());
            refrescarListado();
            actualizarBarraEstado();
            JOptionPane.showMessageDialog(this,
                    importados + " vehículos importados correctamente.",
                    "Importar", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "No se pudo importar el respaldo:\n" + ex, "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void salir() {
        int r = JOptionPane.showConfirmDialog(this,
                "¿Salir de la aplicación?",
                "Salir", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (r == JOptionPane.YES_OPTION) {
            dispose();
            System.exit(0);
        }
    }

    private void mostrarAcercaDe() {
        JOptionPane.showMessageDialog(this,
                "AutoCenter J-Swing\n"
                + "Plataforma de gestión para una tienda/concesionaria de autos.\n\n"
                + "Componentes de Java Swing utilizados:\n"
                + "- JLabel, JTextField, JButton\n"
                + "- JScrollPane (contiene la tabla de resultados)\n"
                + "- JMenuBar, JMenu, JMenuItem\n"
                + "- JDesktopPane con ventanas internas (JInternalFrame)\n"
                + "- JTable, JComboBox, JOptionPane\n\n"
                + "Persistencia: base de datos SQL MySQL/MariaDB\n"
                + "accedida con JDBC (configurada en db.properties).\n"
                + "Sin Spring Boot.",
                "Acerca de AutoCenter J-Swing",
                JOptionPane.INFORMATION_MESSAGE);
    }

    /* ================= Ventanas internas ================= */

    public void abrirListado() {
        if (listado == null || listado.isClosed()) {
            listado = new InternalListado(this);
            escritorio.add(listado);
            colocarEnEscritorio(listado);
            listado.setVisible(true);
            try {
                listado.setSelected(true);
            } catch (Exception ex) {
                // ignorar
            }
        } else {
            try {
                listado.setIcon(false);
                listado.setSelected(true);
            } catch (Exception ex) {
                // ignorar
            }
            listado.moveToFront();
        }
    }

    public void abrirAgregar(Vehiculo existente) {
        if (agregarUI != null && !agregarUI.isClosed()) {
            agregarUI.dispose();
        }
        agregarUI = new InternalAgregar(this, existente);
        escritorio.add(agregarUI);
        colocarEnEscritorio(agregarUI);
        agregarUI.setVisible(true);
        try {
            agregarUI.setSelected(true);
        } catch (Exception ex) {
            // ignorar
        }
    }

    public void refrescarListado() {
        if (listado != null && !listado.isClosed()) {
            listado.refrescar();
        }
    }

    /**
     * Coloca una ventana interna dentro del área visible del escritorio,
     * para que nunca quede por fuera de la pantalla.
     */
    private void colocarEnEscritorio(JInternalFrame marco) {
        int anchoDisp = escritorio.getWidth();
        int altoDisp = escritorio.getHeight();
        if (anchoDisp > 0 && altoDisp > 0) {
            int ancho = Math.min(marco.getWidth(), anchoDisp - 24);
            int alto = Math.min(marco.getHeight(), altoDisp - 24);
            marco.setBounds(12, 12, Math.max(420, ancho), Math.max(280, alto));
        } else {
            marco.setLocation(24, 24);
        }
    }

    private void organizarCascada() {
        JInternalFrame[] marcos = escritorio.getAllFrames();
        for (int i = 0; i < marcos.length; i++) {
            int ancho = Math.max(420, escritorio.getWidth() - 40 - i * 30);
            int alto = Math.max(300, escritorio.getHeight() - 40 - i * 30);
            marcos[i].setBounds(30 + i * 30, 30 + i * 30, ancho, alto);
        }
    }

    private void organizarMosaico() {
        JInternalFrame[] marcos = escritorio.getAllFrames();
        if (marcos.length == 0) {
            return;
        }
        int columnas = (int) Math.ceil(Math.sqrt(marcos.length));
        int filas = (int) Math.ceil((double) marcos.length / columnas);
        int ancho = Math.max(320, escritorio.getWidth() / columnas);
        int alto = Math.max(240, escritorio.getHeight() / filas);
        for (int i = 0; i < marcos.length; i++) {
            marcos[i].setBounds((i % columnas) * ancho, (i / columnas) * alto, ancho, alto);
        }
    }

    private void cerrarTodas() {
        for (JInternalFrame f : escritorio.getAllFrames()) {
            f.dispose();
        }
    }

    /* ================= Datos ================= */

    public Inventario getInventario() {
        return inventario;
    }

    public void actualizarBarraEstado() {
        barraEstado.setText("Inventario: " + inventario.getTotal()
                + " vehículos   |   " + inventario.descripcionBd());
    }

    /** Abre la base de datos; muestra el error y termina si falla. */
    private Inventario abrirInventario() {
        try {
            return new Inventario();
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(null,
                    "No se pudo conectar a la base de datos MySQL/MariaDB:\n\n"
                    + ex.getMessage() + "\n\n"
                    + "Revisa que el servidor esté corriendo (por ejemplo:\n"
                    + "sudo systemctl start mariadb) y los datos de conexión\n"
                    + "en el archivo db.properties (host, puerto, usuario,\n"
                    + "contraseña).",
                    "Error de base de datos", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
            return null; // nunca se alcanza
        }
    }
}
