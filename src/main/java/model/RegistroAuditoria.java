package model;

import java.time.LocalDateTime;

public class RegistroAuditoria {
    private final LocalDateTime fechaHora;
    private final String username;
    private final String accion;
    private final String tablaAfectada;
    private final Integer registroId;   // puede ser null
    private final String detalle;       // puede ser null

    public RegistroAuditoria(LocalDateTime fechaHora, String username, String accion,
                             String tablaAfectada, Integer registroId, String detalle) {
        this.fechaHora = fechaHora;
        this.username = username;
        this.accion = accion;
        this.tablaAfectada = tablaAfectada;
        this.registroId = registroId;
        this.detalle = detalle;
    }

    public LocalDateTime getFechaHora() { return fechaHora; }
    public String getUsername() { return username; }
    public String getAccion() { return accion; }
    public String getTablaAfectada() { return tablaAfectada; }
    public Integer getRegistroId() { return registroId; }
    public String getDetalle() { return detalle; }
}