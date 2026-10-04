package com.tiendaautos.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableCellRenderer;

import com.tiendaautos.model.FiltroVehiculo;
import com.tiendaautos.model.Inventario;
import com.tiendaautos.model.Vehiculo;

/**
 * Ventana interna con el listado de vehículos y el panel de filtros.
 * Usa JLabel, JTextField, JComboBox, JButton, JTable y JScrollPane.
 */
public class InternalListado extends JInternalFrame {

    private static final long serialVersionUID = 1L;

    private final FramePrincipal principal;

    // Componentes de filtro
    private final JTextField tfMarca = new JTextField(10);
    private final JTextField tfModelo = new JTextField(10);
    private final JTextField tfAnioDesde = new JTextField(4);
    private final JTextField tfAnioHasta = new JTextField(4);
    private final JTextField tfPrecioMin = new JTextField(7);
    private final JTextField tfPrecioMax = new JTextField(7);
    private final JTextField tfBusqueda = new JTextField(12);
    private final JComboBox<String> cbTipo = comboConTodos(Inventario.TIPOS);
    private final JComboBox<String> cbCombustible = comboConTodos(Inventario.COMBUSTIBLES);
    private final JComboBox<String> cbEstado = comboConTodos(Inventario.ESTADOS);

    // Botones
    private final JButton btnFiltrar = new JButton("Filtrar");
    private final JButton btnLimpiar = new JButton("Limpiar filtros");
    private final JButton btnAgregar = new JButton("Agregar vehículo…");
    private final JButton btnEditar = new JButton("Editar seleccionado");
    private final JButton btnEliminar = new JButton("Eliminar seleccionado");

    // Tabla de resultados
    private final JLabel lblResultados = new JLabel(" ");
    private final ModeloTablaVehiculos modelo = new ModeloTablaVehiculos();
    private final JTable tabla = new JTable(modelo);

    public InternalListado(FramePrincipal principal) {
        super("Listado y filtros de vehículos", true, true, true, true);
        this.principal = principal;
        setLayout(new BorderLayout(8, 8));
        setSize(1040, 620);
        setLocation(24, 24);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        add(crearPanelFiltros(), BorderLayout.NORTH);
        add(crearPanelTabla(), BorderLayout.CENTER);
        add(crearPanelResultado(), BorderLayout.SOUTH);

        btnFiltrar.addActionListener(e -> refrescar());
        btnLimpiar.addActionListener(e -> limpiarFiltros());
        btnAgregar.addActionListener(e -> principal.abrirAgregar(null));
        btnEditar.addActionListener(e -> editarSeleccionado());
        btnEliminar.addActionListener(e -> eliminarSeleccionado());

        // Enter en cualquier campo de texto también aplica el filtro
        for (JTextField tf : new JTextField[] { tfMarca, tfModelo, tfAnioDesde,
                tfAnioHasta, tfPrecioMin, tfPrecioMax, tfBusqueda }) {
            tf.addActionListener(e -> refrescar());
        }

        // Doble clic sobre una fila = editar
        tabla.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    editarSeleccionado();
                }
            }
        });

        refrescar();
    }

    private static JComboBox<String> comboConTodos(String[] opciones) {
        JComboBox<String> cb = new JComboBox<>();
        cb.addItem(FiltroVehiculo.TODOS);
        for (String o : opciones) {
            cb.addItem(o);
        }
        return cb;
    }

    /* ================= Construcción de la interfaz ================= */

    private JPanel crearPanelFiltros() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Filtros de búsqueda"));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(4, 6, 4, 6);
        g.anchor = GridBagConstraints.WEST;
        g.fill = GridBagConstraints.HORIZONTAL;

        // Fila 0: marca, modelo y búsqueda general
        poner(panel, g, 0, 0, new JLabel("Marca:"));
        poner(panel, g, 1, 0, tfMarca);
        poner(panel, g, 2, 0, new JLabel("Modelo:"));
        poner(panel, g, 3, 0, tfModelo);
        poner(panel, g, 4, 0, new JLabel("Búsqueda general:"));
        poner(panel, g, 5, 0, tfBusqueda);

        // Fila 1: años y tipo de vehículo
        poner(panel, g, 0, 1, new JLabel("Año desde:"));
        poner(panel, g, 1, 1, tfAnioDesde);
        poner(panel, g, 2, 1, new JLabel("Año hasta:"));
        poner(panel, g, 3, 1, tfAnioHasta);
        poner(panel, g, 4, 1, new JLabel("Tipo de vehículo:"));
        poner(panel, g, 5, 1, cbTipo);

        // Fila 2: precios y combustible
        poner(panel, g, 0, 2, new JLabel("Precio mín.:"));
        poner(panel, g, 1, 2, tfPrecioMin);
        poner(panel, g, 2, 2, new JLabel("Precio máx.:"));
        poner(panel, g, 3, 2, tfPrecioMax);
        poner(panel, g, 4, 2, new JLabel("Combustible:"));
        poner(panel, g, 5, 2, cbCombustible);

        // Fila 3: estado y botones
        poner(panel, g, 0, 3, new JLabel("Estado:"));
        poner(panel, g, 1, 3, cbEstado);
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        botones.add(btnFiltrar);
        botones.add(btnLimpiar);
        botones.add(btnAgregar);
        botones.add(btnEditar);
        botones.add(btnEliminar);
        g.gridx = 2;
        g.gridy = 3;
        g.gridwidth = 4;
        panel.add(botones, g);

        return panel;
    }

    private static void poner(JPanel panel, GridBagConstraints g, int x, int y, JComponent c) {
        g.gridx = x;
        g.gridy = y;
        g.gridwidth = 1;
        panel.add(c, g);
    }

    private JScrollPane crearPanelTabla() {
        tabla.setFillsViewportHeight(true);
        tabla.setRowHeight(24);
        tabla.setAutoCreateRowSorter(true);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.getTableHeader().setReorderingAllowed(false);
        tabla.getColumnModel().getColumn(0).setPreferredWidth(48);
        tabla.getColumnModel().getColumn(1).setPreferredWidth(90);
        tabla.getColumnModel().getColumn(2).setPreferredWidth(110);
        tabla.getColumnModel().getColumn(4).setPreferredWidth(90);
        tabla.getColumnModel().getColumn(11).setPreferredWidth(130);

        // Pinta el precio con formato "$ 21,800"
        tabla.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
            private static final long serialVersionUID = 1L;

            @Override
            protected void setValue(Object valor) {
                setText(valor instanceof Double
                        ? String.format("$ %,.0f", (Double) valor)
                        : String.valueOf(valor));
            }
        });

        JScrollPane scroll = new JScrollPane(tabla); // JScrollPane requerido
        scroll.getViewport().setBackground(Color.WHITE);
        return scroll;
    }

    private JPanel crearPanelResultado() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(120, 120, 130)),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)));
        panel.add(lblResultados, BorderLayout.WEST);
        return panel;
    }

    /* ================= Lógica ================= */

    /** Vuelve a aplicar los filtros actuales sobre el inventario. */
    public void refrescar() {
        Integer anioDesde;
        Integer anioHasta;
        Double precioMin;
        Double precioMax;
        try {
            anioDesde = enteroOVacio(tfAnioDesde, "Año desde");
            anioHasta = enteroOVacio(tfAnioHasta, "Año hasta");
            precioMin = decimalOVacio(tfPrecioMin, "Precio mín.");
            precioMax = decimalOVacio(tfPrecioMax, "Precio máx.");
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Filtro inválido",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        FiltroVehiculo filtro = new FiltroVehiculo(tfMarca.getText(), tfModelo.getText(),
                anioDesde, anioHasta,
                (String) cbTipo.getSelectedItem(),
                (String) cbCombustible.getSelectedItem(),
                (String) cbEstado.getSelectedItem(),
                precioMin, precioMax, tfBusqueda.getText());

        List<Vehiculo> resultado;
        try {
            resultado = principal.getInventario().filtrar(filtro);
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this,
                    "Error al consultar la base de datos:\n" + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        modelo.setFilas(resultado);
        lblResultados.setText("Mostrando " + resultado.size() + " de "
                + principal.getInventario().getTotal() + " vehículos del inventario.");
    }

    private Integer enteroOVacio(JTextField campo, String nombre) {
        String t = campo.getText().trim();
        if (t.isEmpty()) {
            return null;
        }
        if (!t.matches("\\d+")) {
            throw new NumberFormatException("El campo \"" + nombre
                    + "\" debe ser un número entero (sin letras ni decimales).");
        }
        return Integer.valueOf(t);
    }

    private Double decimalOVacio(JTextField campo, String nombre) {
        String t = campo.getText().trim();
        if (t.isEmpty()) {
            return null;
        }
        String limpio = t.replace(",", ".");
        if (!limpio.matches("\\d+(\\.\\d+)?")) {
            throw new NumberFormatException("El campo \"" + nombre
                    + "\" debe ser un número (por ejemplo 25000 o 25000.50).");
        }
        return Double.valueOf(limpio);
    }

    private void limpiarFiltros() {
        tfMarca.setText("");
        tfModelo.setText("");
        tfAnioDesde.setText("");
        tfAnioHasta.setText("");
        tfPrecioMin.setText("");
        tfPrecioMax.setText("");
        tfBusqueda.setText("");
        cbTipo.setSelectedIndex(0);
        cbCombustible.setSelectedIndex(0);
        cbEstado.setSelectedIndex(0);
        refrescar();
    }

    private Vehiculo vehiculoSeleccionado() {
        int filaVista = tabla.getSelectedRow();
        if (filaVista < 0) {
            JOptionPane.showMessageDialog(this,
                    "Selecciona primero un vehículo en la tabla.",
                    "Nada seleccionado", JOptionPane.INFORMATION_MESSAGE);
            return null;
        }
        return modelo.getVehiculo(tabla.convertRowIndexToModel(filaVista));
    }

    private void editarSeleccionado() {
        Vehiculo v = vehiculoSeleccionado();
        if (v != null) {
            principal.abrirAgregar(v);
        }
    }

    private void eliminarSeleccionado() {
        Vehiculo v = vehiculoSeleccionado();
        if (v == null) {
            return;
        }
        int r = JOptionPane.showConfirmDialog(this,
                "¿Seguro que quieres eliminar este vehículo?\n\n" + v,
                "Confirmar eliminación", JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
        if (r == JOptionPane.YES_OPTION) {
            try {
                principal.getInventario().eliminar(v.getId());
            } catch (RuntimeException ex) {
                JOptionPane.showMessageDialog(this,
                        "Error al eliminar el vehículo:\n" + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            refrescar();
            principal.actualizarBarraEstado();
        }
    }
}
