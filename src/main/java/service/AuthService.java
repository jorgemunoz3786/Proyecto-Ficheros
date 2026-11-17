package service;

import db.UsuarioDAO;
import model.Rol;
import model.Usuario;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.SQLException;
import java.util.Optional;

public class AuthService {

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

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
}