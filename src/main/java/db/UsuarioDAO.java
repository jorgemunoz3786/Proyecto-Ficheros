package db;

import model.Rol;
import model.Usuario;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;

public class UsuarioDAO {

    /** Guarda un usuario nuevo y devuelve el id que le asigna MySQL. */
    public int insertar(String username, String passwordHash, Rol rol) throws SQLException {
        String sql = "INSERT INTO usuarios (username, password_hash, rol) VALUES (?, ?, ?)";

        try (Connection con = Conexion.obtener();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, username);
            ps.setString(2, passwordHash);
            ps.setString(3, rol.name());   // ADMIN -> "ADMIN"
            ps.executeUpdate();

            try (ResultSet claves = ps.getGeneratedKeys()) {
                claves.next();
                return claves.getInt(1);
            }
        }
    }

    /** Busca un usuario por su username. Devuelve vacío si no existe. */
    public Optional<Usuario> buscarPorUsername(String username) throws SQLException {
        String sql = "SELECT id, username, password_hash, rol, activo, fecha_alta "
                + "FROM usuarios WHERE username = ?";

        try (Connection con = Conexion.obtener();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();   // no existe
                }
                Usuario u = new Usuario(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("password_hash"),
                        Rol.valueOf(rs.getString("rol")),   // "ADMIN" -> Rol.ADMIN
                        rs.getBoolean("activo"),
                        rs.getTimestamp("fecha_alta").toLocalDateTime());
                return Optional.of(u);
            }
        }
    }

    /** Cuántos usuarios hay guardados (sirve para saber si hay que crear el primer ADMIN). */
    public int contar() throws SQLException {
        try (Connection con = Conexion.obtener();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM usuarios")) {
            rs.next();
            return rs.getInt(1);
        }
    }
}