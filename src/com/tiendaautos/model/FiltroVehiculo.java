package com.tiendaautos.model;

import java.text.Normalizer;

/**
 * Criterios de filtrado del inventario.
 * Los campos de texto se guardan normalizados (minúsculas y sin acentos)
 * para que la búsqueda sea flexible; BaseDatos los usa como parámetros
 * de las consultas SQL (WHERE dinámico con PreparedStatement).
 */
public final class FiltroVehiculo {

    /** Opción "no filtrar" para los combos. */
    public static final String TODOS = "Todos";

    private static final String TODOS_NORM = normalizar(TODOS);

    private final String marca;
    private final String modelo;
    private final Integer anioDesde;
    private final Integer anioHasta;
    private final String tipo;
    private final String combustible;
    private final String estado;
    private final Double precioMin;
    private final Double precioMax;
    private final String textoLibre;

    public FiltroVehiculo(String marca, String modelo, Integer anioDesde, Integer anioHasta,
            String tipo, String combustible, String estado,
            Double precioMin, Double precioMax, String textoLibre) {
        this.marca = normalizar(marca);
        this.modelo = normalizar(modelo);
        this.anioDesde = anioDesde;
        this.anioHasta = anioHasta;
        this.tipo = normalizar(tipo);
        this.combustible = normalizar(combustible);
        this.estado = normalizar(estado);
        this.precioMin = precioMin;
        this.precioMax = precioMax;
        this.textoLibre = normalizar(textoLibre);
    }

    /* Getters: los textos vienen ya normalizados; null o "" significa
       "no filtrar por este criterio". */

    public String getMarca() {
        return marca;
    }

    public String getModelo() {
        return modelo;
    }

    public Integer getAnioDesde() {
        return anioDesde;
    }

    public Integer getAnioHasta() {
        return anioHasta;
    }

    public String getTipoAFiltrar() {
        return valorCombo(tipo);
    }

    public String getCombustibleAFiltrar() {
        return valorCombo(combustible);
    }

    public String getEstadoAFiltrar() {
        return valorCombo(estado);
    }

    public Double getPrecioMin() {
        return precioMin;
    }

    public Double getPrecioMax() {
        return precioMax;
    }

    public String getTextoLibre() {
        return textoLibre;
    }

    private static String valorCombo(String valor) {
        return (valor.isEmpty() || valor.equals(TODOS_NORM)) ? null : valor;
    }

    /** Texto en minúsculas y sin acentos, para comparaciones flexibles. */
    public static String normalizar(String texto) {
        if (texto == null) {
            return "";
        }
        String sinAcentos = Normalizer.normalize(texto.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "");
        return sinAcentos.toLowerCase();
    }
}
