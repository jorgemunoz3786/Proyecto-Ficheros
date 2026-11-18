import model.Usuario;
import service.AuthService;
import service.ImportadorCSV;

import java.sql.SQLException;
import java.util.Optional;
import java.util.Scanner;
import model.Rol;
import java.sql.SQLIntegrityConstraintViolationException;

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
        boolean esAdmin = usuarioActual.getRol() == Rol.ADMIN;
        boolean salir = false;
        while (!salir) {
            System.out.println("\n=== GESTOR DE RESERVAS AIRTORTILLA ===");
            System.out.println("1. Importar CSV a la base de datos");
            System.out.println("2. Exportar reservas a ficheros (por destino y fecha)");
            if (esAdmin) {
                System.out.println("3. Crear usuario");
                System.out.println("4. Borrar todas las reservas");
            }
            System.out.println("0. Salir");
            System.out.print("Elige una opción: ");

            if (!teclado.hasNextLine()) break;
            String opcion = teclado.nextLine().trim();

            if (opcion.equals("1")) {
                ImportadorCSV.importar(rutaEntrada, usuarioActual);
            } else if (opcion.equals("2")) {
                AirTortillaManager.ejecutarModoTotal(carpetaSalida);
            } else if (opcion.equals("3") && esAdmin) {
                altaUsuario(teclado, auth, usuarioActual);
            } else if (opcion.equals("4") && esAdmin) {
                borrarReservas(teclado, auth, usuarioActual);}
            else if (opcion.equals("0")) {
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
    /** Pide los datos de un usuario nuevo y lo da de alta (solo ADMIN). */
    private static void altaUsuario(Scanner teclado, AuthService auth, Usuario quienPide) {
        System.out.println("\n--- CREAR USUARIO ---");
        System.out.print("Nombre de usuario: ");
        String username = teclado.nextLine().trim();
        if (username.isEmpty() || username.length() > 50) {
            System.out.println("El nombre debe tener entre 1 y 50 caracteres.");
            return;
        }

        System.out.print("Contraseña (mínimo 8 caracteres): ");
        String password = teclado.nextLine();
        if (password.length() < 8) {
            System.out.println("La contraseña es demasiado corta.");
            return;
        }

        System.out.print("Rol (EMPLEADO / ADMIN): ");
        Rol rol;
        try {
            rol = Rol.valueOf(teclado.nextLine().trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            System.out.println("Rol no válido. Usa EMPLEADO o ADMIN.");
            return;
        }

        try {
            int id = auth.crearUsuarioComo(quienPide, username, password, rol);
            System.out.println("Usuario '" + username + "' creado con id " + id + " y rol " + rol + ".");
        } catch (SQLIntegrityConstraintViolationException e) {
            System.out.println("Ya existe un usuario llamado '" + username + "'.");
        } catch (SQLException e) {
            System.err.println("No se pudo crear el usuario: " + e.getMessage());
        } catch (SecurityException e) {
            System.out.println(e.getMessage());
        }
    }

    /** Pide confirmación y borra todas las reservas (solo ADMIN). */
    private static void borrarReservas(Scanner teclado, AuthService auth, Usuario quienPide) {
        System.out.println("\n--- BORRAR TODAS LAS RESERVAS ---");
        System.out.println("Esta acción elimina todas las reservas de la base de datos y no se puede deshacer.");
        System.out.print("Escribe BORRAR para confirmar: ");
        String confirmacion = teclado.nextLine().trim();

        if (!confirmacion.equals("BORRAR")) {
            System.out.println("Cancelado. No se ha borrado nada.");
            return;
        }

        try {
            int borradas = auth.borrarReservasComo(quienPide);
            System.out.println("Reservas borradas: " + borradas);
        } catch (SQLException e) {
            System.err.println("No se pudo borrar: " + e.getMessage());
        } catch (SecurityException e) {
            System.out.println(e.getMessage());
        }
    }
}