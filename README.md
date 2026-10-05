# AutoCenter J-Swing — Plataforma de gestión para tienda de autos

App de escritorio en **Java Swing** con base de datos **MySQL/MariaDB** (JDBC, sin Spring Boot).

## Requisitos
- JDK 11 o superior (recomendado 17/21).
- Servidor MySQL o MariaDB corriendo. En Arch Linux:
  ```bash
  sudo systemctl enable --now mariadb   # arranca ahora y en cada reinicio
  sudo mariadb -e "CREATE USER IF NOT EXISTS 'tienda'@'localhost' IDENTIFIED BY 'tienda123'; GRANT ALL PRIVILEGES ON *.* TO 'tienda'@'localhost'; FLUSH PRIVILEGES;"
  ```

## Compilar y ejecutar
- **Linux** (Hyprland/Sway, vía XWayland): `./build.sh` y luego `./run.sh`
- **Windows 11**: `build.bat` y `run.bat`

## Base de datos
- La app crea sola la base `tienda_autos`, la tabla `vehiculos` y 16 autos de demostración (solo si la tabla está vacía).
- La conexión se ajusta en **`db.properties`**. Por defecto: usuario `tienda`, contraseña `tienda123`, en `localhost:3306`.
- Script completo por si prefieres crearla a mano: `sudo mysql < sql/tienda_autos.sql`
- Para ver los datos: `mysql -u tienda -ptienda123 tienda_autos` — o en Workbench: Hostname `127.0.0.1`, Port `3306`, usuario `tienda`.
- Si el servidor está apagado, `./start_bd.sh` levanta uno local sin sudo.

## Qué hace
- Ventana MDI (`JDesktopPane`) con menús (`JMenuBar` / `JMenu`) y ventanas internas.
- **Filtros SQL**: marca, modelo, año desde/hasta, tipo de vehículo, combustible, estado, precio mín/máx y búsqueda general. Tabla ordenable con clic en los encabezados.
- Agregar, editar (doble clic en la fila) y eliminar vehículos, con validaciones y VIN único.
- Exportar / importar respaldo CSV desde el menú Sistema.

Componentes: JLabel, JTextField, JButton, JScrollPane, JMenuBar, JMenu, JDesktopPane, JTable, JComboBox, JOptionPane, JFileChooser.

## Código
6 clases en el paquete `com.tiendaautos`:

| Clase | Qué hace |
|---|---|
| `Main` | Arranque |
| `Vehiculo` | El vehículo y su respaldo CSV |
| `BaseDatos` | Todo el SQL (JDBC): filtros, altas, cambios, bajas y respaldo |
| `VentanaPrincipal` | Menús, escritorio y barra de estado |
| `VentanaListado` | Filtros y tabla de resultados |
| `VentanaFormulario` | Agregar / editar vehículo |

El driver JDBC está en `lib/mariadb-java-client.jar` y debe quedarse junto al `.jar`.
