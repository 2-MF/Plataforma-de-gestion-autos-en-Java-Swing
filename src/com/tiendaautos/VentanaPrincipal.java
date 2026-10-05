package com.tiendaautos;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.event.ActionListener;
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

/** Ventana principal: menús, escritorio con ventanas internas y barra de estado. */
public class VentanaPrincipal extends JFrame {

    private JDesktopPane escritorio = new JDesktopPane();
    private BaseDatos bd;
    private JLabel barraEstado = new JLabel(" ");
    private VentanaListado listado;
    private VentanaFormulario formulario;

    public VentanaPrincipal() {
        super("AutoCenter J-Swing — Plataforma de gestión para tienda de autos");
        bd = abrirBaseDatos();
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

        // Tamaño: nunca más grande que la pantalla (importante con escalado).
        Rectangle pantalla = GraphicsEnvironment.getLocalGraphicsEnvironment()
                .getMaximumWindowBounds();
        setSize(Math.min(1280, pantalla.width - 40), Math.min(800, pantalla.height - 60));
        setMinimumSize(new Dimension(640, 480));
        setLocationRelativeTo(null); // centra la ventana

        setLayout(new BorderLayout());
        escritorio.setBackground(new Color(45, 49, 66));
        add(escritorio, BorderLayout.CENTER);
        barraEstado.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(90, 90, 100)),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)));
        add(barraEstado, BorderLayout.SOUTH);
        setJMenuBar(crearMenus());

        // Pregunta antes de salir.
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                salir();
            }
        });

        actualizarBarra();
        // Abre el listado en cuanto arranca.
        SwingUtilities.invokeLater(this::abrirListado);
    }

    /** Crea la barra de menús completa. */
    private JMenuBar crearMenus() {
        JMenuBar barra = new JMenuBar();

        JMenu sistema = new JMenu("Sistema");
        agregarItem(sistema, "Exportar respaldo (CSV)…", "ctrl E", e -> exportarCsv());
        agregarItem(sistema, "Importar respaldo (CSV)…", null, e -> importarCsv());
        sistema.addSeparator();
        agregarItem(sistema, "Salir", "ctrl Q", e -> salir());

        JMenu vehiculos = new JMenu("Vehículos");
        agregarItem(vehiculos, "Ver listado y filtros", "ctrl L", e -> abrirListado());
        agregarItem(vehiculos, "Agregar nuevo vehículo…", "ctrl N", e -> abrirFormulario(null));

        JMenu ventanas = new JMenu("Ventanas");
        agregarItem(ventanas, "Organizar en cascada", null, e -> cascada());
        agregarItem(ventanas, "Organizar en mosaico", null, e -> mosaico());
        ventanas.addSeparator();
        agregarItem(ventanas, "Cerrar todas", null, e -> {
            for (JInternalFrame m : escritorio.getAllFrames()) {
                m.dispose();
            }
        });

        JMenu ayuda = new JMenu("Ayuda");
        agregarItem(ayuda, "Acerca de…", null, e -> JOptionPane.showMessageDialog(this,
                "AutoCenter J-Swing\n"
                + "Plataforma de gestión para una tienda de autos.\n\n"
                + "Java Swing + MySQL/MariaDB (JDBC), sin Spring Boot.",
                "Acerca de AutoCenter J-Swing", JOptionPane.INFORMATION_MESSAGE));

        barra.add(sistema);
        barra.add(vehiculos);
        barra.add(ventanas);
        barra.add(ayuda);
        return barra;
    }

    /** Ayuda a crear un item de menú en una sola línea. */
    private void agregarItem(JMenu menu, String texto, String atajo, ActionListener accion) {
        JMenuItem item = new JMenuItem(texto);
        if (atajo != null) {
            item.setAccelerator(KeyStroke.getKeyStroke(atajo));
        }
        item.addActionListener(accion);
        menu.add(item);
    }

    /** Abre la base de datos; si falla, avisa y cierra la app. */
    private BaseDatos abrirBaseDatos() {
        try {
            return new BaseDatos();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null,
                    "No se pudo conectar a la base de datos MySQL/MariaDB:\n\n"
                    + e.getMessage() + "\n\n"
                    + "Revisa que el servidor esté corriendo (por ejemplo:\n"
                    + "sudo systemctl start mariadb) y los datos de db.properties.",
                    "Error de base de datos", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
            return null;
        }
    }

    /* ============ Ventanas internas ============ */

    /** Abre (o trae al frente) la ventana del listado. */
    public void abrirListado() {
        if (listado == null || listado.isClosed()) {
            listado = new VentanaListado(this);
            escritorio.add(listado);
            colocarDentro(listado);
            listado.setVisible(true);
        } else {
            listado.moveToFront();
            try {
                listado.setSelected(true);
            } catch (Exception e) {
                // si no se puede seleccionar, no pasa nada
            }
        }
    }

    /** Abre el formulario: para agregar (vehiculo == null) o para editar. */
    public void abrirFormulario(Vehiculo vehiculo) {
        if (formulario != null && !formulario.isClosed()) {
            formulario.dispose(); // se cierra el anterior
        }
        formulario = new VentanaFormulario(this, vehiculo);
        escritorio.add(formulario);
        colocarDentro(formulario);
        formulario.setVisible(true);
        try {
            formulario.setSelected(true);
        } catch (Exception e) {
            // si no se puede seleccionar, no pasa nada
        }
    }

    /** Coloca una ventana interna dentro del área visible. */
    private void colocarDentro(JInternalFrame marco) {
        int ancho = Math.max(420, Math.min(marco.getWidth(), escritorio.getWidth() - 24));
        int alto = Math.max(280, Math.min(marco.getHeight(), escritorio.getHeight() - 24));
        marco.setBounds(12, 12, ancho, alto);
    }

    /** Refresca la tabla del listado (si está abierta). */
    public void refrescarListado() {
        if (listado != null && !listado.isClosed()) {
            listado.refrescar();
        }
    }

    public BaseDatos getBd() {
        return bd;
    }

    /** Muestra el total de vehículos y la conexión en la barra de abajo. */
    public void actualizarBarra() {
        try {
            barraEstado.setText("Inventario: " + bd.total() + " vehículos   |   "
                    + bd.descripcion());
        } catch (Exception e) {
            barraEstado.setText("Sin conexión con la base de datos.");
        }
    }

    /* ============ Respaldo CSV ============ */

    /** Guarda un respaldo de todos los vehículos en un CSV. */
    private void exportarCsv() {
        JFileChooser elegir = new JFileChooser();
        elegir.setSelectedFile(new File("respaldo_vehiculos.csv"));
        if (elegir.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return; // el usuario canceló
        }
        try {
            bd.exportarCsv(elegir.getSelectedFile());
            JOptionPane.showMessageDialog(this, "Respaldo exportado correctamente.");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "No se pudo exportar:\n" + e,
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Agrega al inventario los vehículos de un CSV. */
    private void importarCsv() {
        JFileChooser elegir = new JFileChooser();
        if (elegir.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return; // el usuario canceló
        }
        if (JOptionPane.showConfirmDialog(this,
                "Se agregarán al inventario los vehículos del archivo. ¿Continuar?",
                "Importar", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            int cuantos = bd.importarCsv(elegir.getSelectedFile());
            refrescarListado();
            actualizarBarra();
            JOptionPane.showMessageDialog(this, cuantos + " vehículos importados.");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "No se pudo importar:\n" + e,
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /* ============ Salir y organización de ventanas ============ */

    /** Pregunta y cierra la aplicación. */
    private void salir() {
        if (JOptionPane.showConfirmDialog(this, "¿Salir de la aplicación?",
                "Salir", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            dispose();
            System.exit(0);
        }
    }

    private void cascada() {
        JInternalFrame[] marcos = escritorio.getAllFrames();
        for (int i = 0; i < marcos.length; i++) {
            marcos[i].setBounds(30 + i * 30, 30 + i * 30,
                    Math.max(420, escritorio.getWidth() - 40 - i * 30),
                    Math.max(300, escritorio.getHeight() - 40 - i * 30));
        }
    }

    private void mosaico() {
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
}
