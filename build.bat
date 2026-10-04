@echo off
rem Compila el proyecto y genera GestionAutos.jar.
rem El driver JDBC de MariaDB/MySQL (lib\mariadb-java-client.jar) se
rem referencia desde el Class-Path del manifest, asi que debe quedarse junto al .jar.
cd /d "%~dp0"
if not exist lib\mariadb-java-client.jar (
    echo ERROR: falta lib\mariadb-java-client.jar (driver JDBC de MariaDB/MySQL)
    pause
    exit /b 1
)
if exist out rmdir /s /q out
mkdir out
dir /s /b src\*.java > out\sources.txt
javac -encoding UTF-8 -cp lib\mariadb-java-client.jar -d out @out\sources.txt
del out\sources.txt
> out\MANIFEST.MF echo Manifest-Version: 1.0
>> out\MANIFEST.MF echo Main-Class: com.tiendaautos.Main
>> out\MANIFEST.MF echo Class-Path: lib/mariadb-java-client.jar
jar cfm GestionAutos.jar out\MANIFEST.MF -C out .
echo Listo: GestionAutos.jar
pause
