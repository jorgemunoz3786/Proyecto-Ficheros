USE airtortilla;

CREATE TABLE usuarios (
                          id            INT AUTO_INCREMENT PRIMARY KEY,
                          username      VARCHAR(50)  NOT NULL UNIQUE,
                          password_hash VARCHAR(100) NOT NULL,
                          rol           ENUM('EMPLEADO','ADMIN') NOT NULL DEFAULT 'EMPLEADO',
                          activo        BOOLEAN      NOT NULL DEFAULT TRUE,
                          fecha_alta    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE reservas (
                          id             INT AUTO_INCREMENT PRIMARY KEY,
                          codigo         VARCHAR(20)  NOT NULL UNIQUE,   -- RES-000001
                          nombre         VARCHAR(100) NOT NULL,
                          destino        CHAR(2)      NOT NULL,
                          origen         CHAR(2)      NOT NULL,
                          fecha_registro DATE         NOT NULL,
                          localizador    VARCHAR(20)  NULL,              -- LOC-AIR-001 o NULL
                          INDEX idx_destino_fecha (destino, fecha_registro)
);

CREATE TABLE auditoria (
                           id             INT AUTO_INCREMENT PRIMARY KEY,
                           usuario_id     INT          NOT NULL,
                           accion         VARCHAR(30)  NOT NULL,
                           tabla_afectada VARCHAR(30)  NOT NULL,
                           registro_id    INT          NULL,
                           detalle        TEXT         NULL,
                           fecha_hora     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
                           FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
);