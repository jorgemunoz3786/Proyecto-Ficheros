import model.Usuario;
import service.AuthService;
import service.ImportadorCSV;

import java.sql.SQLException;
import java.util.Optional;
import java.util.Scanner;

public class Main {

    private static final int MAX_INTENTOS = 3;

    public static void main(String[] args) {
        String rutaEntrada = args.length > 0 ? args[0] : "data/viajes.csv";
        String carpetaSalida = args.length > 1 ? args[1] : "output_airtortilla";

        Scanner teclado = new Scanner(System.in);
        AuthService auth = new AuthService();

        // 1. Si no hay usuarios, crea el ADMIN inicial
        try {
            auth.crearAdminInicial();
        } catch (SQLException e) {
            System.err.println("No se pudo preparar el sistema de usuarios: " + e.getMessage());
            return;
        }

        // 2. Login (máximo 3 intentos)
        Usuario usuarioActual = pedirLogin(teclado, auth);
        if (usuarioActual == null) {
            System.out.println("Acceso denegado. Saliendo del sistema...");
            return;
        }
        System.out.println("Bienvenido, " + usuarioActual.getUsername()
                + " (" + usuarioActual.getRol() + ")");

        // 3. Menú de siempre
        boolean salir = false;
        while (!salir) {
            System.out.println("\n=== GESTOR DE RESERVAS AIRTORTILLA ===");
            System.out.println("1. Importar CSV a la base de datos");
            System.out.println("2. Exportar reservas a ficheros (por destino y fecha)");
            System.out.println("3. Salir");
            System.out.print("Elige una opción: ");

            if (!teclado.hasNextLine()) break;
            String opcion = teclado.nextLine().trim();

            if (opcion.equals("1")) {
                ImportadorCSV.importar(rutaEntrada);
            } else if (opcion.equals("2")) {
                AirTortillaManager.ejecutarModoTotal(carpetaSalida);
            } else if (opcion.equals("3")) {
                System.out.println("Saliendo del sistema...");
                salir = true;
            } else {
                System.out.println("Opción no válida.");
            }
        }
        teclado.close();
    }

    /** Pide usuario y contraseña hasta MAX_INTENTOS veces. Devuelve null si no se consigue entrar. */
    private static Usuario pedirLogin(Scanner teclado, AuthService auth) {
        System.out.println("=== INICIO DE SESIÓN ===");
        for (int intento = 1; intento <= MAX_INTENTOS; intento++) {
            System.out.print("Usuario: ");
            if (!teclado.hasNextLine()) return null;
            String username = teclado.nextLine().trim();

            System.out.print("Contraseña: ");
            if (!teclado.hasNextLine()) return null;
            String password = teclado.nextLine();

            try {
                Optional<Usuario> resultado = auth.login(username, password);
                if (resultado.isPresent()) {
                    return resultado.get();
                }
            } catch (SQLException e) {
                System.err.println("Error al consultar la base de datos: " + e.getMessage());
                return null;
            }
            System.out.println("Usuario o contraseña incorrectos (intento "
                    + intento + " de " + MAX_INTENTOS + ").");
        }
        return null;
    }
}