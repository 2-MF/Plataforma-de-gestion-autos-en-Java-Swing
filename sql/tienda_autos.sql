-- ============================================================
-- AutoCenter J-Swing — Script de la base de datos (MySQL/MariaDB)
-- ============================================================
-- Crea el usuario de la aplicación, la base de datos tienda_autos,
-- la tabla vehiculos y los 16 vehículos de demostración.
--
-- Uso (con un usuario administrador, p. ej. root del sistema):
--   sudo mysql < sql/tienda_autos.sql
--
-- Nota: la aplicación crea todo esto automáticamente al arrancar,
-- este script sirve para preparar el servidor a mano o para
-- documentar el esquema.
--
-- ¿Por qué columnas *_norm? Guardan el texto en minúsculas y sin
-- acentos para que las búsquedas tipo LIKE encuentren "Coupe" al
-- escribir "coupe". La columna "busqueda" concatena todos los
-- campos para la búsqueda general.
-- ============================================================

-- Usuario que usa la aplicación (edítalo si quieres otro)
CREATE USER IF NOT EXISTS 'tienda'@'localhost' IDENTIFIED BY 'tienda123';
CREATE USER IF NOT EXISTS 'tienda'@'127.0.0.1' IDENTIFIED BY 'tienda123';
GRANT ALL PRIVILEGES ON *.* TO 'tienda'@'localhost';
GRANT ALL PRIVILEGES ON *.* TO 'tienda'@'127.0.0.1';
FLUSH PRIVILEGES;

CREATE DATABASE IF NOT EXISTS tienda_autos
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE tienda_autos;

CREATE TABLE IF NOT EXISTS vehiculos (
    id INT AUTO_INCREMENT PRIMARY KEY,
    marca VARCHAR(60) NOT NULL,
    modelo VARCHAR(60) NOT NULL,
    anio INT NOT NULL,
    tipo VARCHAR(40) NOT NULL,
    precio DOUBLE NOT NULL,
    color VARCHAR(40) NOT NULL,
    kilometraje INT NOT NULL,
    combustible VARCHAR(40) NOT NULL,
    transmision VARCHAR(40) NOT NULL,
    estado VARCHAR(20) NOT NULL,
    vin VARCHAR(60) NOT NULL,
    marca_norm VARCHAR(80) NOT NULL,
    modelo_norm VARCHAR(80) NOT NULL,
    tipo_norm VARCHAR(60) NOT NULL,
    combustible_norm VARCHAR(60) NOT NULL,
    estado_norm VARCHAR(40) NOT NULL,
    vin_norm VARCHAR(80) NOT NULL,
    busqueda VARCHAR(500) NOT NULL
);

-- Datos de demostración (solo para instalaciones nuevas)
INSERT INTO vehiculos (marca, modelo, anio, tipo, precio, color, kilometraje, combustible, transmision, estado, vin, marca_norm, modelo_norm, tipo_norm, combustible_norm, estado_norm, vin_norm, busqueda) VALUES ('Toyota', 'Corolla', 2022, 'Sedán', 21800.0, 'Blanco', 12500, 'Gasolina', 'Automática', 'Usado', 'TOY-2022-COR-001', 'toyota', 'corolla', 'sedan', 'gasolina', 'usado', 'toy-2022-cor-001', 'toyota corolla sedan blanco gasolina automatica usado toy-2022-cor-001 2022');
INSERT INTO vehiculos (marca, modelo, anio, tipo, precio, color, kilometraje, combustible, transmision, estado, vin, marca_norm, modelo_norm, tipo_norm, combustible_norm, estado_norm, vin_norm, busqueda) VALUES ('Toyota', 'RAV4', 2024, 'SUV', 34990.0, 'Gris', 0, 'Híbrido', 'CVT', 'Nuevo', 'TOY-2024-RAV-002', 'toyota', 'rav4', 'suv', 'hibrido', 'nuevo', 'toy-2024-rav-002', 'toyota rav4 suv gris hibrido cvt nuevo toy-2024-rav-002 2024');
INSERT INTO vehiculos (marca, modelo, anio, tipo, precio, color, kilometraje, combustible, transmision, estado, vin, marca_norm, modelo_norm, tipo_norm, combustible_norm, estado_norm, vin_norm, busqueda) VALUES ('Ford', 'Mustang', 2021, 'Coupé', 45990.0, 'Rojo', 24000, 'Gasolina', 'Manual', 'Usado', 'FRD-2021-MUS-003', 'ford', 'mustang', 'coupe', 'gasolina', 'usado', 'frd-2021-mus-003', 'ford mustang coupe rojo gasolina manual usado frd-2021-mus-003 2021');
INSERT INTO vehiculos (marca, modelo, anio, tipo, precio, color, kilometraje, combustible, transmision, estado, vin, marca_norm, modelo_norm, tipo_norm, combustible_norm, estado_norm, vin_norm, busqueda) VALUES ('Ford', 'F-150', 2023, 'Pickup', 52400.0, 'Negro', 8000, 'Gasolina', 'Automática', 'Usado', 'FRD-2023-F15-004', 'ford', 'f-150', 'pickup', 'gasolina', 'usado', 'frd-2023-f15-004', 'ford f-150 pickup negro gasolina automatica usado frd-2023-f15-004 2023');
INSERT INTO vehiculos (marca, modelo, anio, tipo, precio, color, kilometraje, combustible, transmision, estado, vin, marca_norm, modelo_norm, tipo_norm, combustible_norm, estado_norm, vin_norm, busqueda) VALUES ('Ford', 'Focus', 2019, 'Hatchback', 13900.0, 'Azul', 68000, 'Gasolina', 'Manual', 'Usado', 'FRD-2019-FOC-005', 'ford', 'focus', 'hatchback', 'gasolina', 'usado', 'frd-2019-foc-005', 'ford focus hatchback azul gasolina manual usado frd-2019-foc-005 2019');
INSERT INTO vehiculos (marca, modelo, anio, tipo, precio, color, kilometraje, combustible, transmision, estado, vin, marca_norm, modelo_norm, tipo_norm, combustible_norm, estado_norm, vin_norm, busqueda) VALUES ('Chevrolet', 'Onix', 2023, 'Sedán', 15900.0, 'Plata', 3000, 'Gasolina', 'Manual', 'Usado', 'CHV-2023-ONX-006', 'chevrolet', 'onix', 'sedan', 'gasolina', 'usado', 'chv-2023-onx-006', 'chevrolet onix sedan plata gasolina manual usado chv-2023-onx-006 2023');
INSERT INTO vehiculos (marca, modelo, anio, tipo, precio, color, kilometraje, combustible, transmision, estado, vin, marca_norm, modelo_norm, tipo_norm, combustible_norm, estado_norm, vin_norm, busqueda) VALUES ('Chevrolet', 'Tahoe', 2024, 'SUV', 68900.0, 'Negro', 0, 'Gasolina', 'Automática', 'Nuevo', 'CHV-2024-THO-007', 'chevrolet', 'tahoe', 'suv', 'gasolina', 'nuevo', 'chv-2024-tho-007', 'chevrolet tahoe suv negro gasolina automatica nuevo chv-2024-tho-007 2024');
INSERT INTO vehiculos (marca, modelo, anio, tipo, precio, color, kilometraje, combustible, transmision, estado, vin, marca_norm, modelo_norm, tipo_norm, combustible_norm, estado_norm, vin_norm, busqueda) VALUES ('Nissan', 'Sentra', 2020, 'Sedán', 15600.0, 'Gris', 41000, 'Gasolina', 'CVT', 'Usado', 'NSN-2020-SEN-008', 'nissan', 'sentra', 'sedan', 'gasolina', 'usado', 'nsn-2020-sen-008', 'nissan sentra sedan gris gasolina cvt usado nsn-2020-sen-008 2020');
INSERT INTO vehiculos (marca, modelo, anio, tipo, precio, color, kilometraje, combustible, transmision, estado, vin, marca_norm, modelo_norm, tipo_norm, combustible_norm, estado_norm, vin_norm, busqueda) VALUES ('Nissan', 'Frontier', 2022, 'Pickup', 32700.0, 'Blanco', 22000, 'Diésel', 'Manual', 'Usado', 'NSN-2022-FRT-009', 'nissan', 'frontier', 'pickup', 'diesel', 'usado', 'nsn-2022-frt-009', 'nissan frontier pickup blanco diesel manual usado nsn-2022-frt-009 2022');
INSERT INTO vehiculos (marca, modelo, anio, tipo, precio, color, kilometraje, combustible, transmision, estado, vin, marca_norm, modelo_norm, tipo_norm, combustible_norm, estado_norm, vin_norm, busqueda) VALUES ('Tesla', 'Model 3', 2023, 'Sedán', 42990.0, 'Blanco', 12000, 'Eléctrico', 'Automática', 'Usado', 'TSL-2023-M3-010', 'tesla', 'model 3', 'sedan', 'electrico', 'usado', 'tsl-2023-m3-010', 'tesla model 3 sedan blanco electrico automatica usado tsl-2023-m3-010 2023');
INSERT INTO vehiculos (marca, modelo, anio, tipo, precio, color, kilometraje, combustible, transmision, estado, vin, marca_norm, modelo_norm, tipo_norm, combustible_norm, estado_norm, vin_norm, busqueda) VALUES ('Tesla', 'Model Y', 2025, 'SUV', 48990.0, 'Azul', 0, 'Eléctrico', 'Automática', 'Nuevo', 'TSL-2025-MY-011', 'tesla', 'model y', 'suv', 'electrico', 'nuevo', 'tsl-2025-my-011', 'tesla model y suv azul electrico automatica nuevo tsl-2025-my-011 2025');
INSERT INTO vehiculos (marca, modelo, anio, tipo, precio, color, kilometraje, combustible, transmision, estado, vin, marca_norm, modelo_norm, tipo_norm, combustible_norm, estado_norm, vin_norm, busqueda) VALUES ('Honda', 'Civic', 2021, 'Sedán', 19900.0, 'Rojo', 33000, 'Gasolina', 'CVT', 'Usado', 'HND-2021-CVC-012', 'honda', 'civic', 'sedan', 'gasolina', 'usado', 'hnd-2021-cvc-012', 'honda civic sedan rojo gasolina cvt usado hnd-2021-cvc-012 2021');
INSERT INTO vehiculos (marca, modelo, anio, tipo, precio, color, kilometraje, combustible, transmision, estado, vin, marca_norm, modelo_norm, tipo_norm, combustible_norm, estado_norm, vin_norm, busqueda) VALUES ('Honda', 'CR-V', 2024, 'SUV', 32900.0, 'Verde', 500, 'Híbrido', 'CVT', 'Nuevo', 'HND-2024-CRV-013', 'honda', 'cr-v', 'suv', 'hibrido', 'nuevo', 'hnd-2024-crv-013', 'honda cr-v suv verde hibrido cvt nuevo hnd-2024-crv-013 2024');
INSERT INTO vehiculos (marca, modelo, anio, tipo, precio, color, kilometraje, combustible, transmision, estado, vin, marca_norm, modelo_norm, tipo_norm, combustible_norm, estado_norm, vin_norm, busqueda) VALUES ('Mazda', 'CX-5', 2022, 'SUV', 27500.0, 'Gris', 29000, 'Gasolina', 'Automática', 'Usado', 'MZD-2022-CX5-014', 'mazda', 'cx-5', 'suv', 'gasolina', 'usado', 'mzd-2022-cx5-014', 'mazda cx-5 suv gris gasolina automatica usado mzd-2022-cx5-014 2022');
INSERT INTO vehiculos (marca, modelo, anio, tipo, precio, color, kilometraje, combustible, transmision, estado, vin, marca_norm, modelo_norm, tipo_norm, combustible_norm, estado_norm, vin_norm, busqueda) VALUES ('Kia', 'Rio', 2019, 'Hatchback', 12500.0, 'Amarillo', 75000, 'Gasolina', 'Manual', 'Usado', 'KIA-2019-RIO-015', 'kia', 'rio', 'hatchback', 'gasolina', 'usado', 'kia-2019-rio-015', 'kia rio hatchback amarillo gasolina manual usado kia-2019-rio-015 2019');
INSERT INTO vehiculos (marca, modelo, anio, tipo, precio, color, kilometraje, combustible, transmision, estado, vin, marca_norm, modelo_norm, tipo_norm, combustible_norm, estado_norm, vin_norm, busqueda) VALUES ('Hyundai', 'Tucson', 2023, 'SUV', 28900.0, 'Azul', 9500, 'Gasolina', 'Automática', 'Usado', 'HYD-2023-TCS-016', 'hyundai', 'tucson', 'suv', 'gasolina', 'usado', 'hyd-2023-tcs-016', 'hyundai tucson suv azul gasolina automatica usado hyd-2023-tcs-016 2023');
-- Fin del script
