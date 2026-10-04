# Proyecto_J_Swing — AutoCenter J-Swing

Plataforma de gestión para una tienda/concesionaria de autos, hecha con **Java Swing** y **base de datos MySQL/MariaDB** accedida con **JDBC**. Sin Spring Boot. En Arch Linux el servidor MySQL es MariaDB (`mariadb`), 100% compatible.

## Requisitos
- JDK 11 o superior (recomendado JDK 17/21).
- Servidor MySQL o MariaDB corriendo (en Arch: `mariadb`; en Windows: MySQL Server o XAMPP).
- El driver JDBC ya está incluido en `lib/mariadb-java-client.jar` (única dependencia).

## Configurar la base de datos
La conexión se define en **`db.properties`** (junto al .jar):
```properties
host=localhost
puerto=3306
baseDatos=tienda_autos
usuario=tienda
contrasena=tienda123
```
La aplicación **crea la base de datos y la tabla automáticamente** al arrancar (`CREATE DATABASE IF NOT EXISTS` + `CREATE TABLE IF NOT EXISTS`), y si la tabla está vacía inserta 16 vehículos de demostración. También puedes preparar todo a mano ejecutando el script completo `sql/tienda_autos.sql` con un usuario administrador: `sudo mysql < sql/tienda_autos.sql`.

### Preparar el servidor (una sola vez)
```bash
# Arch Linux (MariaDB):
sudo mariadb-install-db --user=mysql --basedir=/usr --datadir=/var/lib/mysql   # solo la primera vez
sudo systemctl start mariadb
sudo mariadb -e "CREATE USER IF NOT EXISTS 'tienda'@'localhost' IDENTIFIED BY 'tienda123'; GRANT ALL PRIVILEGES ON *.* TO 'tienda'@'localhost'; FLUSH PRIVILEGES;"
```
```sql
-- Windows (MySQL): desde MySQL Shell o HeidiSQL
CREATE USER IF NOT EXISTS 'tienda'@'localhost' IDENTIFIED BY 'tienda123';
GRANT ALL PRIVILEGES ON *.* TO 'tienda'@'localhost';
FLUSH PRIVILEGES;
```
O simplemente edita `db.properties` con tu usuario real (por ejemplo `root` y tu contraseña).

## ¿Dónde está la base de datos? ¿Cómo la abro?
- **No es un archivo que se abra directamente**: es el servidor **MariaDB** corriendo en `127.0.0.1:3306`. La base se llama **`tienda_autos`** y la tabla **`vehiculos`**.
- **¿En qué carpeta viven los archivos?** Depende del servidor que uses (compruébalo con `mysql -u tienda -ptienda123 -e "SELECT @@datadir"`):
  - **Servicio del sistema** (`sudo systemctl start mariadb`, lo recomendado): `/var/lib/mysql`
  - **Servidor de usuario** (`./start_bd.sh`, no necesita sudo): `~/.tienda_mariadb`
- **Para que el servicio del sistema arranque solo al encender la PC** (recomendado, hazlo una vez):
  ```bash
  sudo systemctl enable --now mariadb
  ```
- **Desde la terminal**:
  ```bash
  mysql -u tienda -ptienda123 tienda_autos
  # luego: SHOW TABLES; SELECT * FROM vehiculos;
  ```
- **Desde MySQL Workbench** (conexión nueva, método *Standard (TCP/IP)*):
  - Hostname: `127.0.0.1` · Port: `3306`
  - Username: `tienda` · Password: `tienda123` · Default Schema: `tienda_autos`
  - Ojo: el usuario `root` del servidor normalmente NO acepta conexiones TCP (usa `tienda`).
  - Workbench está diseñado para MySQL; con MariaDB suele funcionar, pero si da errores usa la terminal o DBeaver.
- **phpMyAdmin no está instalado**: es una aplicación web aparte (necesita Apache/nginx + PHP), no parte del servidor MySQL. No lo necesitas para este proyecto.

## Compilar y ejecutar

### Arch Linux (Hyprland o Sway)
```bash
./build.sh    # compila y genera GestionAutos.jar
./run.sh      # ejecuta la aplicación
```
- Swing funciona en Hyprland y Sway a través de **XWayland** (ambos lo incluyen por defecto).
- Para lanzarla desde wofi/rofi: edita la ruta `Exec=` de `autocenter-jswing.desktop` y cópiala a `~/.local/share/applications/`.

### Windows 11
1. Instala el JDK (Temurin 17/21 desde https://adoptium.net) y MySQL Server (o XAMPP).
2. Doble clic a `build.bat` (compila) y luego a `run.bat` (ejecuta).

## Esquema de la tabla (SQL)
```sql
CREATE TABLE IF NOT EXISTS vehiculos (
  id INT AUTO_INCREMENT PRIMARY KEY,
  marca VARCHAR(60) NOT NULL,      modelo VARCHAR(60) NOT NULL,
  anio INT NOT NULL,               tipo VARCHAR(40) NOT NULL,
  precio DOUBLE NOT NULL,          color VARCHAR(40) NOT NULL,
  kilometraje INT NOT NULL,        combustible VARCHAR(40) NOT NULL,
  transmision VARCHAR(40) NOT NULL,estado VARCHAR(20) NOT NULL,
  vin VARCHAR(60) NOT NULL,
  -- columnas normalizadas para búsquedas sin mayúsculas/acentos:
  marca_norm VARCHAR(80) NOT NULL, modelo_norm VARCHAR(80) NOT NULL,
  tipo_norm VARCHAR(60) NOT NULL,  combustible_norm VARCHAR(60) NOT NULL,
  estado_norm VARCHAR(40) NOT NULL, vin_norm VARCHAR(80) NOT NULL,
  busqueda VARCHAR(500) NOT NULL
);
```
Sentencias usadas (todas con `PreparedStatement`): `CREATE DATABASE`, `CREATE TABLE`, `INSERT` (AUTO_INCREMENT), **`SELECT ... WHERE` dinámico** según los filtros con `ORDER BY`, `UPDATE ... WHERE id=?`, `DELETE ... WHERE id=?`, `SELECT COUNT(*)`.

## Funciones
- **MDI con JDesktopPane** y ventanas internas (JInternalFrame): "Listado y filtros" y "Agregar/Editar vehículo".
- **JMenuBar / JMenu / JMenuItem**: Sistema (respaldos CSV, salir), Vehículos (listado, agregar), Ventanas (cascada, mosaico, cerrar todas) y Ayuda.
- **Filtros** (consultas SQL): marca, modelo, año desde/hasta, tipo de vehículo, combustible, estado (nuevo/usado), rango de precio y búsqueda general (auto, VIN, color…). Botones "Filtrar" y "Limpiar filtros"; Enter en un campo también filtra.
- **Tabla (JTable dentro de JScrollPane)**, ordenable al hacer clic en los encabezados.
- **Alta** con validaciones (marca/modelo/año/precio obligatorios, VIN único consultado en la BD); doble clic para **editar** (UPDATE); botón para **eliminar** (DELETE) con confirmación.
- Menú Sistema: **exportar/importar respaldo CSV** (JFileChooser).

## Componentes de Java Swing usados
**JLabel, JTextField, JButton, JScrollPane, JMenuBar, JMenu, JMenuItem, JDesktopPane**, JInternalFrame, JTable, JComboBox, JOptionPane, JFileChooser.

## Estructura
```
Proyecto_J_Swing/
├── src/com/tiendaautos/
│   ├── Main.java
│   ├── model/    → Vehiculo, FiltroVehiculo, Inventario, BaseDatos (JDBC)
│   └── ui/       → FramePrincipal, InternalListado, InternalAgregar, ModeloTablaVehiculos
├── lib/mariadb-java-client.jar   (driver JDBC; debe quedar junto al .jar)
├── db.properties                 (configuración de la conexión)
├── sql/tienda_autos.sql          (script de la BD: usuario, base, tabla y datos demo)
├── build.sh / run.sh             (Linux)
├── build.bat / run.bat           (Windows 11)
└── autocenter-jswing.desktop
```

## Notas
- Atajos: Ctrl+E exportar respaldo, Ctrl+L listado, Ctrl+N agregar, Ctrl+Q salir.
- Si la app no conecta, revisa que el servidor esté corriendo y los datos de `db.properties` (la ventana de error lo explica).
- Si pierdes `lib/mariadb-java-client.jar`, descárgalo de Maven Central: `https://repo1.maven.org/maven2/org/mariadb/jdbc/mariadb-java-client/`
