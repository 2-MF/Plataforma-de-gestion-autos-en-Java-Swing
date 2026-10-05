package com.tiendaautos;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
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
import javax.swing.table.DefaultTableModel;

/** Ventana con los filtros de búsqueda y la tabla de vehículos. */
public class VentanaListado extends JInternalFrame {

    private VentanaPrincipal principal;

    // Campos de filtro.
    private JTextField tfMarca = new JTextField(10);
    private JTextField tfModelo = new JTextField(10);
    private JTextField tfAnioDesde = new JTextField(4);
    private JTextField tfAnioHasta = new JTextField(4);
    private JTextField tfPrecioMin = new JTextField(7);
    private JTextField tfPrecioMax = new JTextField(7);
    private JTextField tfBusqueda = new JTextField(12);
    private JComboBox<String> cbTipo = comboConTodos(Vehiculo.TIPOS);
    private JComboBox<String> cbCombustible = comboConTodos(Vehiculo.COMBUSTIBLES);
    private JComboBox<String> cbEstado = comboConTodos(Vehiculo.ESTADOS);

    // Botones.
    private JButton btnFiltrar = new JButton("Filtrar");
    private JButton btnLimpiar = new JButton("Limpiar filtros");
    private JButton btnAgregar = new JButton("Agregar vehículo…");
    private JButton btnEditar = new JButton("Editar seleccionado");
    private JButton btnEliminar = new JButton("Eliminar seleccionado");

    // Tabla de resultados.
    private JLabel lblResultado = new JLabel(" ");
    private List<Vehiculo> vehiculos = new ArrayList<>(); // lo que muestra la tabla

    /** Modelo de la tabla: no editable y con tipos numéricos para ordenar bien. */
    private DefaultTableModel modelo = new DefaultTableModel() {
        @Override
        public boolean isCellEditable(int fila, int columna) {
            return false;
        }

        @Override
        public Class<?> getColumnClass(int columna) {
            if (columna == 0 || columna == 10) return Integer.class; // id, km
            if (columna == 5) return Double.class;                  // precio
            return String.class;
        }
    };
    private JTable tabla = new JTable(modelo);

    public VentanaListado(VentanaPrincipal principal) {
        super("Listado y filtros de vehículos", true, true, true, true);
        this.principal = principal;
        setSize(1040, 620);
        setLayout(new BorderLayout(8, 8));
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        // Títulos de las columnas.
        for (String titulo : new String[] { "ID", "Marca", "Modelo", "Año", "Tipo",
                "Precio", "Estado", "Combustible", "Transmisión", "Color", "Km",
                "VIN / Serie" }) {
            modelo.addColumn(titulo);
        }

        add(crearFiltros(), BorderLayout.NORTH);
        add(new JScrollPane(tabla), BorderLayout.CENTER); // la tabla con scroll
        add(lblResultado, BorderLayout.SOUTH);

        prepararTabla();
        conectarAcciones();
        refrescar();
    }

    /** Panel superior con todos los filtros. */
    private JPanel crearFiltros() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Filtros de búsqueda"));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(4, 6, 4, 6);
        g.anchor = GridBagConstraints.WEST;
        g.fill = GridBagConstraints.HORIZONTAL;

        poner(panel, g, 0, 0, new JLabel("Marca:"));
        poner(panel, g, 1, 0, tfMarca);
        poner(panel, g, 2, 0, new JLabel("Modelo:"));
        poner(panel, g, 3, 0, tfModelo);
        poner(panel, g, 4, 0, new JLabel("Búsqueda general:"));
        poner(panel, g, 5, 0, tfBusqueda);

        poner(panel, g, 0, 1, new JLabel("Año desde:"));
        poner(panel, g, 1, 1, tfAnioDesde);
        poner(panel, g, 2, 1, new JLabel("Año hasta:"));
        poner(panel, g, 3, 1, tfAnioHasta);
        poner(panel, g, 4, 1, new JLabel("Tipo de vehículo:"));
        poner(panel, g, 5, 1, cbTipo);

        poner(panel, g, 0, 2, new JLabel("Precio mín.:"));
        poner(panel, g, 1, 2, tfPrecioMin);
        poner(panel, g, 2, 2, new JLabel("Precio máx.:"));
        poner(panel, g, 3, 2, tfPrecioMax);
        poner(panel, g, 4, 2, new JLabel("Combustible:"));
        poner(panel, g, 5, 2, cbCombustible);

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

    /** Pone un componente en la cuadrícula. */
    private static void poner(JPanel panel, GridBagConstraints g, int x, int y, JComponent c) {
        g.gridx = x;
        g.gridy = y;
        g.gridwidth = 1;
        panel.add(c, g);
    }

    /** Crea un combo con la opción "Todos" al inicio. */
    private static JComboBox<String> comboConTodos(String[] opciones) {
        JComboBox<String> cb = new JComboBox<>();
        cb.addItem("Todos");
        for (String o : opciones) {
            cb.addItem(o);
        }
        return cb;
    }

    /** Ajustes de la tabla: orden por encabezado y formato del precio. */
    private void prepararTabla() {
        tabla.setRowHeight(24);
        tabla.setAutoCreateRowSorter(true); // clic en el encabezado = ordenar
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.getColumnModel().getColumn(0).setPreferredWidth(48);
        tabla.getColumnModel().getColumn(11).setPreferredWidth(130);
        // El precio se muestra como "$ 21,800".
        tabla.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            protected void setValue(Object valor) {
                setText(valor instanceof Double
                        ? String.format("$%,.0f", (Double) valor)
                        : String.valueOf(valor));
            }
        });
    }

    /** Une los botones y el teclado con las acciones. */
    private void conectarAcciones() {
        btnFiltrar.addActionListener(e -> refrescar());
        btnLimpiar.addActionListener(e -> { limpiar(); refrescar(); });
        btnAgregar.addActionListener(e -> principal.abrirFormulario(null));
        btnEditar.addActionListener(e -> editar());
        btnEliminar.addActionListener(e -> eliminar());

        // Enter en cualquier campo de texto también filtra.
        for (JTextField t : camposDeTexto()) {
            t.addActionListener(e -> refrescar());
        }
        // Doble clic en una fila = editar.
        tabla.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    editar();
                }
            }
        });
    }

    /** Los 7 campos de filtro, para no repetirlos en cada método. */
    private JTextField[] camposDeTexto() {
        return new JTextField[] { tfMarca, tfModelo, tfAnioDesde, tfAnioHasta,
                tfPrecioMin, tfPrecioMax, tfBusqueda };
    }

    /** Vacía todos los filtros. */
    private void limpiar() {
        for (JTextField t : camposDeTexto()) {
            t.setText("");
        }
        cbTipo.setSelectedIndex(0);
        cbCombustible.setSelectedIndex(0);
        cbEstado.setSelectedIndex(0);
    }

    /** Consulta la base con los filtros actuales y llena la tabla. */
    public void refrescar() {
        int anioDesde = entero(tfAnioDesde, "Año desde");
        int anioHasta = entero(tfAnioHasta, "Año hasta");
        double precioMin = decimal(tfPrecioMin, "Precio mín.");
        double precioMax = decimal(tfPrecioMax, "Precio máx.");
        // -2 / -1 significan "mal escrito" (ya se mostró el aviso).
        if (anioDesde == -2 || anioHasta == -2 || precioMin == -1 || precioMax == -1) {
            return;
        }
        // El índice 0 de cada combo es "Todos": no filtra.
        String tipo = cbTipo.getSelectedIndex() == 0 ? "" : (String) cbTipo.getSelectedItem();
        String combustible = cbCombustible.getSelectedIndex() == 0
                ? "" : (String) cbCombustible.getSelectedItem();
        String estado = cbEstado.getSelectedIndex() == 0
                ? "" : (String) cbEstado.getSelectedItem();

        try {
            vehiculos = principal.getBd().buscar(tfMarca.getText(), tfModelo.getText(),
                    anioDesde, anioHasta, tipo, combustible, estado,
                    precioMin, precioMax, tfBusqueda.getText());

            modelo.setRowCount(0); // limpia la tabla
            for (Vehiculo v : vehiculos) {
                modelo.addRow(new Object[] { v.id, v.marca, v.modelo, String.valueOf(v.anio),
                        v.tipo, v.precio, v.estado, v.combustible, v.transmision,
                        v.color, v.kilometraje, v.vin });
            }
            lblResultado.setText("Mostrando " + vehiculos.size() + " de "
                    + principal.getBd().total() + " vehículos del inventario.");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al consultar la base de datos:\n" + e,
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Lee un número entero. Vacío = -1 (no filtra). Mal escrito = -2 (avisa). */
    private int entero(JTextField campo, String nombre) {
        String t = campo.getText().trim();
        if (t.isEmpty()) {
            return -1;
        }
        try {
            int n = Integer.parseInt(t);
            if (n >= 0) {
                return n;
            }
        } catch (NumberFormatException e) {
            // cae al aviso de abajo
        }
        JOptionPane.showMessageDialog(this,
                "El campo \"" + nombre + "\" debe ser un número entero.",
                "Filtro inválido", JOptionPane.WARNING_MESSAGE);
        return -2;
    }

    /** Lee un número decimal. Vacío = 0 (no filtra). Mal escrito = -1 (avisa). */
    private double decimal(JTextField campo, String nombre) {
        String t = campo.getText().trim();
        if (t.isEmpty()) {
            return 0;
        }
        try {
            double n = Double.parseDouble(t.replace(",", "."));
            if (n >= 0) {
                return n;
            }
        } catch (NumberFormatException e) {
            // cae al aviso de abajo
        }
        JOptionPane.showMessageDialog(this,
                "El campo \"" + nombre + "\" debe ser un número.",
                "Filtro inválido", JOptionPane.WARNING_MESSAGE);
        return -1;
    }

    /** El vehículo de la fila elegida (o null si no hay nada elegido). */
    private Vehiculo seleccionado() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            JOptionPane.showMessageDialog(this, "Selecciona primero un vehículo en la tabla.");
            return null;
        }
        return vehiculos.get(tabla.convertRowIndexToModel(fila));
    }

    /** Abre el formulario con el vehículo elegido. */
    private void editar() {
        Vehiculo v = seleccionado();
        if (v != null) {
            principal.abrirFormulario(v);
        }
    }

    /** Pide confirmación y borra el vehículo elegido. */
    private void eliminar() {
        Vehiculo v = seleccionado();
        if (v == null) {
            return;
        }
        if (JOptionPane.showConfirmDialog(this, "¿Eliminar este vehículo?\n\n" + v,
                "Confirmar eliminación", JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            principal.getBd().eliminar(v.id);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "No se pudo eliminar:\n" + e,
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        refrescar();
        principal.actualizarBarra();
    }
}
