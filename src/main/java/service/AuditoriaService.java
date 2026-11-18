package service;

import db.AuditoriaDAO;
import model.Usuario;

import java.sql.SQLException;

public class AuditoriaService {

    // Acciones (constantes para evitar erratas como "CREAR_USARIO")
    public static final String CREAR_USUARIO = "CREAR_USUARIO";
    public static final String IMPORTAR_CSV = "IMPORTAR_CSV";

    // Tablas afectadas
    public static final String TABLA_USUARIOS = "usuarios";
    public static final String TABLA_RESERVAS = "reservas";

    private static final AuditoriaDAO dao = new AuditoriaDAO();

    public static final String BORRAR_RESERVAS = "BORRAR_RESERVAS";

    /**
     * Apunta en el libro quién hizo qué. Si el apunte falla, avisa pero no lanza
     * excepción: el cambio real ya se hizo y no queremos que el programa se caiga.
     */
    public static void registrar(Usuario quien, String accion, String tabla,
                                 Integer registroId, String detalle) {
        try {
            dao.registrar(quien.getId(), accion, tabla, registroId, detalle);
        } catch (SQLException e) {
            System.err.println("[AVISO] No se pudo registrar en auditoría: " + e.getMessage());
        }
    }
}