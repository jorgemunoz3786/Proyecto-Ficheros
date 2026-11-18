package db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import model.RegistroAuditoria;
import java.sql.ResultSet;
import java.util.ArrayList;

public class AuditoriaDAO {

    /**
     * Añade una línea al registro de auditoría.
     * registroId puede ser null si la acción no afecta a una fila concreta.
     */
    public void registrar(int usuarioId, String accion, String tablaAfectada,
                          Integer registroId, String detalle) throws SQLException {
        String sql = "INSERT INTO auditoria (usuario_id, accion, tabla_afectada, registro_id, detalle) "
                + "VALUES (?, ?, ?, ?, ?)";

        try (Connection con = Conexion.obtener();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, usuarioId);
            ps.setString(2, accion);
            ps.setString(3, tablaAfectada);
            if (registroId == null) {
                ps.setNull(4, Types.INTEGER);
            } else {
                ps.setInt(4, registroId);
            }
            ps.setString(5, detalle);   // null es válido: la columna admite NULL
            ps.executeUpdate();
        }
    }
    /** Devuelve los últimos "limite" apuntes, del más reciente al más antiguo. */
    public ArrayList<RegistroAuditoria> ultimos(int limite) throws SQLException {
        ArrayList<RegistroAuditoria> lista = new ArrayList<>();
        String sql = "SELECT a.fecha_hora, u.username, a.accion, a.tabla_afectada, "
                + "a.registro_id, a.detalle "
                + "FROM auditoria a JOIN usuarios u ON a.usuario_id = u.id "
                + "ORDER BY a.id DESC LIMIT ?";

        try (Connection con = Conexion.obtener();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, limite);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int regId = rs.getInt("registro_id");
                    Integer registroId = rs.wasNull() ? null : regId;
                    lista.add(new RegistroAuditoria(
                            rs.getTimestamp("fecha_hora").toLocalDateTime(),
                            rs.getString("username"),
                            rs.getString("accion"),
                            rs.getString("tabla_afectada"),
                            registroId,
                            rs.getString("detalle")));
                }
            }
        }
        return lista;
    }
}