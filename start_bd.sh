#!/usr/bin/env bash
# Inicia el servidor MariaDB del usuario (127.0.0.1:3306), donde vive la
# base de datos tienda_autos de AutoCenter J-Swing.
# Úsalo si reiniciaste la máquina o el servidor se apagó.
set -e
cd "$(dirname "$0")"

DATADIR="$HOME/.tienda_mariadb"
SOCKET="/tmp/mysqld_dev.sock"
LOG="$HOME/.tienda_mariadb_servidor.log"

if ss -tln 2>/dev/null | grep -q ':3306 '; then
    echo "El servidor MariaDB ya está corriendo en el puerto 3306."
    exit 0
fi

if [ ! -d "$DATADIR/mysql" ]; then
    echo "Primera vez: inicializando la base de datos..."
    mariadb-install-db --datadir="$DATADIR" \
        --auth-root-authentication-method=normal --skip-test-db > /dev/null
fi

nohup mariadbd --datadir="$DATADIR" --socket="$SOCKET" \
    --port=3306 --bind-address=127.0.0.1 \
    --pid-file=/tmp/mysqld_dev.pid >> "$LOG" 2>&1 &

for i in $(seq 1 20); do
    ss -tln 2>/dev/null | grep -q ':3306 ' && break
    sleep 1
done

if ss -tln 2>/dev/null | grep -q ':3306 '; then
    # En una instalación nueva, crea el usuario que usa la aplicación
    if [ ! -d "$DATADIR/tienda_autos" ]; then
        mariadb -S "$SOCKET" -u root -e \
            "CREATE USER IF NOT EXISTS 'tienda'@'localhost' IDENTIFIED BY 'tienda123'; \
             CREATE USER IF NOT EXISTS 'tienda'@'127.0.0.1' IDENTIFIED BY 'tienda123'; \
             GRANT ALL PRIVILEGES ON *.* TO 'tienda'@'localhost'; \
             GRANT ALL PRIVILEGES ON *.* TO 'tienda'@'127.0.0.1'; FLUSH PRIVILEGES;" || true
    fi
    echo "Servidor MariaDB corriendo en 127.0.0.1:3306."
    echo "Conectarse con:  mysql -u tienda -ptienda123 tienda_autos"
else
    echo "No se pudo iniciar el servidor. Revisa el log: $LOG"
    exit 1
fi
