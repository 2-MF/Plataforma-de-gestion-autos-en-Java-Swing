#!/usr/bin/env bash
# Compila el proyecto y genera GestionAutos.jar.
# El driver JDBC de MariaDB/MySQL (lib/mariadb-java-client.jar) se
# referencia desde el Class-Path del manifest, asi que debe quedarse junto al .jar.
set -e
cd "$(dirname "$0")"

if [ ! -f lib/mariadb-java-client.jar ]; then
    echo "ERROR: falta lib/mariadb-java-client.jar (driver JDBC de MariaDB/MySQL)." >&2
    exit 1
fi

rm -rf out
mkdir -p out
find src -name "*.java" > out/sources.txt
javac -encoding UTF-8 -cp lib/mariadb-java-client.jar -d out @out/sources.txt
rm out/sources.txt
printf 'Manifest-Version: 1.0\nMain-Class: com.tiendaautos.Main\nClass-Path: lib/mariadb-java-client.jar\n' > out/MANIFEST.MF
jar cfm GestionAutos.jar out/MANIFEST.MF -C out .
echo "Listo: GestionAutos.jar"
