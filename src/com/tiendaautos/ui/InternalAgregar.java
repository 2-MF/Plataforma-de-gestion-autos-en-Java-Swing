package com.tiendaautos.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.KeyEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.tiendaautos.model.Inventario;
import com.tiendaautos.model.Vehiculo;

/**
 * Ventana interna con el formulario para agregar o editar un vehículo.
 * Usa JLabel, JTextField, JComboBox y JButton.
 */
public class InternalAgregar extends JInternalFrame {

    private static final long serialVersionUID = 1L;

    private final FramePrincipal principal;
    private final Vehiculo original; // null => modo "agregar"

    private final JTextField tfMarca = new JTextField(12);
    private final JTextField tfModelo = new JTextField(12);
    private final JTextField tfAnio = new JTextField(6);
    private final JTextField tfPrecio = new JTextField(10);
    private final JTextField tfColor = new JTextField(10);
    private final JTextField tfKilometraje = new JTextField(10);
    private final JTextField tfVin = new JTextField(14);

    private final JComboBox<String> cbTipo = new JComboBox<>(Inventario.TIPOS);
    private final JComboBox<String> cbCombustible = new JComboBox<>(Inventario.COMBUSTIBLES);
    private final JComboBox<String> cbTransmision = new JComboBox<>(Inventario.TRANSMISIONES);
    private final JComboBox<String> cbEstado = new JComboBox<>(Inventario.ESTADOS);

    private final JButton btnGuardar = new JButton("Guardar");
    private final JButton btnLimpiar = new JButton("Limpiar");
    private final JButton btnCerrar = new JButton("Cerrar");

    public InternalAgregar(FramePrincipal principal, Vehiculo existente) {
        super(existente == null ? "Agregar nuevo vehículo"
                : "Editar vehículo (ID " + existente.getId() + ")",
                true, true, true, true);
        this.principal = principal;
        this.original = existente;
        setLayout(new BorderLayout(8, 8));
        setSize(600, 420);
        setLocation(70, 70);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        add(crearFormulario(), BorderLayout.CENTER);
        add(crearBotones(), BorderLayout.SOUTH);

        btnGuardar.addActionListener(e -> guardar());
        btnLimpiar.addActionListener(e -> limpiar());
        btnCerrar.addActionListener(e -> dispose());
        btnGuardar.setMnemonic(KeyEvent.VK_G);
        btnLimpiar.setMnemonic(KeyEvent.VK_L);
        btnCerrar.setMnemonic(KeyEvent.VK_C);

        if (existente != null) {
            llenarDesde(existente);
        } else {
            limpiar();
        }
    }

    private JPanel crearFormulario() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder(
                "Datos del vehículo (* = obligatorio)"));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(5, 8, 5, 8);
        g.anchor = GridBagConstraints.WEST;
        g.fill = GridBagConstraints.HORIZONTAL;

        poner(panel, g, 0, 0, new JLabel("Marca *:"));
        poner(panel, g, 1, 0, tfMarca);
        poner(panel, g, 2, 0, new JLabel("Modelo *:"));
        poner(panel, g, 3, 0, tfModelo);

        poner(panel, g, 0, 1, new JLabel("Año *:"));
        poner(panel, g, 1, 1, tfAnio);
        poner(panel, g, 2, 1, new JLabel("Precio * ($):"));
        poner(panel, g, 3, 1, tfPrecio);

        poner(panel, g, 0, 2, new JLabel("Tipo de vehículo:"));
        poner(panel, g, 1, 2, cbTipo);
        poner(panel, g, 2, 2, new JLabel("Estado:"));
        poner(panel, g, 3, 2, cbEstado);

        poner(panel, g, 0, 3, new JLabel("Combustible:"));
        poner(panel, g, 1, 3, cbCombustible);
        poner(panel, g, 2, 3, new JLabel("Transmisión:"));
        poner(panel, g, 3, 3, cbTransmision);

        poner(panel, g, 0, 4, new JLabel("Color:"));
        poner(panel, g, 1, 4, tfColor);
        poner(panel, g, 2, 4, new JLabel("Kilometraje:"));
        poner(panel, g, 3, 4, tfKilometraje);

        poner(panel, g, 0, 5, new JLabel("VIN / Serie:"));
        g.gridx = 1;
        g.gridy = 5;
        g.gridwidth = 3;
        panel.add(tfVin, g);

        return panel;
    }

    private static void poner(JPanel panel, GridBagConstraints g, int x, int y, JComponent c) {
        g.gridx = x;
        g.gridy = y;
        g.gridwidth = 1;
        panel.add(c, g);
    }

    private JPanel crearBotones() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        panel.add(btnGuardar);
        panel.add(btnLimpiar);
        panel.add(btnCerrar);
        return panel;
    }

    private void llenarDesde(Vehiculo v) {
        tfMarca.setText(v.getMarca());
        tfModelo.setText(v.getModelo());
        tfAnio.setText(String.valueOf(v.getAnio()));
        String precio = String.valueOf(v.getPrecio());
        if (precio.endsWith(".0")) {
            precio = precio.substring(0, precio.length() - 2);
        }
        tfPrecio.setText(precio);
        tfColor.setText(v.getColor());
        tfKilometraje.setText(String.valueOf(v.getKilometraje()));
        tfVin.setText(v.getVin());
        cbTipo.setSelectedItem(v.getTipo());
        cbCombustible.setSelectedItem(v.getCombustible());
        cbTransmision.setSelectedItem(v.getTransmision());
        cbEstado.setSelectedItem(v.getEstado());
    }

    private void limpiar() {
        tfMarca.setText("");
        tfModelo.setText("");
        tfAnio.setText("");
        tfPrecio.setText("");
        tfColor.setText("");
        tfKilometraje.setText("");
        tfVin.setText("");
        cbTipo.setSelectedIndex(0);
        cbCombustible.setSelectedIndex(0);
        cbTransmision.setSelectedIndex(0);
        cbEstado.setSelectedIndex(0);
        tfMarca.requestFocusInWindow();
    }

    private void guardar() {
        String marca = tfMarca.getText().trim();
        String modelo = tfModelo.getText().trim();
        String color = tfColor.getText().trim();
        String vin = tfVin.getText().trim();

        if (marca.isEmpty() || modelo.isEmpty()) {
            error("La marca y el modelo son obligatorios.");
            return;
        }

        int anio;
        try {
            anio = Integer.parseInt(tfAnio.getText().trim());
        } catch (NumberFormatException ex) {
            error("El año debe ser un número entero, por ejemplo 2024.");
            return;
        }
        if (anio < 1900 || anio > 2100) {
            error("El año debe estar entre 1900 y 2100.");
            return;
        }

        double precio;
        try {
            precio = Double.parseDouble(tfPrecio.getText().trim().replace(",", "."));
        } catch (NumberFormatException ex) {
            error("El precio debe ser un número, por ejemplo 24990 o 24990.50.");
            return;
        }
        if (precio <= 0) {
            error("El precio debe ser mayor que cero.");
            return;
        }

        int km = 0;
        String txtKm = tfKilometraje.getText().trim();
        if (!txtKm.isEmpty()) {
            try {
                km = Integer.parseInt(txtKm);
            } catch (NumberFormatException ex) {
                error("El kilometraje debe ser un número entero.");
                return;
            }
            if (km < 0) {
                error("El kilometraje no puede ser negativo.");
                return;
            }
        }

        int idActual = (original == null) ? -1 : original.getId();
        try {
            if (!vin.isEmpty() && principal.getInventario().existeVin(vin, idActual)) {
                error("Ya existe otro vehículo registrado con ese VIN/Serie.");
                return;
            }

            Vehiculo v = new Vehiculo(marca, modelo, anio,
                    (String) cbTipo.getSelectedItem(), precio, color, km,
                    (String) cbCombustible.getSelectedItem(),
                    (String) cbTransmision.getSelectedItem(),
                    (String) cbEstado.getSelectedItem(), vin);

            if (original == null) {
                principal.getInventario().agregar(v);
                JOptionPane.showMessageDialog(this,
                        "Vehículo agregado al inventario con ID " + v.getId() + ".",
                        "Guardado", JOptionPane.INFORMATION_MESSAGE);
                limpiar();
            } else {
                v.setId(original.getId());
                principal.getInventario().actualizar(v);
                JOptionPane.showMessageDialog(this,
                        "Vehículo actualizado correctamente.",
                        "Guardado", JOptionPane.INFORMATION_MESSAGE);
                dispose();
            }

            principal.refrescarListado();
            principal.actualizarBarraEstado();
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this,
                    "Error de base de datos:\n" + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void error(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Datos inválidos",
                JOptionPane.WARNING_MESSAGE);
    }
}
