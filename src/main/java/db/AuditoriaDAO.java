package db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

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
}