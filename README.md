# GymFlow

Sistema web de gestión para gimnasios: clientes, planes, membresías, inventario, ventas, caja con corte diario y pagos en línea. Los clientes pueden iniciar sesión con **correo y contraseña** o con **reconocimiento facial** desde la cámara web.

Proyecto final individual de la materia *Aplicaciones Web Orientadas a Servicios* (UP Chiapas, 5.º semestre, Grupo 5A).
**Autor:** Víctor Alfonso Álvarez Castillo (matrícula 243766).

## Demo en línea

| Parte | URL |
| --- | --- |
| Cliente web (Vercel) | https://gym-flow-self.vercel.app |
| API (Render) | https://gymflow-xvua.onrender.com/api/v1 |
| Swagger UI | https://gymflow-xvua.onrender.com/swagger-ui.html |

> La API usa el plan gratuito de Render y se duerme tras 15 minutos sin uso. La primera petición puede tardar entre 30 y 60 segundos. Para despertarla, abre `https://gymflow-xvua.onrender.com/api/v1/plans`.

## Funcionalidades

- Registro e inicio de sesión con JWT; roles **Administrador** y **Cliente**.
- Login facial: descriptor de 128 valores (face-api), desafío de un solo uso de 60 s, umbral de distancia 0.5 y límite de 5 intentos fallidos cada 5 minutos por IP.
- Gestión de clientes: alta, edición, baneo/reactivación y baja (con historial se pide banear).
- Planes y membresías: asignación, renovación y cancelación.
- Pagos en efectivo (ligados a la membresía y al corte) y en línea con **Mercado Pago Checkout Pro**; sincronización automática de pagos pendientes cada cierto tiempo y botón de verificación en el perfil.
- Productos, ventas con descuento de stock bajo bloqueo pesimista y movimientos de caja.
- Corte diario de caja (zona horaria America/Mexico_City).
- Documentación OpenAPI con Swagger UI y colección Postman de 143 pruebas.

## Tecnologías

| Capa | Tecnología |
| --- | --- |
| Backend | Java 17, Spring Boot 4.1, Spring Web, Spring Data JPA / Hibernate, Spring Security (JWT sin estado, BCrypt), Jakarta Validation, springdoc-openapi |
| Base de datos | PostgreSQL (Neon en la nube) |
| Frontend | React, Vite, react-router-dom, @vladmandic/face-api |
| Pagos | Mercado Pago (cuentas de prueba) |
| Despliegue | Render (Docker), Vercel, Neon |

## Estructura del repositorio

```
GymFlow/
├── gymflow-api/     API REST (Spring Boot, Maven, Dockerfile)
├── gymflow-web/     Cliente web (React + Vite, vercel.json)
└── docs/            Diagrama E-R, diseño de endpoints, colecciones Postman
```

## Requisitos

- JDK 17
- Maven (o el wrapper `mvnw` incluido)
- Node.js 18 o superior
- PostgreSQL local o una base en Neon
- Cámara web (para el reconocimiento facial)
- Cuentas de prueba de Mercado Pago (solo para probar pagos en línea)

## Ejecutar en local

### 1. Base de datos

Crea una base vacía en PostgreSQL (por ejemplo `gymflow`). Las tablas se crean solas al arrancar (`ddl-auto=update`).

### 2. Backend

Crea el archivo `gymflow-api/src/main/resources/application-local.properties` con tus propios valores. Este archivo está en `.gitignore` y **nunca debe subirse al repositorio**.

Debe definir: la URL, usuario y contraseña de la base de datos, el secreto del JWT, el correo y contraseña del administrador inicial y, opcionalmente, el access token de Mercado Pago. Usa los mismos nombres de propiedad que aparecen en `application.properties`.

Después, desde `gymflow-api`:

```bash
./mvnw spring-boot:run        # Linux / macOS
.\mvnw.cmd spring-boot:run    # Windows
```

La API queda en `http://localhost:8080`. Al arrancar por primera vez se crean los roles y el usuario administrador.

### 3. Frontend

Desde `gymflow-web`:

```bash
npm install
```

Copia los modelos de reconocimiento facial a la carpeta pública (solo la primera vez):

```bash
# Linux / macOS
cp -r node_modules/@vladmandic/face-api/model/* public/models/
# Windows (PowerShell)
Copy-Item node_modules\@vladmandic\face-api\model\* public\models\ -Recurse
```

Crea `gymflow-web/.env` con la URL de la API:

```
VITE_API_URL=http://localhost:8080/api/v1
```

Arranca el cliente:

```bash
npm run dev
```

Abre la dirección que muestra Vite (normalmente `http://localhost:5173`).

## Variables de entorno (producción)

Se configuran en el panel de Render; no se guardan en el repositorio.

| Variable | Para qué sirve |
| --- | --- |
| `SPRING_PROFILES_ACTIVE` | `prod` |
| `SPRING_DATASOURCE_URL` | URL JDBC de Neon (conexión directa, `sslmode=require`) |
| `SPRING_DATASOURCE_USERNAME` | Usuario de la base |
| `SPRING_DATASOURCE_PASSWORD` | Contraseña de la base |
| `JWT_SECRET` | Secreto con el que se firman los tokens |
| `JWT_EXPIRATION_MS` | Duración del token en milisegundos |
| `APP_ADMIN_EMAIL` | Correo del administrador inicial |
| `APP_ADMIN_PASSWORD` | Contraseña del administrador inicial |
| `MERCADOPAGO_ACCESS_TOKEN` | Access token de producción de la cuenta vendedora de prueba |
| `FRONTEND_URL` | URL del cliente web (retorno de Mercado Pago) |
| `CORS_ALLOWED_ORIGINS` | Orígenes permitidos por CORS |

En Vercel solo se necesita `VITE_API_URL` apuntando a la API de Render.

## Probar la API

- **Swagger UI:** `/swagger-ui.html`. Inicia sesión con `POST /auth/login`, copia el token y pulsa *Authorize*.
- **Postman:** importa `docs/GymFlow-API-Pruebas-Completas.postman_collection.json`. Define las variables `baseUrl`, `adminEmail` y `adminPassword`; la carpeta *0 Preparación* inicia sesión y guarda el token. Ejecuta toda la colección con el Collection Runner.
- **Endpoints:** ver `docs/` (diseño de endpoints con método, ruta, rol y códigos de respuesta).

## Pagos de prueba con Mercado Pago

1. Usa las cuentas de prueba creadas en el panel de desarrolladores (vendedor y comprador).
2. Pon el access token de producción de la cuenta **vendedora** de prueba en la configuración, nunca en el código.
3. Paga desde una ventana de incógnito con la cuenta **compradora** de prueba.
4. Si no se regresa automáticamente al sitio, abre *Perfil* y pulsa verificar pago: la membresía se activa una sola vez, tras comprobar el estado y el monto.

## Seguridad

- Contraseñas con BCrypt; JWT sin estado; autorización por rol con `@PreAuthorize`.
- Ningún secreto se guarda en el repositorio: `application-local.properties` está ignorado y los valores de producción viven en variables de entorno.
- Validación de entradas en el servidor y manejo global de errores con respuestas estandarizadas.
- Límite de intentos y desafío de un solo uso en el login facial.

## Licencia y uso

Proyecto académico. Uso educativo.
