package service;

import db.UsuarioDAO;
import model.Rol;
import model.Usuario;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.SQLException;
import java.util.Optional;

import db.ReservaDAO;

public class AuthService {

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private final ReservaDAO reservaDAO = new ReservaDAO();

    /** Devuelve el usuario si las credenciales son correctas y está activo; si no, vacío. */
    public Optional<Usuario> login(String username, String password) throws SQLException {
        Optional<Usuario> encontrado = usuarioDAO.buscarPorUsername(username);
        if (encontrado.isEmpty()) {
            return Optional.empty();
        }
        Usuario u = encontrado.get();
        if (!u.isActivo()) {
            return Optional.empty();
        }
        if (!BCrypt.checkpw(password, u.getPasswordHash())) {
            return Optional.empty();
        }
        return Optional.of(u);
    }

    /** Hashea la contraseña con BCrypt y guarda el usuario. Devuelve su id. */
    public int crearUsuario(String username, String password, Rol rol) throws SQLException {
        String hash = BCrypt.hashpw(password, BCrypt.gensalt());
        return usuarioDAO.insertar(username, hash, rol);
    }

    /** Si no hay ningún usuario, crea el ADMIN inicial con la contraseña de ADMIN_PASSWORD. */
    public void crearAdminInicial() throws SQLException {
        if (usuarioDAO.contar() > 0) {
            return;   // ya hay usuarios, no hacemos nada
        }
        String clave = System.getenv("ADMIN_PASSWORD");
        if (clave == null || clave.isEmpty()) {
            throw new SQLException("No hay usuarios y falta la variable de entorno ADMIN_PASSWORD");
        }
        crearUsuario("admin", clave, Rol.ADMIN);
        System.out.println("Usuario inicial 'admin' creado.");
    }

    /** Alta de usuario hecha por alguien concreto. Solo un ADMIN activo puede hacerla. */
    public int crearUsuarioComo(Usuario quienPide, String username, String password, Rol rol)
            throws SQLException {
        if (quienPide.getRol() != Rol.ADMIN) {
            throw new SecurityException("Solo un ADMIN puede dar de alta usuarios.");
        }
        return crearUsuario(username, password, rol);
    }
    /** Borra todas las reservas. Solo un ADMIN puede hacerlo. Devuelve cuántas se borraron. */
    public int borrarReservasComo(Usuario quienPide) throws SQLException {
        if (quienPide.getRol() != Rol.ADMIN) {
            throw new SecurityException("Solo un ADMIN puede borrar reservas.");
        }
        int borradas = reservaDAO.borrarTodas();
        if (borradas > 0) {
            AuditoriaService.registrar(quienPide, AuditoriaService.BORRAR_RESERVAS,
                    AuditoriaService.TABLA_RESERVAS, null,
                    borradas + " reservas borradas");
        }
        return borradas;
    }
}