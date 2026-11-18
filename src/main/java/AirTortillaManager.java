import db.ReservaDAO;
import model.Reserva;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

/**
 * Exporta las reservas guardadas en MySQL a un fichero por destino y fecha:
 * AirTortilla_XX_YYYY_MM_DD.csv (XX = código de país de 2 letras).
 */
public class AirTortillaManager {

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("M/d/yyyy");
    private static final DateTimeFormatter FORMATO_FICHERO = DateTimeFormatter.ofPattern("yyyy_MM_dd");
    private static final String CABECERA_SALIDA = "id,nombre,destino,origen,fechaRegistro,localizador";

    /** Si un campo contiene coma o comillas, lo envuelve en comillas para el CSV de salida. */
    static String prepararCampo(String valor) {
        if (valor.contains(",") || valor.contains("\"")) {
            return "\"" + valor.replace("\"", "\"\"") + "\"";
        }
        return valor;
    }

    /** Nombre del fichero de salida de una reserva: AirTortilla_XX_YYYY_MM_DD.csv */
    static String nombreFichero(Reserva r) {
        return "AirTortilla_" + r.getDestino() + "_" + r.getFecha().format(FORMATO_FICHERO) + ".csv";
    }

    public static void ejecutarModoTotal(String carpetaSalida) {
        System.out.println("\n--- [EXPORTAR] Generando ficheros desde MySQL ---");

        ArrayList<Reserva> reservas;
        try {
            reservas = new ReservaDAO().listarTodas();
        } catch (SQLException e) {
            System.err.println("No se pudieron leer las reservas: " + e.getMessage());
            return;
        }
        if (reservas.isEmpty()) {
            System.out.println("No hay reservas en la base de datos. Importa primero el CSV.");
            return;
        }

        // 1. Agrupamos: nombre de fichero -> sus reservas (el TreeMap lo mantiene ordenado)
        TreeMap<String, ArrayList<Reserva>> porFichero = new TreeMap<>();
        for (Reserva r : reservas) {
            porFichero.computeIfAbsent(nombreFichero(r), k -> new ArrayList<>()).add(r);
        }

        // 2. Escribimos cada fichero
        new File(carpetaSalida).mkdirs();
        int creados = 0;
        for (Map.Entry<String, ArrayList<Reserva>> entrada : porFichero.entrySet()) {
            String nombre = entrada.getKey();
            try (BufferedWriter escritor = Files.newBufferedWriter(Paths.get(carpetaSalida, nombre))) {
                escritor.write(CABECERA_SALIDA + "\n");
                for (Reserva r : entrada.getValue()) {
                    escritor.write(r.getId() + "," + prepararCampo(r.getNombre()) + ","
                            + r.getDestino() + "," + r.getOrigen() + ","
                            + r.getFecha().format(FORMATO_FECHA) + "," + r.getLocalizador() + "\n");
                }
            } catch (IOException e) {
                System.err.println("Error al escribir " + nombre + ": " + e.getMessage());
                continue;
            }
            creados++;
            System.out.println("-> " + nombre + " (" + entrada.getValue().size() + " reservas)");
        }

        // 3. Resumen
        System.out.println("Reservas exportadas: " + reservas.size() + " | Ficheros: " + creados);
    }
}