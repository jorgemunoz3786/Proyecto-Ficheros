package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Conexion {

    // Puerto 3307: el que mapeamos en docker-compose.yml
    private static final String URL =
            "jdbc:mysql://localhost:3307/airtortilla?useUnicode=true&characterEncoding=UTF-8&serverTimezone=UTC";

    private static final String USUARIO = System.getenv().getOrDefault("MYSQL_USER", "airtortilla");
    private static final String CLAVE = System.getenv("MYSQL_PASSWORD");

    public static Connection obtener() throws SQLException {
        if (CLAVE == null) {
            throw new SQLException("Falta la variable de entorno MYSQL_PASSWORD");
        }
        return DriverManager.getConnection(URL, USUARIO, CLAVE);
    }
}