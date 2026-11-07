# GamerHub

Aplicación de eventos gamer. El frontend usa Spring MVC, Thymeleaf y CSS. El backend ofrece una API REST con Spring Security y consulta usuarios y eventos mediante Spring Data JPA. Ambas capas comparten un proyecto Maven.

## Acceso web

- `/`, `/home` y `/recetas` muestran el inicio público.
- `/eventos` permite buscar por nombre, categoría, fecha y ubicación.
- `/registro` y `/login` permiten crear una cuenta e iniciar sesión.
- `/eventos/{id}` y `/perfil` requieren una sesión autenticada.

## API

| Método y ruta           | Acceso     | Resultado                                                                      |
| ----------------------- | ---------- | ------------------------------------------------------------------------------ |
| `POST /api/login`       | Público    | Recibe JSON con `email` y `password`. Devuelve `token` y `type`.               |
| `GET /api/eventos`      | Público    | Devuelve el catálogo resumido. Admite `name`, `category`, `date` y `location`. |
| `GET /api/eventos/{id}` | Rol `USER` | Devuelve el detalle. Recibe `Authorization: Bearer <token>`.                   |

Spring Security autentica el correo y la contraseña contra la base de datos. Las contraseñas se almacenan con BCrypt. La web inicia sesión contra la API y guarda el JWT en la sesión del servidor. El detalle se obtiene enviando ese JWT a la API. La web conserva la protección CSRF. La API valida la firma y vigencia del JWT y consulta el rol de la cuenta. Los tokens duran 15 minutos y la clave se renueva cuando se reinicia el proceso.

## Configuración

El punto de entrada es `GamerhubApplication`. La configuración predeterminada usa H2 local. El proyecto incluye MySQL Connector/J. El perfil Spring `mysql` usa un esquema MySQL existente con codificación `utf8mb4`. La conexión se configura con `DB_URL`, `DB_USERNAME` y `DB_PASSWORD`. El archivo `gamerhub.sql` crea el esquema y carga tres eventos de muestra. Un ejemplo de URL JDBC es `jdbc:mysql://localhost:3306/gamerhub`.

La variable opcional `DEMO_PASSWORD` habilita la creación de las cuentas `player1@gamerhub.test`, `player2@gamerhub.test` y `player3@gamerhub.test`. Las cuentas existentes conservan sus contraseñas. El catálogo de muestra se genera cuando la tabla de eventos está vacía.

## Pruebas

La suite Maven comprueba las rutas públicas y privadas, el registro, BCrypt, el perfil, la búsqueda, CSRF, el cierre de sesión y el flujo JWT con las tres cuentas de demostración. Las pruebas usan H2 en memoria.
