package com.tiendaautos;

import java.util.ArrayList;
import java.util.List;

/** Un vehículo del inventario. Campos públicos para usarlos directo. */
public class Vehiculo {

    /** Opciones fijas de los combos de la interfaz. */
    public static final String[] TIPOS = { "Sedán", "SUV", "Hatchback", "Coupé", "Convertible", "Pickup", "Minivan" };
    public static final String[] COMBUSTIBLES = { "Gasolina", "Diésel", "Eléctrico", "Híbrido" };
    public static final String[] TRANSMISIONES = { "Manual", "Automática", "CVT" };
    public static final String[] ESTADOS = { "Nuevo", "Usado" };

    public int id;
    public String marca;
    public String modelo;
    public int anio;
    public String tipo;
    public double precio;
    public String color;
    public int kilometraje;
    public String combustible;
    public String transmision;
    public String estado;
    public String vin;

    public Vehiculo() {
        marca = "";
        modelo = "";
        tipo = "";
        color = "";
        combustible = "";
        transmision = "";
        estado = "";
        vin = "";
    }

    public Vehiculo(String marca, String modelo, int anio, String tipo, double precio,
            String color, int kilometraje, String combustible, String transmision,
            String estado, String vin) {
        this();
        this.marca = marca;
        this.modelo = modelo;
        this.anio = anio;
        this.tipo = tipo;
        this.precio = precio;
        this.color = color;
        this.kilometraje = kilometraje;
        this.combustible = combustible;
        this.transmision = transmision;
        this.estado = estado;
        this.vin = vin;
    }

    /** Texto corto para mostrar en mensajes. */
    @Override
    public String toString() {
        return marca + " " + modelo + " " + anio + " (" + tipo + ", " + estado
                + ") — " + String.format("$%,.0f", precio);
    }

    /* ============ Respaldo CSV ============ */

    /** Pasa el vehículo a una línea CSV (para exportar respaldos). */
    public String toCSV() {
        return String.join(";",
                esc(id), esc(marca), esc(modelo), esc(anio), esc(tipo), esc(precio),
                esc(color), esc(kilometraje), esc(combustible), esc(transmision),
                esc(estado), esc(vin));
    }

    /** Lee un vehículo de una línea CSV (para importar respaldos). */
    public static Vehiculo fromCSV(String linea) {
        List<String> c = partirLinea(linea);
        if (c.size() < 12) {
            return null;
        }
        try {
            Vehiculo v = new Vehiculo();
            v.id = Integer.parseInt(c.get(0).trim());
            v.marca = c.get(1).trim();
            v.modelo = c.get(2).trim();
            v.anio = Integer.parseInt(c.get(3).trim());
            v.tipo = c.get(4).trim();
            v.precio = Double.parseDouble(c.get(5).trim());
            v.color = c.get(6).trim();
            v.kilometraje = Integer.parseInt(c.get(7).trim());
            v.combustible = c.get(8).trim();
            v.transmision = c.get(9).trim();
            v.estado = c.get(10).trim();
            v.vin = c.get(11).trim();
            return v;
        } catch (NumberFormatException e) {
            return null; // línea dañada: se ignora
        }
    }

    /** Pone un valor entre comillas y duplica las comillas internas. */
    private static String esc(Object valor) {
        return "\"" + String.valueOf(valor).replace("\"", "\"\"") + "\"";
    }

    /** Separa una línea CSV por ";" respetando las comillas. */
    private static List<String> partirLinea(String linea) {
        List<String> campos = new ArrayList<>();
        StringBuilder actual = new StringBuilder();
        boolean enComillas = false;
        for (int i = 0; i < linea.length(); i++) {
            char c = linea.charAt(i);
            if (enComillas) {
                if (c == '"' && i + 1 < linea.length() && linea.charAt(i + 1) == '"') {
                    actual.append('"'); // dos comillas seguidas = una comilla
                    i++;
                } else if (c == '"') {
                    enComillas = false;
                } else {
                    actual.append(c);
                }
            } else if (c == '"') {
                enComillas = true;
            } else if (c == ';') {
                campos.add(actual.toString());
                actual.setLength(0);
            } else {
                actual.append(c);
            }
        }
        campos.add(actual.toString());
        return campos;
    }
}
