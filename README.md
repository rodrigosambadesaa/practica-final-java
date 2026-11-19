# Garaje Pro

Aplicación completa para gestionar trabajos de taller. Incluye una API REST, una interfaz web
responsive y la aplicación Java clásica (consola y Swing).

## Arquitectura

| Componente | Tecnología | Ruta |
| --- | --- | --- |
| Frontend | Angular 22, TypeScript 6, Vitest, Nginx | `frontend/` |
| Backend | Java 21, Spring Boot 4.1, JPA/Hibernate | `backend/` |
| Base de datos | MySQL 8.4 en Docker; H2 para desarrollo local | `compose.yaml` |
| Aplicación clásica | Java 21, consola y Swing | `src/` |

## Arranque con Docker

Requisito: Docker Desktop o Docker Engine con Compose v2.

```bash
docker compose up --build -d
```

La aplicación queda disponible en:

- Web: <http://localhost:4200>
- API: <http://localhost:8080/api/trabajos>
- Salud del backend: <http://localhost:8080/actuator/health>
- Salud del frontend: <http://localhost:4200/healthz>

Compose espera a que MySQL y la API estén sanos antes de arrancar sus dependencias. Los datos de
MySQL se conservan en el volumen `mysql-data`.

Para cambiar puertos o credenciales:

```bash
cp .env.example .env
docker compose up --build -d
```

Para detener los contenedores sin borrar los datos:

```bash
docker compose down
```

Para borrar también los volúmenes de desarrollo:

```bash
docker compose down -v
```

## Aplicación Java clásica en Docker

La imagen clásica se compila y ejecuta sus pruebas durante el build:

```bash
docker compose --profile classic build classic
docker compose --profile classic run --rm classic
```

Sus ficheros `garaje.bin` y `contador.bin` se guardan en el volumen `classic-data`.

## Desarrollo local

### Backend

Requisitos: JDK 21 y Maven 3.9+.

```bash
cd backend
mvn spring-boot:run
```

Por defecto usa H2 en `backend/data/garaje-db`. Para usar MySQL, activa el perfil `mysql` y define
`SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME` y `SPRING_DATASOURCE_PASSWORD`.

### Frontend

Angular 22 requiere Node.js `>=22.22.3` o `>=24.15.0`.

```bash
cd frontend
npm ci
npm start
```

En desarrollo, configura un proxy hacia la API o sirve ambos componentes con Docker. En producción,
Nginx publica el frontend y redirige `/api` al backend.

### Aplicación clásica

```bash
javac -encoding UTF-8 -d bin src/garaje/*.java src/excepcionesgaraje/*.java \
  src/interfaces/*.java src/utilidades/*.java
java -cp bin garaje.AplicacionGestionGaraje --consola
```

Sin `--consola` se abre Swing cuando el entorno tiene interfaz gráfica.

## Pruebas

```bash
# Backend: pruebas de dominio e integración HTTP/JPA
cd backend
mvn clean test

# Frontend: pruebas de componentes y servicio HTTP
cd frontend
npm test
npm run build

# Todas las comprobaciones de build dentro de imágenes reproducibles
docker compose build
docker compose --profile classic build classic
docker compose config --quiet
```

## API

| Método | Ruta | Acción |
| --- | --- | --- |
| `GET` | `/api/trabajos` | Lista trabajos no eliminados |
| `GET` | `/api/trabajos/{id}` | Obtiene un trabajo activo |
| `POST` | `/api/trabajos` | Crea un trabajo (`201`) |
| `PATCH` | `/api/trabajos/{id}/horas` | Acumula horas |
| `PATCH` | `/api/trabajos/{id}/material` | Acumula material |
| `PATCH` | `/api/trabajos/{id}/finalizar` | Finaliza el trabajo |
| `DELETE` | `/api/trabajos/{id}` | Borrado lógico (`204`) |
| `POST` | `/api/db/seed` | Carga ejemplos si no hay trabajos activos |
| `POST` | `/api/db/reset` | Vacía la tabla de trabajos |

Los errores de validación y de negocio se devuelven como respuestas Problem Details.

## Decisiones de tipos del modelo clásico

- `long` para identificadores y contador, evitando el coste innecesario de `BigInteger`.
- `boolean` para estados obligatorios, evitando estados nulos de `Boolean`.
- `String` para texto inmutable, en lugar de buffers mutables.
- `LocalTime` para horarios y `LocalDate` para periodos laborales.
- `PeriodoLaboral` como `record`, en lugar de dos arrays de fechas que podían desincronizarse.
- `List` y copias defensivas en las API públicas, en lugar de exponer `ArrayList`.
- `BigDecimal` se mantiene para horas e importes, donde la precisión decimal sí es necesaria.

El cambio del formato serializado del modelo clásico puede hacer incompatibles ficheros `.bin`
antiguos. Si ocurre, la aplicación inicia un almacén vacío; conserva una copia externa si necesitas
migrar datos históricos.

## Integridad del modelo y verificaciones automáticas

- La identidad de cada trabajo es su ID único, independientemente del subtipo.
- Los IDs ya asignados no se pueden reasignar; se rechazan duplicados y la lista se
  consulta siempre ordenada por descripción, sin distinguir mayúsculas/minúsculas.
- Las horas y los importes no admiten valores negativos; **cero es válido** de
  acuerdo con el enunciado. Los trabajos finalizados no pueden reabrirse ni
  modificar sus horas o material, tampoco mediante los setters públicos.
- Al arrancar, la aplicación clásica sincroniza el contador con el máximo ID
  persistido, incluso si se perdió `contador.bin`.
- La consola también permite consultar un trabajo concreto (opción 8) y su plazo
  máximo (opción 9); la opción 7 sigue guardando y saliendo.
- `.github/workflows/full-tests.yml` comprueba modelo clásico, API Java,
  frontend Angular y la configuración/arranque de Docker. La comprobación de
  Java4Shell permanece en su workflow independiente.

Los incrementos de la API se validan tanto en los DTO como en el servicio: se aceptan
cero y hasta dos decimales, se rechazan cantidades negativas y se comprueba que el
acumulado no desborde las columnas `DECIMAL(12,2)`. Las modificaciones concurrentes
bloquean la fila mientras se aplica cada incremento; el listado web se ordena sin
distinguir mayúsculas/minúsculas.

## Operaciones administrativas y seguridad

Las rutas `POST /api/db/reset` y `POST /api/db/seed` requieren una clave
administrativa mediante la cabecera `X-Admin-Token`. **Si la clave no esta
configurada, ambas operaciones se rechazan**. Para habilitarlas en Docker:

1. Copia `.env.example` a `.env` si aun no existe.
2. Genera una clave aleatoria de al menos 32 caracteres con `openssl rand -hex 32`
   y asignala a `APP_ADMIN_TOKEN` en tu `.env` privado.
3. Inicia de nuevo el backend mediante `docker compose up -d --build`.
4. Introduce temporalmente esa clave en el campo **Clave administrativa** de la
   interfaz web antes de pulsar **Cargar ejemplos** o **Vaciar**.

La clave no va incluida en el frontend, ni se guarda en el almacenamiento del
navegador. Las comprobaciones se realizan en el backend. No la incluyas en
capturas, repositorios, URLs ni registros; usa HTTPS fuera de localhost.
La consola web H2 esta desactivada por defecto, pero puede habilitarse
explicitamente en desarrollo local con `H2_CONSOLE_ENABLED=true` (sin perfil MySQL).

**Importante:** esta clave protege exclusivamente los dos endpoints administrativos.
El resto de operaciones de gestion de trabajos sigue sin autenticacion; no publiques
la API para usuarios no confiables sin incorporar autenticacion/autorizacion general.
