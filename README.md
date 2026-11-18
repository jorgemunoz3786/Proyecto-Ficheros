# AirTortilla – Gestor de reservas

Aplicación de terminal para gestionar reservas de vuelo. Las reservas se guardan en una **base de datos MySQL** (que corre en Docker), el acceso está protegido con **usuario y contraseña**, y cada modificación importante queda registrada en una **auditoría**.

## Qué puede hacer

- **Iniciar sesión** con usuario y contraseña (máximo 3 intentos).
- **Importar** un CSV de reservas a la base de datos, validando cada fila.
- **Exportar** las reservas a ficheros CSV, uno por país de destino y fecha.
- **Crear usuarios** (solo administradores).
- **Borrar todas las reservas** (solo administradores, con confirmación).
- **Consultar la auditoría**: quién hizo qué y cuándo (solo administradores).

## Requisitos

- Java 21
- Maven
- Docker y Docker Compose
- IntelliJ IDEA (recomendado para ejecutar con variables de entorno)

## Puesta en marcha

### 1. Levantar la base de datos

Desde la raíz del proyecto:

```bash
docker compose up -d
```

Esto arranca MySQL en el puerto **3307** de tu máquina y crea las tablas (`usuarios`, `reservas`, `auditoria`) la primera vez.

> Los scripts de la carpeta `sql/` solo se ejecutan cuando el volumen de datos se crea por primera vez. Si cambias el esquema y quieres que se aplique, hay que borrar el volumen (`docker compose down -v`; **esto elimina todos los datos**).

### 2. Configurar las variables de entorno

La aplicación lee su configuración del entorno. **No se guardan en el código ni se suben a Git.**

| Variable | Obligatoria | Para qué sirve |
|---|---|---|
| `MYSQL_USER` | No (por defecto `airtortilla`) | Usuario de MySQL |
| `MYSQL_PASSWORD` | **Sí** | Contraseña de MySQL (la misma que usa el contenedor) |
| `ADMIN_PASSWORD` | **Sí, la primera vez** | Contraseña del usuario `admin` inicial |

En IntelliJ: **Run → Edit Configurations → Main → Environment variables**, y escribe las variables separadas por `;`:

```
MYSQL_PASSWORD=tu_clave_mysql;ADMIN_PASSWORD=tu_clave_admin
```

### 3. Ejecutar

Ejecuta la clase `Main` desde IntelliJ. Opcionalmente acepta dos argumentos:

| Argumento | Por defecto | Significado |
|---|---|---|
| 1.º | `data/viajes.csv` | Ruta del CSV a importar |
| 2.º | `output_airtortilla` | Carpeta donde se exportan los ficheros |

### 4. Primer arranque: el usuario `admin`

Si la tabla de usuarios está vacía, el programa crea automáticamente el usuario **`admin`** con la contraseña de `ADMIN_PASSWORD`. Verás el mensaje `Usuario inicial 'admin' creado.` Desde el segundo arranque ya no se crea de nuevo.

## Uso

Al arrancar se pide usuario y contraseña. Con 3 fallos seguidos el programa se cierra.

Una vez dentro aparece el menú. Las opciones 3, 4 y 5 **solo se muestran a los administradores**.

| Opción | Qué hace | Quién |
|---|---|---|
| 1 | Importar el CSV a la base de datos | Todos |
| 2 | Exportar las reservas a ficheros por destino y fecha | Todos |
| 3 | Crear usuario | Solo ADMIN |
| 4 | Borrar todas las reservas | Solo ADMIN |
| 5 | Ver auditoría | Solo ADMIN |
| 0 | Salir | Todos |

### Importar reservas (opción 1)

El CSV de entrada debe tener esta cabecera y columnas:

```
nombre,destino,origen,fechaRegistro
```

- `destino` y `origen`: código de país de 2 letras (por ejemplo `FR`, `ES`).
- `fechaRegistro`: formato `M/d/yyyy` (por ejemplo `3/15/2025`) o `yyyy-MM-dd`.

Cada fila se valida. Las filas con errores (nombre vacío, país mal escrito, fecha imposible, columnas que faltan) **se rechazan y se avisa de en qué línea están**, pero el resto se importa.

Cada reserva recibe un código único (`RES-000001`, `RES-000002`…). Si varias reservas comparten nombre, origen y destino, todas reciben el mismo **localizador** (`LOC-AIR-001`…); las demás no llevan localizador.

La importación **solo funciona si la tabla de reservas está vacía**, para evitar duplicados. Si ya hay reservas, usa antes la opción 4.

### Exportar reservas (opción 2)

Genera en la carpeta de salida un fichero por destino y fecha:

```
AirTortilla_FR_2025_03_15.csv
```

Cada fichero tiene la cabecera `id,nombre,destino,origen,fechaRegistro,localizador`. Si una reserva no tiene localizador, esa columna queda vacía. Al terminar se muestra cuántas reservas y ficheros se han generado.

### Crear usuario (opción 3, solo ADMIN)

Pide nombre de usuario (1 a 50 caracteres, no repetido), contraseña (mínimo 8 caracteres) y rol:

- **EMPLEADO**: puede importar y exportar.
- **ADMIN**: además puede crear usuarios, borrar reservas y ver la auditoría.

Las contraseñas **nunca se guardan en claro**: se almacena solo su hash BCrypt.

### Borrar todas las reservas (opción 4, solo ADMIN)

Elimina todas las reservas de la base de datos. Es irreversible, por eso pide escribir exactamente `BORRAR` para confirmar. Cualquier otra cosa cancela la operación.

### Ver auditoría (opción 5, solo ADMIN)

Muestra los últimos 20 apuntes, del más reciente al más antiguo, con este formato:

```
fecha y hora | usuario | acción | tabla | #id | detalle
```

Se registran estas acciones: `CREAR_USUARIO`, `IMPORTAR_CSV` y `BORRAR_RESERVAS`. El detalle nunca incluye contraseñas.

## Estructura del proyecto

```
src/main/java/
├── Main.java                 Menú y login (lo que ve el usuario)
├── AirTortillaManager.java   Exportación de reservas a ficheros
├── db/
│   ├── Conexion.java         Conexión a MySQL
│   ├── ReservaDAO.java       Acceso a la tabla reservas
│   ├── UsuarioDAO.java       Acceso a la tabla usuarios
│   └── AuditoriaDAO.java     Acceso a la tabla auditoria
├── model/
│   ├── Reserva.java
│   ├── Usuario.java
│   ├── Rol.java              EMPLEADO / ADMIN
│   └── RegistroAuditoria.java
└── service/
    ├── AuthService.java      Login, usuarios, permisos por rol
    ├── AuditoriaService.java Registro de acciones
    ├── ImportadorCSV.java    Lectura y validación del CSV
    └── ReservaService.java   Asignación de localizadores
data/viajes.csv               Reservas de ejemplo
sql/                          Scripts de creación de tablas
docker-compose.yml            Base de datos MySQL
```

## Base de datos

| Tabla | Contenido |
|---|---|
| `usuarios` | `username` (único), `password_hash`, `rol`, `activo`, `fecha_alta` |
| `reservas` | `codigo` (único, `RES-000001`), `nombre`, `destino`, `origen`, `fecha_registro`, `localizador` (puede ser NULL) |
| `auditoria` | `usuario_id` (clave foránea a `usuarios`), `accion`, `tabla_afectada`, `registro_id`, `detalle`, `fecha_hora` |

## Problemas frecuentes

| Síntoma | Causa probable | Solución |
|---|---|---|
| `Falta la variable de entorno MYSQL_PASSWORD` | No está en la Run Configuration | Añádela (ver paso 2) |
| `No hay usuarios y falta la variable de entorno ADMIN_PASSWORD` | Primer arranque sin `ADMIN_PASSWORD` | Añádela y vuelve a ejecutar |
| `Communications link failure` / no conecta | MySQL no está levantado, o el puerto no coincide | `docker compose up -d` y comprueba que usa el 3307 |
| «Ya tiene N reservas, no se importa» | La importación exige tabla vacía | Usa la opción 4 y vuelve a importar |
| `Acceso denegado` | 3 intentos fallidos | Vuelve a ejecutar el programa |

## Limitaciones conocidas

- La contraseña **se ve al escribirla** en la terminal (`Scanner`; en IntelliJ `System.console()` no está disponible).
- El usuario `admin` inicial, los inicios de sesión fallidos y los intentos no autorizados **no se registran** en la auditoría.
- El apunte de auditoría se guarda justo después del cambio, no en la misma transacción.
- No hay opción de menú para desactivar o modificar usuarios.
- La importación solo admite la tabla de reservas vacía.