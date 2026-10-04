package com.tiendaautos.ui;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import com.tiendaautos.model.Vehiculo;

/** Modelo de la JTable de resultados del listado. */
public class ModeloTablaVehiculos extends AbstractTableModel {

    private static final long serialVersionUID = 1L;

    private static final String[] COLUMNAS = {
        "ID", "Marca", "Modelo", "Año", "Tipo", "Precio", "Estado",
        "Combustible", "Transmisión", "Color", "Km", "VIN / Serie"
    };

    private List<Vehiculo> filas = new ArrayList<>();

    public void setFilas(List<Vehiculo> filas) {
        this.filas = filas;
        fireTableDataChanged();
    }

    public Vehiculo getVehiculo(int indice) {
        if (indice < 0 || indice >= filas.size()) {
            return null;
        }
        return filas.get(indice);
    }

    @Override
    public int getRowCount() {
        return filas.size();
    }

    @Override
    public int getColumnCount() {
        return COLUMNAS.length;
    }

    @Override
    public String getColumnName(int columna) {
        return COLUMNAS[columna];
    }

    /** Se usan tipos numéricos para que ordenar por columnas funcione bien. */
    @Override
    public Class<?> getColumnClass(int columna) {
        switch (columna) {
            case 0: return Integer.class;  // ID
            case 5: return Double.class;   // Precio
            case 10: return Integer.class; // Km
            default: return String.class;
        }
    }

    @Override
    public Object getValueAt(int fila, int columna) {
        Vehiculo v = filas.get(fila);
        switch (columna) {
            case 0: return v.getId();
            case 1: return v.getMarca();
            case 2: return v.getModelo();
            case 3: return String.valueOf(v.getAnio());
            case 4: return v.getTipo();
            case 5: return v.getPrecio();
            case 6: return v.getEstado();
            case 7: return v.getCombustible();
            case 8: return v.getTransmision();
            case 9: return v.getColor();
            case 10: return v.getKilometraje();
            case 11: return v.getVin();
            default: return "";
        }
    }
}
