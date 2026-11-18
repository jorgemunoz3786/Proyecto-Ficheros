package service;

import db.ReservaDAO;
import model.Reserva;
import model.Usuario;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;

public class ImportadorCSV {

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("M/d/yyyy");
    private static final DateTimeFormatter FORMATO_FECHA_ALTERNATIVO = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /** Importa el CSV a MySQL. Solo si la tabla reservas está vacía. */
    public static void importar(String rutaCsv, Usuario quien)  {
        ReservaDAO dao = new ReservaDAO();
        try {
            int existentes = dao.contar();
            if (existentes > 0) {
                System.out.println("La base de datos ya tiene " + existentes
                        + " reservas. No se importa para evitar duplicados.");
                return;
            }

            ArrayList<String> errores = new ArrayList<>();
            ArrayList<Reserva> reservas = leerReservas(rutaCsv, errores);

            dao.insertarVarias(reservas);

            if (!reservas.isEmpty()) {
                AuditoriaService.registrar(quien, AuditoriaService.IMPORTAR_CSV,
                        AuditoriaService.TABLA_RESERVAS, null,
                        reservas.size() + " reservas importadas desde " + rutaCsv
                                + " (" + errores.size() + " filas rechazadas)");
            }

            System.out.println("Importación completada. Reservas guardadas: " + reservas.size()
                    + " | Filas rechazadas: " + errores.size());
            for (String error : errores) {
                System.out.println("   [AVISO] " + error);
            }
        } catch (SQLException e) {
            System.err.println("Error de base de datos, no se guardó nada: " + e.getMessage());
        }
    }

    // ---------- Código movido desde AirTortillaManager ----------

    static boolean esCodigoPais(String texto) {
        return texto.matches("[A-Z]{2}");
    }

    static LocalDate leerFecha(String texto) {
        try {
            return LocalDate.parse(texto, FORMATO_FECHA);
        } catch (DateTimeParseException e) {
            // probamos el otro formato
        }
        try {
            return LocalDate.parse(texto, FORMATO_FECHA_ALTERNATIVO);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    static ArrayList<String> separarLinea(String linea) {
        ArrayList<String> campos = new ArrayList<>();
        StringBuilder campo = new StringBuilder();
        boolean entreComillas = false;

        for (int i = 0; i < linea.length(); i++) {
            char c = linea.charAt(i);
            if (c == '"') {
                if (entreComillas && i + 1 < linea.length() && linea.charAt(i + 1) == '"') {
                    campo.append('"');
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

    static ArrayList<Reserva> leerReservas(String rutaArchivo, ArrayList<String> errores) {
        ArrayList<Reserva> reservas = new ArrayList<>();

        try (BufferedReader lector = Files.newBufferedReader(Paths.get(rutaArchivo))) {
            String linea;
            int numeroLinea = 0;
            int numeroFila = 0;

            while ((linea = lector.readLine()) != null) {
                numeroLinea++;
                if (numeroLinea == 1) continue;
                if (linea.trim().isEmpty()) continue;
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

        ReservaService.asignarLocalizadores(reservas);
        return reservas;
    }


}