package db;

import model.Reserva;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class ReservaDAO {

    /** Guarda una reserva. Si el localizador está vacío, guarda NULL. */
    public void insertar(Reserva r) throws SQLException {
        String sql = "INSERT INTO reservas (codigo, nombre, destino, origen, fecha_registro, localizador) "
                + "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection con = Conexion.obtener();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, r.getId());
            ps.setString(2, r.getNombre());
            ps.setString(3, r.getDestino());
            ps.setString(4, r.getOrigen());
            ps.setDate(5, Date.valueOf(r.getFecha()));
            if (r.getLocalizador().isEmpty()) {
                ps.setNull(6, java.sql.Types.VARCHAR);
            } else {
                ps.setString(6, r.getLocalizador());
            }
            ps.executeUpdate();
        }
    }

    /** Devuelve todas las reservas, ordenadas por código. */
    public ArrayList<Reserva> listarTodas() throws SQLException {
        ArrayList<Reserva> reservas = new ArrayList<>();
        String sql = "SELECT codigo, nombre, destino, origen, fecha_registro, localizador "
                + "FROM reservas ORDER BY codigo";

        try (Connection con = Conexion.obtener();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                Reserva r = new Reserva(
                        rs.getString("codigo"),
                        rs.getString("nombre"),
                        rs.getString("destino"),
                        rs.getString("origen"),
                        rs.getDate("fecha_registro").toLocalDate());
                String loc = rs.getString("localizador");
                if (loc != null) r.setLocalizador(loc);
                reservas.add(r);
            }
        }
        return reservas;
    }

    /** True si ya existe una reserva con ese código (RES-000001). */
    public boolean existeCodigo(String codigo) throws SQLException {
        String sql = "SELECT 1 FROM reservas WHERE codigo = ?";

        try (Connection con = Conexion.obtener();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, codigo);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    /** Cuántas reservas hay guardadas. */
    public int contar() throws SQLException {
        try (Connection con = Conexion.obtener();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM reservas")) {
            rs.next();
            return rs.getInt(1);
        }
    }

    /** Guarda todas las reservas en una transacción: o entran todas o ninguna. */
    public void insertarVarias(ArrayList<Reserva> reservas) throws SQLException {
        String sql = "INSERT INTO reservas (codigo, nombre, destino, origen, fecha_registro, localizador) "
                + "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection con = Conexion.obtener()) {
            con.setAutoCommit(false);   // empieza la transacción
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                for (Reserva r : reservas) {
                    ps.setString(1, r.getId());
                    ps.setString(2, r.getNombre());
                    ps.setString(3, r.getDestino());
                    ps.setString(4, r.getOrigen());
                    ps.setDate(5, Date.valueOf(r.getFecha()));
                    if (r.getLocalizador().isEmpty()) {
                        ps.setNull(6, java.sql.Types.VARCHAR);
                    } else {
                        ps.setString(6, r.getLocalizador());
                    }
                    ps.addBatch();      // la apunta, sin enviarla todavía
                }
                ps.executeBatch();      // las envía todas juntas
                con.commit();           // confirma
            } catch (SQLException e) {
                con.rollback();         // algo falló: deshace todo
                throw e;
            }
        }
    }
    /** Borra todas las reservas. Devuelve cuántas se han borrado. */
    public int borrarTodas() throws SQLException {
        try (Connection con = Conexion.obtener();
             Statement st = con.createStatement()) {
            return st.executeUpdate("DELETE FROM reservas");
        }
    }
}