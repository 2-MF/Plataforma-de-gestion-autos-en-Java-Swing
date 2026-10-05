package com.tiendaautos;

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

/** Formulario para agregar un vehículo nuevo o editar uno existente. */
public class VentanaFormulario extends JInternalFrame {

    private VentanaPrincipal principal;
    private Vehiculo original; // null = agregar; si no, es el que se edita

    private JTextField tfMarca = new JTextField(12);
    private JTextField tfModelo = new JTextField(12);
    private JTextField tfAnio = new JTextField(6);
    private JTextField tfPrecio = new JTextField(10);
    private JTextField tfColor = new JTextField(10);
    private JTextField tfKm = new JTextField(10);
    private JTextField tfVin = new JTextField(14);

    private JComboBox<String> cbTipo = new JComboBox<>(Vehiculo.TIPOS);
    private JComboBox<String> cbCombustible = new JComboBox<>(Vehiculo.COMBUSTIBLES);
    private JComboBox<String> cbTransmision = new JComboBox<>(Vehiculo.TRANSMISIONES);
    private JComboBox<String> cbEstado = new JComboBox<>(Vehiculo.ESTADOS);

    private JButton btnGuardar = new JButton("Guardar");
    private JButton btnLimpiar = new JButton("Limpiar");
    private JButton btnCerrar = new JButton("Cerrar");

    public VentanaFormulario(VentanaPrincipal principal, Vehiculo vehiculo) {
        super(vehiculo == null ? "Agregar nuevo vehículo"
                : "Editar vehículo (ID " + vehiculo.id + ")", true, true, true, true);
        this.principal = principal;
        this.original = vehiculo;
        setSize(600, 420);
        setLayout(new BorderLayout(8, 8));
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        add(crearFormulario(), BorderLayout.CENTER);

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        botones.add(btnGuardar);
        botones.add(btnLimpiar);
        botones.add(btnCerrar);
        add(botones, BorderLayout.SOUTH);

        // Atajos de teclado (Alt + letra).
        btnGuardar.setMnemonic(KeyEvent.VK_G);
        btnLimpiar.setMnemonic(KeyEvent.VK_L);
        btnCerrar.setMnemonic(KeyEvent.VK_C);

        btnGuardar.addActionListener(e -> guardar());
        btnLimpiar.addActionListener(e -> limpiar());
        btnCerrar.addActionListener(e -> dispose());

        if (vehiculo != null) {
            llenar(vehiculo); // modo editar: muestra sus datos
        }
    }

    /** Panel con las etiquetas y los campos. */
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
        poner(panel, g, 3, 4, tfKm);

        poner(panel, g, 0, 5, new JLabel("VIN / Serie:"));
        g.gridx = 1;
        g.gridy = 5;
        g.gridwidth = 3;
        panel.add(tfVin, g);

        return panel;
    }

    /** Pone un componente en la cuadrícula. */
    private static void poner(JPanel panel, GridBagConstraints g, int x, int y, JComponent c) {
        g.gridx = x;
        g.gridy = y;
        g.gridwidth = 1;
        panel.add(c, g);
    }

    /** Pone los datos del vehículo en los campos (al editar). */
    private void llenar(Vehiculo v) {
        tfMarca.setText(v.marca);
        tfModelo.setText(v.modelo);
        tfAnio.setText("" + v.anio);
        String precio = "" + v.precio;
        if (precio.endsWith(".0")) { // 21800.0 -> 21800
            precio = precio.substring(0, precio.length() - 2);
        }
        tfPrecio.setText(precio);
        tfColor.setText(v.color);
        tfKm.setText("" + v.kilometraje);
        tfVin.setText(v.vin);
        cbTipo.setSelectedItem(v.tipo);
        cbCombustible.setSelectedItem(v.combustible);
        cbTransmision.setSelectedItem(v.transmision);
        cbEstado.setSelectedItem(v.estado);
    }

    /** Vacía los campos. */
    private void limpiar() {
        tfMarca.setText("");
        tfModelo.setText("");
        tfAnio.setText("");
        tfPrecio.setText("");
        tfColor.setText("");
        tfKm.setText("");
        tfVin.setText("");
        cbTipo.setSelectedIndex(0);
        cbCombustible.setSelectedIndex(0);
        cbTransmision.setSelectedIndex(0);
        cbEstado.setSelectedIndex(0);
        tfMarca.requestFocusInWindow();
    }

    /** Valida los datos y guarda en la base (INSERT o UPDATE). */
    private void guardar() {
        String marca = tfMarca.getText().trim();
        String modelo = tfModelo.getText().trim();
        String color = tfColor.getText().trim();
        String vin = tfVin.getText().trim();

        // Validaciones.
        if (marca.isEmpty() || modelo.isEmpty()) {
            aviso("La marca y el modelo son obligatorios.");
            return;
        }
        int anio = entero(tfAnio, "El año debe ser un número entero, por ejemplo 2024.");
        if (anio == -2) {
            return;
        }
        if (anio < 1900 || anio > 2100) {
            aviso("El año debe estar entre 1900 y 2100.");
            return;
        }
        double precio = decimal(tfPrecio, "El precio debe ser un número, por ejemplo 24990.");
        if (precio == -1) {
            return;
        }
        if (precio <= 0) {
            aviso("El precio debe ser mayor que cero.");
            return;
        }
        int km = 0; // si se deja vacío, el kilometraje es 0
        if (!tfKm.getText().trim().isEmpty()) {
            km = entero(tfKm, "El kilometraje debe ser un número entero.");
            if (km == -2) {
                return;
            }
            if (km < 0) {
                aviso("El kilometraje no puede ser negativo.");
                return;
            }
        }

        try {
            // El VIN no puede repetirse (se ignora el propio id al editar).
            if (!vin.isEmpty()
                    && principal.getBd().existeVin(vin, original == null ? -1 : original.id)) {
                aviso("Ya existe otro vehículo registrado con ese VIN/Serie.");
                return;
            }

            Vehiculo v = new Vehiculo(marca, modelo, anio,
                    (String) cbTipo.getSelectedItem(), precio, color, km,
                    (String) cbCombustible.getSelectedItem(),
                    (String) cbTransmision.getSelectedItem(),
                    (String) cbEstado.getSelectedItem(), vin);

            if (original == null) {
                v.id = principal.getBd().insertar(v); // INSERT
                JOptionPane.showMessageDialog(this,
                        "Vehículo agregado con ID " + v.id + ".");
                limpiar(); // deja el formulario listo para otro
            } else {
                v.id = original.id;
                principal.getBd().actualizar(v); // UPDATE
                JOptionPane.showMessageDialog(this, "Vehículo actualizado.");
                dispose(); // cierra el formulario
            }
            principal.refrescarListado();
            principal.actualizarBarra();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error de base de datos:\n" + e,
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Lee un número entero obligatorio. Mal escrito = -2 (ya avisó). */
    private int entero(JTextField campo, String mensaje) {
        try {
            return Integer.parseInt(campo.getText().trim());
        } catch (NumberFormatException e) {
            aviso(mensaje);
            return -2;
        }
    }

    /** Lee un número decimal obligatorio. Mal escrito = -1 (ya avisó). */
    private double decimal(JTextField campo, String mensaje) {
        try {
            return Double.parseDouble(campo.getText().trim().replace(",", "."));
        } catch (NumberFormatException e) {
            aviso(mensaje);
            return -1;
        }
    }

    /** Muestra un aviso de datos inválidos. */
    private void aviso(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Datos inválidos",
                JOptionPane.WARNING_MESSAGE);
    }
}
