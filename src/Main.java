import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        // Rutas por defecto (se pueden cambiar pasando argumentos al ejecutar)
        String rutaEntrada = args.length > 0 ? args[0] : "data/viajes.csv";
        String carpetaSalida = args.length > 1 ? args[1] : "output_airtortilla";

        Scanner teclado = new Scanner(System.in); // un único Scanner para todo el programa
        boolean salir = false;

        while (!salir) {
            System.out.println("\n=== GESTOR DE RESERVAS AIRTORTILLA ===");
            System.out.println("1. Modalidad Total (generar ficheros por destino y fecha)");
            System.out.println("2. Modalidad Línea a Línea (consultar ficheros)");
            System.out.println("3. Salir");
            System.out.print("Elige una opción: ");

            if (!teclado.hasNextLine()) break;
            String opcion = teclado.nextLine().trim();

            if (opcion.equals("1")) {
                AirTortillaManager.ejecutarModoTotal(rutaEntrada, carpetaSalida);
            } else if (opcion.equals("2")) {
                AirTortillaManager.ejecutarModoLineaALinea(rutaEntrada, carpetaSalida, teclado);
            } else if (opcion.equals("3")) {
                System.out.println("Saliendo del sistema...");
                salir = true;
            } else {
                System.out.println("Opción no válida.");
            }
        }
        teclado.close();
    }
}