import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;

/**
 * Gestor de reservas AirTortilla.
 *
 * Entrada : CSV con cabecera nombre,destino,origen,fechaRegistro
 * Salida  : un fichero por destino y fecha: AirTortilla_XX_YYYY_MM_DD.csv
 *           (XX = código de país de 2 letras, por ejemplo ES, FR, GB)
 */
public class AirTortillaManager {

    // ---------- Formatos de fecha ----------
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("M/d/yyyy");
    private static final DateTimeFormatter FORMATO_FECHA_ALTERNATIVO = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter FORMATO_FICHERO = DateTimeFormatter.ofPattern("yyyy_MM_dd");

    private static final String CABECERA_SALIDA = "id,nombre,destino,origen,fechaRegistro,localizador";

    // ---------- Clase Reserva ----------
    static class Reserva {
        private String id;            // identificador único (RES-000001, RES-000002...)
        private String nombre;
        private String destino;       // código de país de 2 letras
        private String origen;        // código de país de 2 letras
        private LocalDate fecha;
        private String localizador;   // vacío si no hay coincidencias

        public Reserva(String id, String nombre, String destino, String origen, LocalDate fecha) {
            this.id = id;
            this.nombre = nombre;
            this.destino = destino;
            this.origen = origen;
            this.fecha = fecha;
            this.localizador = "";
        }

        public String getId() { return id; }
        public String getNombre() { return nombre; }
        public String getDestino() { return destino; }
        public String getOrigen() { return origen; }
        public LocalDate getFecha() { return fecha; }
        public String getLocalizador() { return localizador; }
        public void setLocalizador(String localizador) { this.localizador = localizador; }
    }

    // ---------- Validaciones y utilidades de CSV ----------

    /** Un código de país válido son exactamente 2 letras mayúsculas (ej. FR). */
    static boolean esCodigoPais(String texto) {
        return texto.matches("[A-Z]{2}");
    }

    /** Convierte un texto en fecha. Devuelve null si no tiene un formato válido. */
    static LocalDate leerFecha(String texto) {
        try {
            return LocalDate.parse(texto, FORMATO_FECHA);
        } catch (DateTimeParseException e) {
            // no encaja con M/d/yyyy: probamos el otro formato
        }
        try {
            return LocalDate.parse(texto, FORMATO_FECHA_ALTERNATIVO);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    /** Separa una línea del CSV por comas, respetando los campos entre comillas. */
    static ArrayList<String> separarLinea(String linea) {
        ArrayList<String> campos = new ArrayList<>();
        StringBuilder campo = new StringBuilder();
        boolean entreComillas = false;

        for (int i = 0; i < linea.length(); i++) {
            char c = linea.charAt(i);
            if (c == '"') {
                if (entreComillas && i + 1 < linea.length() && linea.charAt(i + 1) == '"') {
                    campo.append('"');   // dos comillas seguidas = una comilla literal
                    i++;
                } else {
                    entreComillas = !entreComillas;
                }
            } else if (c == ',' && !entreComillas) {
                campos.add(campo.toString().trim());
                campo = new StringBuilder();
            } else {
                campo.append(c);
            }
        }
        campos.add(campo.toString().trim());
        return campos;
    }

    /** Si un campo contiene coma o comillas, lo envuelve en comillas para el CSV de salida. */
    static String prepararCampo(String valor) {
        if (valor.contains(",") || valor.contains("\"")) {
            return "\"" + valor.replace("\"", "\"\"") + "\"";
        }
        return valor;
    }

    // ---------- Carga de reservas ----------

    /**
     * Lee el CSV y devuelve la lista de reservas válidas.
     * Los problemas encontrados se añaden a la lista "errores".
     */
    static ArrayList<Reserva> leerReservas(String rutaArchivo, ArrayList<String> errores) {
        ArrayList<Reserva> reservas = new ArrayList<>();

        try (BufferedReader lector = Files.newBufferedReader(Paths.get(rutaArchivo))) {
            String linea;
            int numeroLinea = 0;   // línea del fichero (para los mensajes de error)
            int numeroFila = 0;    // fila de datos (para el ID)

            while ((linea = lector.readLine()) != null) {
                numeroLinea++;
                if (numeroLinea == 1) continue;     // saltamos la cabecera
                if (linea.trim().isEmpty()) continue; // saltamos líneas vacías
                numeroFila++;

                ArrayList<String> campos = separarLinea(linea);
                if (campos.size() < 4) {
                    errores.add("Línea " + numeroLinea + ": faltan columnas (hay " + campos.size() + ", se esperan 4)");
                    continue;
                }

                String nombre = campos.get(0);
                String destino = campos.get(1).toUpperCase();
                String origen = campos.get(2).toUpperCase();
                LocalDate fecha = leerFecha(campos.get(3));

                if (nombre.isEmpty()) {
                    errores.add("Línea " + numeroLinea + ": el nombre está vacío");
                } else if (!esCodigoPais(destino)) {
                    errores.add("Línea " + numeroLinea + ": destino no válido '" + campos.get(1) + "' (debe ser 2 letras, ej. FR)");
                } else if (!esCodigoPais(origen)) {
                    errores.add("Línea " + numeroLinea + ": origen no válido '" + campos.get(2) + "' (debe ser 2 letras, ej. ES)");
                } else if (fecha == null) {
                    errores.add("Línea " + numeroLinea + ": fecha no válida '" + campos.get(3) + "'");
                } else {
                    String id = String.format("RES-%06d", numeroFila);
                    reservas.add(new Reserva(id, nombre, destino, origen, fecha));
                }
            }
        } catch (IOException e) {
            errores.add("No se pudo leer el fichero: " + e.getMessage());
        }

        asignarLocalizadores(reservas);
        return reservas;
    }

    // ---------- Localizadores ----------

    /** Dos reservas son del mismo viaje si coinciden nombre, origen y destino. */
    static boolean mismoViaje(Reserva a, Reserva b) {
        return a.getNombre().equalsIgnoreCase(b.getNombre())
                && a.getOrigen().equals(b.getOrigen())
                && a.getDestino().equals(b.getDestino());
    }

    /**
     * Si 2 o más reservas coinciden en nombre, origen y destino, todas reciben
     * el mismo localizador (LOC-AIR-001, LOC-AIR-002...). Se numeran en el orden
     * en que aparecen en el CSV, así que el resultado es siempre el mismo.
     */
    static void asignarLocalizadores(ArrayList<Reserva> reservas) {
        int contador = 1;

        for (int i = 0; i < reservas.size(); i++) {
            Reserva actual = reservas.get(i);
            if (!actual.getLocalizador().isEmpty()) continue; // ya tiene localizador

            // Contamos cuántas reservas son del mismo viaje (incluida ella misma)
            int coincidencias = 0;
            for (int j = 0; j < reservas.size(); j++) {
                if (mismoViaje(actual, reservas.get(j))) coincidencias++;
            }

            if (coincidencias >= 2) {
                String localizador = String.format("LOC-AIR-%03d", contador);
                contador++;
                for (int j = 0; j < reservas.size(); j++) {
                    if (mismoViaje(actual, reservas.get(j))) {
                        reservas.get(j).setLocalizador(localizador);
                    }
                }
            }
        }
    }

    // ---------- Modalidad total ----------

    /** Nombre del fichero de salida de una reserva: AirTortilla_XX_YYYY_MM_DD.csv */
    static String nombreFichero(Reserva r) {
        return "AirTortilla_" + r.getDestino() + "_" + r.getFecha().format(FORMATO_FICHERO) + ".csv";
    }

    static void ejecutarModoTotal(String rutaEntrada, String carpetaSalida) {
        System.out.println("\n--- [MODALIDAD TOTAL] Generando ficheros ---");

        ArrayList<String> errores = new ArrayList<>();
        ArrayList<Reserva> reservas = leerReservas(rutaEntrada, errores);

        // 1. Lista de nombres de fichero distintos (uno por destino y fecha)
        ArrayList<String> nombresFicheros = new ArrayList<>();
        for (Reserva r : reservas) {
            String nombre = nombreFichero(r);
            if (!nombresFicheros.contains(nombre)) {
                nombresFicheros.add(nombre);
            }
        }
        Collections.sort(nombresFicheros); // orden alfabético: por destino y luego por fecha

        // 2. Escribimos cada fichero con las reservas que le corresponden
        new File(carpetaSalida).mkdirs(); // crea la carpeta si no existe
        for (String nombreFichero : nombresFicheros) {
            int escritas = 0;
            try (BufferedWriter escritor = Files.newBufferedWriter(Paths.get(carpetaSalida, nombreFichero))) {
                escritor.write(CABECERA_SALIDA + "\n");
                for (Reserva r : reservas) {
                    if (nombreFichero(r).equals(nombreFichero)) {
                        escritor.write(r.getId() + "," + prepararCampo(r.getNombre()) + ","
                                + r.getDestino() + "," + r.getOrigen() + ","
                                + r.getFecha().format(FORMATO_FECHA) + "," + r.getLocalizador() + "\n");
                        escritas++;
                    }
                }
            } catch (IOException e) {
                System.err.println("Error al escribir " + nombreFichero + ": " + e.getMessage());
                continue;
            }
            System.out.println("-> " + nombreFichero + " (" + escritas + " reservas)");
        }

        // 3. Resumen
        System.out.println("Reservas válidas: " + reservas.size()
                + " | Ficheros: " + nombresFicheros.size()
                + " | Filas rechazadas: " + errores.size());
        for (String error : errores) {
            System.out.println("   [AVISO] " + error);
        }
    }

    // ---------- Modalidad línea a línea ----------

    /** Devuelve los nombres de los ficheros AirTortilla_*.csv que hay en la carpeta de salida. */
    static ArrayList<String> listarGenerados(String carpetaSalida) {
        ArrayList<String> nombres = new ArrayList<>();
        File[] archivos = new File(carpetaSalida).listFiles();
        if (archivos == null) return nombres; // la carpeta no existe

        for (File archivo : archivos) {
            String nombre = archivo.getName();
            if (nombre.startsWith("AirTortilla_") && nombre.endsWith(".csv")) {
                nombres.add(nombre);
            }
        }
        Collections.sort(nombres);
        return nombres;
    }

    static void ejecutarModoLineaALinea(String rutaEntrada, String carpetaSalida, Scanner teclado) {
        System.out.println("\n--- [MODALIDAD LÍNEA A LÍNEA] Consulta interactiva ---");
        System.out.println("1. Consultar el CSV de entrada (" + rutaEntrada + ")");
        System.out.println("2. Consultar un fichero generado (" + carpetaSalida + ")");
        System.out.print("Elige una opción: ");
        String eleccion = teclado.nextLine().trim();

        String rutaFichero;
        if (eleccion.equals("1")) {
            rutaFichero = rutaEntrada;
        } else if (eleccion.equals("2")) {
            ArrayList<String> generados = listarGenerados(carpetaSalida);
            if (generados.isEmpty()) {
                System.out.println("No hay ficheros generados. Ejecuta antes la modalidad total.");
                return;
            }
            for (int i = 0; i < generados.size(); i++) {
                System.out.println((i + 1) + ". " + generados.get(i));
            }
            System.out.print("Número de fichero: ");
            int numero;
            try {
                numero = Integer.parseInt(teclado.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Debes escribir un número.");
                return;
            }
            if (numero < 1 || numero > generados.size()) {
                System.out.println("Ese número no está en la lista.");
                return;
            }
            rutaFichero = Paths.get(carpetaSalida, generados.get(numero - 1)).toString();
        } else {
            System.out.println("Opción no válida.");
            return;
        }

        try (BufferedReader lector = Files.newBufferedReader(Paths.get(rutaFichero))) {
            String linea;
            int numeroLinea = 0;
            while ((linea = lector.readLine()) != null) {
                numeroLinea++;
                if (numeroLinea == 1) {
                    System.out.println("CABECERA -> " + linea);
                    continue;
                }
                System.out.println("Línea " + (numeroLinea - 1) + ": " + linea);
                System.out.print("¿Continuar con la siguiente línea? (S/N): ");
                if (teclado.nextLine().trim().equalsIgnoreCase("N")) {
                    System.out.println("Consulta detenida.");
                    return;
                }
            }
            System.out.println("Fin del fichero.");
        } catch (IOException e) {
            System.err.println("No se pudo leer el fichero: " + e.getMessage());
        }
    }

}