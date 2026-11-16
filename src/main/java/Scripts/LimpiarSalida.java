package Scripts;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Utilidad para desarrolladores: al ejecutar esta clase (Run en el IDE) se elimina
 * todo el contenido de la carpeta de salida. La carpeta en sí se conserva.
 *
 * Opcionalmente acepta la carpeta como argumento: java LimpiarSalida [carpeta_salida]
 */
public class LimpiarSalida {

    private static final String CARPETA_POR_DEFECTO = "output_airtortilla";

    public static void main(String[] args) {
        Path dir = Paths.get(args.length > 0 ? args[0] : CARPETA_POR_DEFECTO)
                .toAbsolutePath().normalize();

        if (!Files.isDirectory(dir)) {
            System.out.println("No existe la carpeta, nada que eliminar: " + dir);
            return;
        }
        if (esRutaProtegida(dir)) {
            System.err.println("Ruta protegida, no se elimina nada: " + dir);
            return;
        }

        try (Stream<Path> s = Files.walk(dir)) {
            List<Path> rutas = s.filter(p -> !p.equals(dir))
                    .sorted(Comparator.reverseOrder())
                    .collect(Collectors.toList());
            for (Path p : rutas) Files.delete(p);
            System.out.println("Carpeta vaciada: " + dir + " (" + rutas.size() + " elementos eliminados)");
        } catch (IOException e) {
            System.err.println("No se pudo vaciar la carpeta: " + e.getMessage());
        }
    }

    /** Evita borrar la raíz, el directorio personal, el directorio actual o un ancestro suyo. */
    private static boolean esRutaProtegida(Path dir) {
        Path home = Paths.get(System.getProperty("user.home")).toAbsolutePath().normalize();
        Path actual = Paths.get("").toAbsolutePath().normalize();
        return dir.getParent() == null || dir.equals(home) || actual.startsWith(dir);
    }
}
