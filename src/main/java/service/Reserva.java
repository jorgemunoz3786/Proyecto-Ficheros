package model;

import java.time.LocalDate;

public class Reserva {
    private String id;            // RES-000001 (la columna "codigo" de la BD)
    private String nombre;
    private String destino;       // código de país de 2 letras
    private String origen;        // código de país de 2 letras
    private LocalDate fecha;
    private String localizador;   // vacío si no hay coincidencias

    public Reserva(String id, String nombre, String destino, String origen, LocalDate fecha) {
        this.id = id;
        this.nombre = nombre;
        this.destino = destino;
        this.origen = origen;
        this.fecha = fecha;
        this.localizador = "";
    }

    public String getId() { return id; }
    public String getNombre() { return nombre; }
    public String getDestino() { return destino; }
    public String getOrigen() { return origen; }
    public LocalDate getFecha() { return fecha; }
    public String getLocalizador() { return localizador; }
    public void setLocalizador(String localizador) { this.localizador = localizador; }
}