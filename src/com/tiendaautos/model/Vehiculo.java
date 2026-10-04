package com.tiendaautos.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Representa un vehículo del inventario de la tienda.
 * Incluye conversión a/desde CSV para la persistencia de datos.
 */
public class Vehiculo implements Serializable {

    private static final long serialVersionUID = 1L;

    private int id;
    private String marca;
    private String modelo;
    private int anio;
    private String tipo;
    private double precio;
    private String color;
    private int kilometraje;
    private String combustible;
    private String transmision;
    private String estado;
    private String vin;

    public Vehiculo() {
        marca = ""; modelo = ""; tipo = ""; color = "";
        combustible = ""; transmision = ""; estado = ""; vin = "";
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

    // Getters / Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getMarca() { return marca; }
    public void setMarca(String marca) { this.marca = marca; }
    public String getModelo() { return modelo; }
    public void setModelo(String modelo) { this.modelo = modelo; }
    public int getAnio() { return anio; }
    public void setAnio(int anio) { this.anio = anio; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public double getPrecio() { return precio; }
    public void setPrecio(double precio) { this.precio = precio; }
    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
    public int getKilometraje() { return kilometraje; }
    public void setKilometraje(int kilometraje) { this.kilometraje = kilometraje; }
    public String getCombustible() { return combustible; }
    public void setCombustible(String combustible) { this.combustible = combustible; }
    public String getTransmision() { return transmision; }
    public void setTransmision(String transmision) { this.transmision = transmision; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public String getVin() { return vin; }
    public void setVin(String vin) { this.vin = vin; }

    public String formatoPrecio() {
        return String.format("$ %,.0f", precio);
    }

    @Override
    public String toString() {
        return String.format("[%d] %s %s %d — %s — %s — %s — $ %,.0f",
                id, marca, modelo, anio, tipo, estado, color, precio);
    }

    /* ================= Persistencia en CSV ================= */

    public String toCSV() {
        return String.join(";",
                esc(id), esc(marca), esc(modelo), esc(anio), esc(tipo), esc(precio),
                esc(color), esc(kilometraje), esc(combustible), esc(transmision),
                esc(estado), esc(vin));
    }

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
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private static String esc(Object valor) {
        return "\"" + String.valueOf(valor).replace("\"", "\"\"") + "\"";
    }

    /** Parser mínimo de CSV con separador ';' y campos entre comillas. */
    private static List<String> partirLinea(String linea) {
        List<String> campos = new ArrayList<>();
        StringBuilder actual = new StringBuilder();
        boolean enComillas = false;
        for (int i = 0; i < linea.length(); i++) {
            char c = linea.charAt(i);
            if (enComillas) {
                if (c == '"') {
                    if (i + 1 < linea.length() && linea.charAt(i + 1) == '"') {
                        actual.append('"');
                        i++;
                    } else {
                        enComillas = false;
                    }
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
