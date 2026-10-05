package com.tiendaautos;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/** Toda la base de datos (MySQL/MariaDB con JDBC) en una sola clase. */
public class BaseDatos {

    // Conexión: valores por defecto; se cambian en db.properties.
    private String host = "localhost";
    private int puerto = 3306;
    private String base = "tienda_autos";
    private String usuario = "tienda";
    private String contrasena = "tienda123";

    // Las columnas *_norm guardan el texto en minúsculas y sin acentos,
    // para que buscar "coupe" encuentre "Coupé" en el propio SQL.
    private static final String CREAR_TABLA = "CREATE TABLE IF NOT EXISTS vehiculos ("
            + " id INT AUTO_INCREMENT PRIMARY KEY,"
            + " marca VARCHAR(60) NOT NULL,"
            + " modelo VARCHAR(60) NOT NULL,"
            + " anio INT NOT NULL,"
            + " tipo VARCHAR(40) NOT NULL,"
            + " precio DOUBLE NOT NULL,"
            + " color VARCHAR(40) NOT NULL,"
            + " kilometraje INT NOT NULL,"
            + " combustible VARCHAR(40) NOT NULL,"
            + " transmision VARCHAR(40) NOT NULL,"
            + " estado VARCHAR(20) NOT NULL,"
            + " vin VARCHAR(60) NOT NULL,"
            + " marca_norm VARCHAR(80) NOT NULL,"
            + " modelo_norm VARCHAR(80) NOT NULL,"
            + " tipo_norm VARCHAR(60) NOT NULL,"
            + " combustible_norm VARCHAR(60) NOT NULL,"
            + " estado_norm VARCHAR(40) NOT NULL,"
            + " vin_norm VARCHAR(80) NOT NULL,"
            + " busqueda VARCHAR(500) NOT NULL)";

    public BaseDatos() {
        leerConfig();
        crearBaseYTabla();
        cargarDemo();
    }

    /** Lee la configuración de db.properties (si existe). */
    private void leerConfig() {
        try (FileReader f = new FileReader("db.properties")) {
            Properties p = new Properties();
            p.load(f);
            host = p.getProperty("host", host);
            puerto = Integer.parseInt(p.getProperty("puerto", "" + puerto));
            base = p.getProperty("baseDatos", base);
            usuario = p.getProperty("usuario", usuario);
            contrasena = p.getProperty("contrasena", contrasena);
        } catch (Exception e) {
            // Sin archivo (o dañado) se usan los valores por defecto.
        }
    }

    /** Conecta al servidor ya con la base elegida. */
    private Connection conectar() throws SQLException {
        return DriverManager.getConnection("jdbc:mariadb://" + host + ":" + puerto + "/"
                + base, usuario, contrasena);
    }

    /** Crea la base de datos y la tabla si no existen. */
    private void crearBaseYTabla() {
        try {
            // 1) Crear la base si falta (se conecta sin elegir ninguna).
            String url = "jdbc:mariadb://" + host + ":" + puerto + "/";
            try (Connection c = DriverManager.getConnection(url, usuario, contrasena);
                    Statement st = c.createStatement()) {
                st.execute("CREATE DATABASE IF NOT EXISTS `" + base
                        + "` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");
            }
            // 2) Crear la tabla si falta.
            try (Connection c = conectar(); Statement st = c.createStatement()) {
                st.execute(CREAR_TABLA);
            }
        } catch (SQLException e) {
            // RuntimeException para que la ventana principal muestre un
            // mensaje claro y cierre la app.
            throw new RuntimeException("No se pudo conectar a MySQL/MariaDB ("
                    + host + ":" + puerto + "): " + e.getMessage(), e);
        }
    }

    /* ============ Altas, cambios y bajas ============ */

    /** INSERT: agrega un vehículo y devuelve el id que le dio la base. */
    public int insertar(Vehiculo v) throws SQLException {
        String sql = "INSERT INTO vehiculos (marca, modelo, anio, tipo, precio, color, "
                + "kilometraje, combustible, transmision, estado, vin, "
                + "marca_norm, modelo_norm, tipo_norm, combustible_norm, estado_norm, "
                + "vin_norm, busqueda) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        // ponytail: una conexión por operación; usar un pool si crece el uso.
        try (Connection c = conectar();
                PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            llenar(ps, v);
            ps.executeUpdate();
            try (ResultSet r = ps.getGeneratedKeys()) {
                if (r.next()) {
                    return r.getInt(1);
                }
            }
        }
        return -1;
    }

    /** UPDATE: guarda los cambios de un vehículo existente. */
    public void actualizar(Vehiculo v) throws SQLException {
        String sql = "UPDATE vehiculos SET marca=?, modelo=?, anio=?, tipo=?, precio=?, "
                + "color=?, kilometraje=?, combustible=?, transmision=?, estado=?, vin=?, "
                + "marca_norm=?, modelo_norm=?, tipo_norm=?, combustible_norm=?, estado_norm=?, "
                + "vin_norm=?, busqueda=? WHERE id=?";
        try (Connection c = conectar(); PreparedStatement ps = c.prepareStatement(sql)) {
            llenar(ps, v);
            ps.setInt(19, v.id);
            ps.executeUpdate();
        }
    }

    /** DELETE: borra un vehículo por su id. */
    public void eliminar(int id) throws SQLException {
        try (Connection c = conectar();
                PreparedStatement ps = c.prepareStatement("DELETE FROM vehiculos WHERE id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    /** Dice si ya existe otro vehículo con ese VIN. */
    public boolean existeVin(String vin, int idIgnorar) throws SQLException {
        try (Connection c = conectar(); PreparedStatement ps = c.prepareStatement(
                "SELECT COUNT(*) FROM vehiculos WHERE id <> ? AND vin_norm = ?")) {
            ps.setInt(1, idIgnorar);
            ps.setString(2, normalizar(vin));
            try (ResultSet r = ps.executeQuery()) {
                return r.next() && r.getInt(1) > 0;
            }
        }
    }

    /** Cuántos vehículos hay en total. */
    public int total() throws SQLException {
        try (Connection c = conectar();
                Statement st = c.createStatement();
                ResultSet r = st.executeQuery("SELECT COUNT(*) FROM vehiculos")) {
            return r.next() ? r.getInt(1) : 0;
        }
    }

    /* ============ Búsqueda con filtros ============ */

    /**
     * SELECT con filtros. Lo vacío (o -1 / 0) no filtra.
     * Todos los valores van como parámetros "?" para evitar inyección SQL.
     */
    public List<Vehiculo> buscar(String marca, String modelo, int anioDesde, int anioHasta,
            String tipo, String combustible, String estado,
            double precioMin, double precioMax, String texto) throws SQLException {

        List<String> condiciones = new ArrayList<>();
        List<Object> valores = new ArrayList<>();

        if (!marca.isEmpty()) {
            condiciones.add("marca_norm LIKE ?");
            valores.add("%" + normalizar(marca) + "%");
        }
        if (!modelo.isEmpty()) {
            condiciones.add("modelo_norm LIKE ?");
            valores.add("%" + normalizar(modelo) + "%");
        }
        if (anioDesde > 0) {
            condiciones.add("anio >= ?");
            valores.add(anioDesde);
        }
        if (anioHasta > 0) {
            condiciones.add("anio <= ?");
            valores.add(anioHasta);
        }
        if (!tipo.isEmpty()) {
            condiciones.add("tipo_norm = ?");
            valores.add(normalizar(tipo));
        }
        if (!combustible.isEmpty()) {
            condiciones.add("combustible_norm = ?");
            valores.add(normalizar(combustible));
        }
        if (!estado.isEmpty()) {
            condiciones.add("estado_norm = ?");
            valores.add(normalizar(estado));
        }
        if (precioMin > 0) {
            condiciones.add("precio >= ?");
            valores.add(precioMin);
        }
        if (precioMax > 0) {
            condiciones.add("precio <= ?");
            valores.add(precioMax);
        }
        if (!texto.isEmpty()) {
            condiciones.add("busqueda LIKE ?");
            valores.add("%" + normalizar(texto) + "%");
        }

        // Junta las condiciones con AND.
        String sql = "SELECT * FROM vehiculos";
        if (!condiciones.isEmpty()) {
            sql += " WHERE " + String.join(" AND ", condiciones);
        }
        sql += " ORDER BY marca_norm, modelo_norm, anio DESC";

        List<Vehiculo> resultado = new ArrayList<>();
        try (Connection c = conectar(); PreparedStatement ps = c.prepareStatement(sql)) {
            for (int i = 0; i < valores.size(); i++) {
                ps.setObject(i + 1, valores.get(i));
            }
            try (ResultSet r = ps.executeQuery()) {
                while (r.next()) {
                    resultado.add(leer(r));
                }
            }
        }
        return resultado;
    }

    /** Texto en minúsculas y sin acentos, para búsquedas flexibles. */
    public static String normalizar(String texto) {
        if (texto == null) {
            return "";
        }
        return Normalizer.normalize(texto.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "").toLowerCase();
    }

    /* ============ Ayudas internas ============ */

    /** Pone los 18 valores del vehículo en el INSERT/UPDATE. */
    private void llenar(PreparedStatement ps, Vehiculo v) throws SQLException {
        ps.setString(1, v.marca);
        ps.setString(2, v.modelo);
        ps.setInt(3, v.anio);
        ps.setString(4, v.tipo);
        ps.setDouble(5, v.precio);
        ps.setString(6, v.color);
        ps.setInt(7, v.kilometraje);
        ps.setString(8, v.combustible);
        ps.setString(9, v.transmision);
        ps.setString(10, v.estado);
        ps.setString(11, v.vin);
        ps.setString(12, normalizar(v.marca));
        ps.setString(13, normalizar(v.modelo));
        ps.setString(14, normalizar(v.tipo));
        ps.setString(15, normalizar(v.combustible));
        ps.setString(16, normalizar(v.estado));
        ps.setString(17, normalizar(v.vin));
        // "busqueda" concatena todos los campos para la búsqueda general.
        ps.setString(18, normalizar(v.marca + " " + v.modelo + " " + v.tipo + " "
                + v.color + " " + v.combustible + " " + v.transmision + " "
                + v.estado + " " + v.vin + " " + v.anio));
    }

    /** Convierte una fila del SELECT en un objeto Vehiculo. */
    private Vehiculo leer(ResultSet r) throws SQLException {
        Vehiculo v = new Vehiculo(r.getString("marca"), r.getString("modelo"),
                r.getInt("anio"), r.getString("tipo"), r.getDouble("precio"),
                r.getString("color"), r.getInt("kilometraje"),
                r.getString("combustible"), r.getString("transmision"),
                r.getString("estado"), r.getString("vin"));
        v.id = r.getInt("id");
        return v;
    }

    /** Descripción de la conexión para la barra de estado. */
    public String descripcion() {
        return "MySQL/MariaDB: " + usuario + "@" + host + ":" + puerto + "/" + base;
    }

    /* ============ Respaldo CSV ============ */

    /** Guarda todos los vehículos en un CSV. */
    public void exportarCsv(File destino) throws IOException, SQLException {
        try (PrintWriter pw = new PrintWriter(Files.newBufferedWriter(destino.toPath(),
                StandardCharsets.UTF_8))) {
            for (Vehiculo v : buscar("", "", 0, 0, "", "", "", 0, 0, "")) {
                pw.println(v.toCSV());
            }
        }
    }

    /** Importa vehículos de un CSV; devuelve cuántos agregó. */
    public int importarCsv(File origen) throws IOException, SQLException {
        int cuantos = 0;
        try (BufferedReader br = Files.newBufferedReader(origen.toPath(),
                StandardCharsets.UTF_8)) {
            String linea;
            while ((linea = br.readLine()) != null) {
                if (linea.trim().isEmpty()) {
                    continue;
                }
                Vehiculo v = Vehiculo.fromCSV(linea);
                if (v != null) {
                    insertar(v);
                    cuantos++;
                }
            }
        }
        return cuantos;
    }

    /** Si la tabla está vacía, agrega 16 vehículos de demostración. */
    private void cargarDemo() {
        try {
            if (total() > 0) {
                return; // ya hay datos: no se cargan los demo
            }
            insertar(new Vehiculo("Toyota", "Corolla", 2022, "Sedán", 21800, "Blanco", 12500, "Gasolina", "Automática", "Usado", "TOY-2022-COR-001"));
            insertar(new Vehiculo("Toyota", "RAV4", 2024, "SUV", 34990, "Gris", 0, "Híbrido", "CVT", "Nuevo", "TOY-2024-RAV-002"));
            insertar(new Vehiculo("Ford", "Mustang", 2021, "Coupé", 45990, "Rojo", 24000, "Gasolina", "Manual", "Usado", "FRD-2021-MUS-003"));
            insertar(new Vehiculo("Ford", "F-150", 2023, "Pickup", 52400, "Negro", 8000, "Gasolina", "Automática", "Usado", "FRD-2023-F15-004"));
            insertar(new Vehiculo("Ford", "Focus", 2019, "Hatchback", 13900, "Azul", 68000, "Gasolina", "Manual", "Usado", "FRD-2019-FOC-005"));
            insertar(new Vehiculo("Chevrolet", "Onix", 2023, "Sedán", 15900, "Plata", 3000, "Gasolina", "Manual", "Usado", "CHV-2023-ONX-006"));
            insertar(new Vehiculo("Chevrolet", "Tahoe", 2024, "SUV", 68900, "Negro", 0, "Gasolina", "Automática", "Nuevo", "CHV-2024-THO-007"));
            insertar(new Vehiculo("Nissan", "Sentra", 2020, "Sedán", 15600, "Gris", 41000, "Gasolina", "CVT", "Usado", "NSN-2020-SEN-008"));
            insertar(new Vehiculo("Nissan", "Frontier", 2022, "Pickup", 32700, "Blanco", 22000, "Diésel", "Manual", "Usado", "NSN-2022-FRT-009"));
            insertar(new Vehiculo("Tesla", "Model 3", 2023, "Sedán", 42990, "Blanco", 12000, "Eléctrico", "Automática", "Usado", "TSL-2023-M3-010"));
            insertar(new Vehiculo("Tesla", "Model Y", 2025, "SUV", 48990, "Azul", 0, "Eléctrico", "Automática", "Nuevo", "TSL-2025-MY-011"));
            insertar(new Vehiculo("Honda", "Civic", 2021, "Sedán", 19900, "Rojo", 33000, "Gasolina", "CVT", "Usado", "HND-2021-CVC-012"));
            insertar(new Vehiculo("Honda", "CR-V", 2024, "SUV", 32900, "Verde", 500, "Híbrido", "CVT", "Nuevo", "HND-2024-CRV-013"));
            insertar(new Vehiculo("Mazda", "CX-5", 2022, "SUV", 27500, "Gris", 29000, "Gasolina", "Automática", "Usado", "MZD-2022-CX5-014"));
            insertar(new Vehiculo("Kia", "Rio", 2019, "Hatchback", 12500, "Amarillo", 75000, "Gasolina", "Manual", "Usado", "KIA-2019-RIO-015"));
            insertar(new Vehiculo("Hyundai", "Tucson", 2023, "SUV", 28900, "Azul", 9500, "Gasolina", "Automática", "Usado", "HYD-2023-TCS-016"));
        } catch (SQLException e) {
            throw new RuntimeException("No se pudo cargar la demo: " + e.getMessage(), e);
        }
    }
}
