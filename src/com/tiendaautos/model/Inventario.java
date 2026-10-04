package com.tiendaautos.model;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Inventario de la tienda respaldado en una base de datos SQL (SQLite).
 * Fachada que delega en BaseDatos (JDBC) todas las operaciones: altas,
 * actualizaciones, eliminaciones, consultas con filtros y respaldos CSV.
 */
public class Inventario {

    public static final String[] TIPOS = { "Sedán", "SUV", "Hatchback", "Coupé", "Convertible", "Pickup", "Minivan" };
    public static final String[] COMBUSTIBLES = { "Gasolina", "Diésel", "Eléctrico", "Híbrido" };
    public static final String[] TRANSMISIONES = { "Manual", "Automática", "CVT" };
    public static final String[] ESTADOS = { "Nuevo", "Usado" };

    private final BaseDatos bd;

    public Inventario() {
        this.bd = new BaseDatos();
        try {
            bd.inicializar();
            if (bd.total() == 0) {
                cargarDatosDemo();
            }
        } catch (SQLException e) {
            throw new RuntimeException("No se pudo inicializar la base de datos: "
                    + e.getMessage(), e);
        }
    }

    /** Descripción de la conexión (para la barra de estado). */
    public String descripcionBd() {
        return BaseDatos.descripcion();
    }

    /** Todos los vehículos de la base de datos (SELECT sin filtros). */
    public List<Vehiculo> getVehiculos() {
        return filtrar(sinFiltros());
    }

    public int getTotal() {
        try {
            return bd.total();
        } catch (SQLException e) {
            throw new RuntimeException("Error al contar los vehículos: "
                    + e.getMessage(), e);
        }
    }

    /** INSERT del vehículo en la base de datos. */
    public void agregar(Vehiculo v) {
        try {
            v.setId(bd.insertar(v));
        } catch (SQLException e) {
            throw new RuntimeException("Error al insertar el vehículo: "
                    + e.getMessage(), e);
        }
    }

    /** UPDATE del vehículo en la base de datos. */
    public void actualizar(Vehiculo v) {
        try {
            bd.actualizar(v);
        } catch (SQLException e) {
            throw new RuntimeException("Error al actualizar el vehículo: "
                    + e.getMessage(), e);
        }
    }

    /** DELETE del vehículo por id. */
    public boolean eliminar(int id) {
        try {
            return bd.eliminar(id);
        } catch (SQLException e) {
            throw new RuntimeException("Error al eliminar el vehículo: "
                    + e.getMessage(), e);
        }
    }

    /** Indica si ya existe otro vehículo con ese VIN/Serie. */
    public boolean existeVin(String vin, int idIgnorar) {
        if (FiltroVehiculo.normalizar(vin).isEmpty()) {
            return false;
        }
        try {
            return bd.existeVin(vin, idIgnorar);
        } catch (SQLException e) {
            throw new RuntimeException("Error al consultar el VIN: "
                    + e.getMessage(), e);
        }
    }

    /** SELECT con los filtros aplicados (WHERE dinámico en SQL). */
    public List<Vehiculo> filtrar(FiltroVehiculo filtro) {
        try {
            return bd.consultar(filtro);
        } catch (SQLException e) {
            throw new RuntimeException("Error al consultar la base de datos: "
                    + e.getMessage(), e);
        }
    }

    /* ================= Respaldo CSV ================= */

    /** Exporta todo el inventario a un archivo CSV de respaldo. */
    public void exportarCsv(File destino) throws IOException {
        List<Vehiculo> todos = getVehiculos();
        File carpeta = destino.getParentFile();
        if (carpeta != null) {
            carpeta.mkdirs();
        }
        try (PrintWriter pw = new PrintWriter(
                Files.newBufferedWriter(destino.toPath(), StandardCharsets.UTF_8))) {
            for (Vehiculo v : todos) {
                pw.println(v.toCSV());
            }
        }
    }

    /** Importa vehículos desde un respaldo CSV. Devuelve cuántos se importaron. */
    public int importarCsv(File origen) throws IOException {
        int importados = 0;
        try (BufferedReader br = Files.newBufferedReader(origen.toPath(), StandardCharsets.UTF_8)) {
            String linea;
            while ((linea = br.readLine()) != null) {
                if (linea.trim().isEmpty()) {
                    continue;
                }
                Vehiculo v = Vehiculo.fromCSV(linea);
                if (v != null) {
                    agregar(v);
                    importados++;
                }
            }
        }
        return importados;
    }

    private static FiltroVehiculo sinFiltros() {
        return new FiltroVehiculo("", "", null, null, null, null, null, null, null, "");
    }

    /** Inventario de demostración (solo si la base de datos está vacía). */
    private void cargarDatosDemo() {
        agregar(new Vehiculo("Toyota", "Corolla", 2022, "Sedán", 21800, "Blanco", 12500, "Gasolina", "Automática", "Usado", "TOY-2022-COR-001"));
        agregar(new Vehiculo("Toyota", "RAV4", 2024, "SUV", 34990, "Gris", 0, "Híbrido", "CVT", "Nuevo", "TOY-2024-RAV-002"));
        agregar(new Vehiculo("Ford", "Mustang", 2021, "Coupé", 45990, "Rojo", 24000, "Gasolina", "Manual", "Usado", "FRD-2021-MUS-003"));
        agregar(new Vehiculo("Ford", "F-150", 2023, "Pickup", 52400, "Negro", 8000, "Gasolina", "Automática", "Usado", "FRD-2023-F15-004"));
        agregar(new Vehiculo("Ford", "Focus", 2019, "Hatchback", 13900, "Azul", 68000, "Gasolina", "Manual", "Usado", "FRD-2019-FOC-005"));
        agregar(new Vehiculo("Chevrolet", "Onix", 2023, "Sedán", 15900, "Plata", 3000, "Gasolina", "Manual", "Usado", "CHV-2023-ONX-006"));
        agregar(new Vehiculo("Chevrolet", "Tahoe", 2024, "SUV", 68900, "Negro", 0, "Gasolina", "Automática", "Nuevo", "CHV-2024-THO-007"));
        agregar(new Vehiculo("Nissan", "Sentra", 2020, "Sedán", 15600, "Gris", 41000, "Gasolina", "CVT", "Usado", "NSN-2020-SEN-008"));
        agregar(new Vehiculo("Nissan", "Frontier", 2022, "Pickup", 32700, "Blanco", 22000, "Diésel", "Manual", "Usado", "NSN-2022-FRT-009"));
        agregar(new Vehiculo("Tesla", "Model 3", 2023, "Sedán", 42990, "Blanco", 12000, "Eléctrico", "Automática", "Usado", "TSL-2023-M3-010"));
        agregar(new Vehiculo("Tesla", "Model Y", 2025, "SUV", 48990, "Azul", 0, "Eléctrico", "Automática", "Nuevo", "TSL-2025-MY-011"));
        agregar(new Vehiculo("Honda", "Civic", 2021, "Sedán", 19900, "Rojo", 33000, "Gasolina", "CVT", "Usado", "HND-2021-CVC-012"));
        agregar(new Vehiculo("Honda", "CR-V", 2024, "SUV", 32900, "Verde", 500, "Híbrido", "CVT", "Nuevo", "HND-2024-CRV-013"));
        agregar(new Vehiculo("Mazda", "CX-5", 2022, "SUV", 27500, "Gris", 29000, "Gasolina", "Automática", "Usado", "MZD-2022-CX5-014"));
        agregar(new Vehiculo("Kia", "Rio", 2019, "Hatchback", 12500, "Amarillo", 75000, "Gasolina", "Manual", "Usado", "KIA-2019-RIO-015"));
        agregar(new Vehiculo("Hyundai", "Tucson", 2023, "SUV", 28900, "Azul", 9500, "Gasolina", "Automática", "Usado", "HYD-2023-TCS-016"));
    }
}
