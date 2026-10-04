#!/usr/bin/env bash
# Ejecuta la aplicacion (compila antes si es necesario y avisa si la
# base de datos no responde).
cd "$(dirname "$0")"
if [ ! -f GestionAutos.jar ]; then
    ./build.sh
fi
if ! ss -tln 2>/dev/null | grep -q ':3306 '; then
    echo "AVISO: el servidor MySQL/MariaDB no esta corriendo (puerto 3306)."
    echo "  - Servicio del sistema (recomendado):  sudo systemctl start mariadb"
    echo "  - O servidor local sin sudo:            ./start_bd.sh"
    echo "Abriendo la app de todos modos..."
fi
exec java -jar GestionAutos.jar "$@"


