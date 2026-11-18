package model;

import java.time.LocalDateTime;

public class Usuario {
    private int id;
    private String username;
    private String passwordHash;
    private Rol rol;
    private boolean activo;
    private LocalDateTime fechaAlta;

    public Usuario(int id, String username, String passwordHash,
                   Rol rol, boolean activo, LocalDateTime fechaAlta) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.rol = rol;
        this.activo = activo;
        this.fechaAlta = fechaAlta;
    }

    public int getId() { return id; }
    public String getUsername() { return username; }
    public String getPasswordHash() { return passwordHash; }
    public Rol getRol() { return rol; }
    public boolean isActivo() { return activo; }
    public LocalDateTime getFechaAlta() { return fechaAlta; }
}