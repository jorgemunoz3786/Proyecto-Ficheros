# AirTortilla – Gestor de reservas

## ¿Qué hace?

AirTortilla recibe una lista de reservas de vuelo y la reparte en ficheros más pequeños, uno por **país de destino** y **día de la reserva**. Así cada equipo puede trabajar solo con las reservas que le corresponden.

Por ejemplo, todas las reservas hacia Francia hechas el 15 de marzo de 2025 acaban en un único fichero llamado `AirTortilla_FR_2025_03_15.csv` (`FR` es el código de Francia).

## ¿Cómo funciona?

1. **Lee las reservas** de un fichero con los datos de cada pasajero: nombre, destino, origen y fecha.
2. **Revisa que los datos estén bien.** Si una reserva tiene un error (por ejemplo, una fecha imposible o un país mal escrito), la aparta y avisa de en qué línea está, pero sigue con el resto.
3. **Da un número único a cada reserva** (`RES-000001`, `RES-000002`…), para poder identificarla sin confusión.
4. **Detecta reservas repetidas.** Si dos o más pasajeros tienen el mismo nombre, origen y destino, todas comparten un código llamado *localizador* (`LOC-AIR-001`…). Las demás reservas no llevan localizador.
5. **Crea los ficheros de salida**, uno por destino y fecha, en la carpeta `output_airtortilla`.

## ¿Cómo se usa?

Al abrir el programa aparece un menú con tres opciones:

- **1. Modalidad Total:** hace todo el proceso de una vez y genera todos los ficheros. Al terminar muestra un resumen: cuántas reservas eran válidas, cuántos ficheros se crearon y qué filas tenían errores.
- **2. Modalidad Línea a Línea:** sirve para consultar los datos con calma. Eliges si quieres ver el fichero original o uno de los generados, y el programa va mostrando una línea cada vez. Tú decides si continúas (S) o paras (N).
- **3. Salir.**

## Ficheros del proyecto

- `Main.java`: el menú que ve el usuario.
- `AirTortillaManager.java`: hace el trabajo (leer, revisar, repartir y guardar).
- `LimpiarSalida.java`: herramienta para desarrolladores que vacía la carpeta de ficheros generados.
- `data/viajes.csv`: reservas de ejemplo para probar.
