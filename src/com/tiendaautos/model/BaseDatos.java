package com.tiendaautos.model;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Capa de acceso a la base de datos SQL (MySQL/MariaDB) usando JDBC.
 * Contiene las sentencias SQL: CREATE DATABASE, CREATE TABLE, INSERT,
 * SELECT con filtros dinámicos en el WHERE, UPDATE y DELETE. Todas las
 * consultas usan PreparedStatement con parámetros para evitar inyección
 * SQL. La conexión se configura en el archivo db.properties.
 */
public class BaseDatos {

    /** Valores por defecto si no existe db.properties. */
    private static final Properties CONFIG_DEFECTO = new Properties();

    static {
        CONFIG_DEFECTO.setProperty("host", "localhost");
        CONFIG_DEFECTO.setProperty("puerto", "3306");
        CONFIG_DEFECTO.setProperty("baseDatos", "tienda_autos");
        CONFIG_DEFECTO.setProperty("usuario", "tienda");
        CONFIG_DEFECTO.setProperty("contrasena", "tienda123");
    }

    public BaseDatos() {
    }

    /** Lee db.properties (si existe) sobre los valores por defecto. */
    private static Properties config() {
        Properties config = new Properties(CONFIG_DEFECTO);
        File archivo = new File("db.properties");
        if (archivo.isFile()) {
            try (Reader lector = Files.newBufferedReader(archivo.toPath(),
                    StandardCharsets.UTF_8)) {
                config.load(lector);
            } catch (IOException ex) {
                // Si falla la lectura se usan los valores por defecto.
            }
        }
        return config;
    }

    /** URL JDBC. baseDatos puede ser "" para conectarse sin elegir una. */
    private static String url(String baseDatos) {
        Properties c = config();
        return "jdbc:mariadb://" + c.getProperty("host") + ":" + c.getProperty("puerto")
                + "/" + baseDatos;
    }

    private Connection conectar() throws SQLException {
        try {
            Class.forName("org.mariadb.jdbc.Driver");
        } catch (ClassNotFoundException ex) {
            throw new SQLException("No se encontró el driver JDBC de MariaDB/MySQL "
                    + "(lib/mariadb-java-client.jar).", ex);
        }
        Properties c = config();
        return DriverManager.getConnection(url(c.getProperty("baseDatos")),
                c.getProperty("usuario"), c.getProperty("contrasena"));
    }

    /** Descripción de la conexión para la barra de estado. */
    public static String descripcion() {
        Properties c = config();
        return "MySQL/MariaDB: " + c.getProperty("usuario") + "@" + c.getProperty("host")
                + ":" + c.getProperty("puerto") + "/" + c.getProperty("baseDatos");
    }

    /**
     * Crea la base de datos y la tabla si no existen. Las columnas
     * *_norm guardan el texto normalizado (minúsculas y sin acentos)
     * para búsquedas flexibles, y "busqueda" concatena todos los campos
     * para la búsqueda general.
     */
    public void inicializar() throws SQLException {
        Properties c = config();

        // 1) Crear la base de datos si no existe (conectando sin elegir una)
        try (Connection cn = DriverManager.getConnection(url(""),
                c.getProperty("usuario"), c.getProperty("contrasena"));
                Statement st = cn.createStatement()) {
            st.execute("CREATE DATABASE IF NOT EXISTS `" + c.getProperty("baseDatos")
                    + "` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");
        }

        // 2) Crear la tabla si no existe
        String sql = "CREATE TABLE IF NOT EXISTS vehiculos ("
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
        try (Connection cn = conectar(); Statement st = cn.createStatement()) {
            st.execute(sql);
        }
    }

    /** SELECT con WHERE dinámico según los criterios del filtro. */
    public List<Vehiculo> consultar(FiltroVehiculo f) throws SQLException {
        StringBuilder sql = new StringBuilder(
                "SELECT id, marca, modelo, anio, tipo, precio, color, kilometraje, "
                + "combustible, transmision, estado, vin FROM vehiculos WHERE 1=1");
        List<Object> parametros = new ArrayList<>();

        if (!f.getMarca().isEmpty()) {
            sql.append(" AND marca_norm LIKE ?");
            parametros.add("%" + f.getMarca() + "%");
        }
        if (!f.getModelo().isEmpty()) {
            sql.append(" AND modelo_norm LIKE ?");
            parametros.add("%" + f.getModelo() + "%");
        }
        if (f.getAnioDesde() != null) {
            sql.append(" AND anio >= ?");
            parametros.add(f.getAnioDesde());
        }
        if (f.getAnioHasta() != null) {
            sql.append(" AND anio <= ?");
            parametros.add(f.getAnioHasta());
        }
        if (f.getTipoAFiltrar() != null) {
            sql.append(" AND tipo_norm = ?");
            parametros.add(f.getTipoAFiltrar());
        }
        if (f.getCombustibleAFiltrar() != null) {
            sql.append(" AND combustible_norm = ?");
            parametros.add(f.getCombustibleAFiltrar());
        }
        if (f.getEstadoAFiltrar() != null) {
            sql.append(" AND estado_norm = ?");
            parametros.add(f.getEstadoAFiltrar());
        }
        if (f.getPrecioMin() != null) {
            sql.append(" AND precio >= ?");
            parametros.add(f.getPrecioMin());
        }
        if (f.getPrecioMax() != null) {
            sql.append(" AND precio <= ?");
            parametros.add(f.getPrecioMax());
        }
        if (!f.getTextoLibre().isEmpty()) {
            sql.append(" AND busqueda LIKE ?");
            parametros.add("%" + f.getTextoLibre() + "%");
        }
        sql.append(" ORDER BY marca_norm, modelo_norm, anio DESC");

        List<Vehiculo> resultado = new ArrayList<>();
        try (Connection c = conectar();
                PreparedStatement ps = c.prepareStatement(sql.toString())) {
            for (int i = 0; i < parametros.size(); i++) {
                ps.setObject(i + 1, parametros.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    resultado.add(leerVehiculo(rs));
                }
            }
        }
        return resultado;
    }

    /** INSERT. Devuelve el id generado por AUTOINCREMENT. */
    public int insertar(Vehiculo v) throws SQLException {
        String sql = "INSERT INTO vehiculos (marca, modelo, anio, tipo, precio, color, "
                + "kilometraje, combustible, transmision, estado, vin, "
                + "marca_norm, modelo_norm, tipo_norm, combustible_norm, estado_norm, "
                + "vin_norm, busqueda) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        try (Connection c = conectar();
                PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            llenarParametros(ps, v);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return -1;
    }

    /** UPDATE por id. */
    public void actualizar(Vehiculo v) throws SQLException {
        String sql = "UPDATE vehiculos SET marca=?, modelo=?, anio=?, tipo=?, precio=?, "
                + "color=?, kilometraje=?, combustible=?, transmision=?, estado=?, vin=?, "
                + "marca_norm=?, modelo_norm=?, tipo_norm=?, combustible_norm=?, estado_norm=?, "
                + "vin_norm=?, busqueda=? WHERE id=?";
        try (Connection c = conectar(); PreparedStatement ps = c.prepareStatement(sql)) {
            llenarParametros(ps, v);
            ps.setInt(19, v.getId());
            ps.executeUpdate();
        }
    }

    /** DELETE por id. */
    public boolean eliminar(int id) throws SQLException {
        try (Connection c = conectar();
                PreparedStatement ps = c.prepareStatement("DELETE FROM vehiculos WHERE id=?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    /** Comprueba si ya existe otro vehículo con ese VIN (ignorando un id). */
    public boolean existeVin(String vin, int idIgnorar) throws SQLException {
        String sql = "SELECT COUNT(*) FROM vehiculos WHERE id <> ? AND vin_norm = ?";
        try (Connection c = conectar(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, idIgnorar);
            ps.setString(2, FiltroVehiculo.normalizar(vin));
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    /** SELECT COUNT(*) sobre la tabla. */
    public int total() throws SQLException {
        try (Connection c = conectar();
                Statement st = c.createStatement();
                ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM vehiculos")) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    /* ================= Helpers ================= */

    private static Vehiculo leerVehiculo(ResultSet rs) throws SQLException {
        Vehiculo v = new Vehiculo(rs.getString("marca"), rs.getString("modelo"),
                rs.getInt("anio"), rs.getString("tipo"), rs.getDouble("precio"),
                rs.getString("color"), rs.getInt("kilometraje"),
                rs.getString("combustible"), rs.getString("transmision"),
                rs.getString("estado"), rs.getString("vin"));
        v.setId(rs.getInt("id"));
        return v;
    }

    private static void llenarParametros(PreparedStatement ps, Vehiculo v) throws SQLException {
        ps.setString(1, v.getMarca());
        ps.setString(2, v.getModelo());
        ps.setInt(3, v.getAnio());
        ps.setString(4, v.getTipo());
        ps.setDouble(5, v.getPrecio());
        ps.setString(6, v.getColor());
        ps.setInt(7, v.getKilometraje());
        ps.setString(8, v.getCombustible());
        ps.setString(9, v.getTransmision());
        ps.setString(10, v.getEstado());
        ps.setString(11, v.getVin());
        ps.setString(12, FiltroVehiculo.normalizar(v.getMarca()));
        ps.setString(13, FiltroVehiculo.normalizar(v.getModelo()));
        ps.setString(14, FiltroVehiculo.normalizar(v.getTipo()));
        ps.setString(15, FiltroVehiculo.normalizar(v.getCombustible()));
        ps.setString(16, FiltroVehiculo.normalizar(v.getEstado()));
        ps.setString(17, FiltroVehiculo.normalizar(v.getVin()));
        ps.setString(18, FiltroVehiculo.normalizar(v.getMarca() + " " + v.getModelo() + " "
                + v.getTipo() + " " + v.getColor() + " " + v.getCombustible() + " "
                + v.getTransmision() + " " + v.getEstado() + " " + v.getVin() + " "
                + v.getAnio()));
    }
}
