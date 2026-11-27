# U4 · Plan y documentación de pruebas de servicios web (QA) — GymFlow API

**Proyecto:** GymFlow — API REST de gestión de gimnasios (Spring Boot 4.1 · Java 17 · PostgreSQL · JWT)
**Autor:** Víctor Alfonso Álvarez Castillo (243766) · Grupo 5A · *Aplicaciones Web Orientadas a Servicios*
**Fecha de ejecución:** 26/11/2026
**Herramienta:** Postman (colección v2.1) ejecutada con el Collection Runner / Newman 6

---

## 1. Objetivo

Validar la estabilidad, la seguridad y la integridad de la API de GymFlow con casos de prueba estructurados y semiautomatizados en Postman. Las pruebas cubren tanto los flujos esperados como los escenarios de error. Cada caso trae *scripts* de prueba (`pm.test`) que comprueban de forma automática el código HTTP, la estructura de la respuesta y que no se filtre información interna.

## 2. Alcance

| Incluido | Fuera de alcance |
| --- | --- |
| Autenticación (`/auth/register`, `/auth/login`) | Login facial (`/auth/face/*`), porque necesita cámara y descriptores reales |
| Productos (`/products` CRUD) | Pagos en línea con Mercado Pago (servicio externo) |
| Ventas e inventario (`/sales`) | Pruebas de carga o estrés |
| Planes públicos (`/plans`) | Cliente web (React) |
| Control de acceso por rol, CORS, encabezados y verbos HTTP | |

**Trazabilidad:** todos los endpoints probados aparecen en `docs/openapi.yaml` y en `docs/GymFlow - Diseño de Endpoints de la API.pdf`.

## 3. Entorno de pruebas

| Elemento | Valor |
| --- | --- |
| URL base | `http://localhost:8080/api/v1` |
| Backend | Spring Boot 4.1 con el perfil `local` |
| Base de datos | PostgreSQL 18 local |
| Usuarios | Administrador inicial (`ROLE_ADMIN`) y un cliente creado por la propia colección (`ROLE_USER`) |
| Origen CORS permitido | `http://localhost:5173` (cliente web Vite) |

## 4. Archivos de esta carpeta

| Archivo | Contenido |
| --- | --- |
| `GymFlow-U4-Pruebas-QA.postman_collection.json` | Colección con los 26 casos (CP-01 a CP-26), más las peticiones de preparación (`PREP`) y de verificación (`VER`) |
| `GymFlow-Local.postman_environment.json` | Entorno sin credenciales. Hay que llenar `adminEmail` y `adminPassword` |
| `evidencias/CP-XX.png` | Una captura por caso: petición enviada, código HTTP, cuerpo JSON de la respuesta y resultado de los tests |
| `evidencias/CP-21-antes.png` | Evidencia del defecto encontrado, antes de corregirlo |

### Cómo ejecutar

1. Levanta la API (`.\mvnw.cmd spring-boot:run` dentro de `gymflow-api`).
2. En Postman, importa la colección y el entorno, y llena `adminEmail` y `adminPassword` en el entorno.
3. Ejecuta la colección completa **en orden** con el *Collection Runner*, porque los casos comparten variables (token e id de producto). Otra opción es usar la línea de comandos:

```bash
npx newman run docs/testing/GymFlow-U4-Pruebas-QA.postman_collection.json \
  -e docs/testing/GymFlow-Local.postman_environment.json \
  --env-var adminEmail=<correo> --env-var adminPassword=<contraseña>
```

## 5. Matriz de pruebas

Las validaciones que se aplican a todos los casos de error son:

- el cuerpo sigue el formato estandarizado `{timestamp, status, error, message}`;
- la respuesta no contiene `Exception`, `at com.`, `org.hibernate`, `SQL`, `jdbc`, `password` ni *stack traces*.

| ID | Categoría | Endpoint | Descripción / datos de entrada | Resultado esperado | Resultado obtenido | Tiempo | Estado | Evidencia |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| CP-01 | Funcional | `POST /auth/login` | Credenciales válidas del administrador | 200, token JWT `Bearer`, sin contraseña en la respuesta | **200 OK**, token y datos del usuario | 73 ms | ✅ Pasa | [CP-01](evidencias/CP-01.png) |
| CP-02 | Funcional | `POST /products` | Producto válido (precio 890, stock 5) | 201 con el `id` asignado | **201 Created**, id generado | 9 ms | ✅ Pasa | [CP-02](evidencias/CP-02.png) |
| CP-03 | Funcional | `GET /products/{id}` | Id del producto de CP-02 | 200 con el producto | **200 OK** | 10 ms | ✅ Pasa | [CP-03](evidencias/CP-03.png) |
| CP-04 | Funcional | `PUT /products/{id}` | Cambiar el precio a 950.50 | 200 con el precio actualizado | **200 OK**, `precio: 950.5` | 11 ms | ✅ Pasa | [CP-04](evidencias/CP-04.png) |
| CP-05 | Funcional | `POST /sales` | Venta de 2 piezas del producto | 201, total 1901 y stock 5 → 3 | **201 Created**, total 1901. La petición VER confirma stock = 3 | 18 ms | ✅ Pasa | [CP-05](evidencias/CP-05.png) |
| CP-06 | Funcional | `DELETE /products/{id}` | Producto temporal sin ventas | Eliminado; después el GET da 404 | **200 OK** "Producto eliminado correctamente". La petición VER confirma 404 | 11 ms | ✅ Pasa (ver H-02) | [CP-06](evidencias/CP-06.png) |
| CP-07 | Validación de tipos | `POST /products` | `"precio": "abc"` (texto en un campo numérico) | 400 controlado | **400** "El cuerpo de la petición no es un JSON válido" | 8 ms | ✅ Pasa (ver H-03) | [CP-07](evidencias/CP-07.png) |
| CP-08 | Validación de tipos | `POST /auth/register` | `"email": "correo-sin-arroba.com"` | 400 que señala el campo `email` | **400** "email: El correo no tiene un formato válido…" | 5 ms | ✅ Pasa | [CP-08](evidencias/CP-08.png) |
| CP-09 | Validación de esquema | `POST /products` | `nombre: ""`, `categoria: null`, `precio: null`, `stock: -4` | 400 que lista cada campo inválido | **400** con los 4 campos reportados | 10 ms | ✅ Pasa | [CP-09](evidencias/CP-09.png) |
| CP-10 | Validación de esquema | `POST /products` | JSON sin la llave de cierre `}` | 400 sin tumbar el servidor | **400** "El cuerpo de la petición no es un JSON válido" | 9 ms | ✅ Pasa | [CP-10](evidencias/CP-10.png) |
| CP-11 | Validación de tipos | `GET /products/abc` | Id no numérico en la URL | 400 | **400** "El valor del parámetro 'id' no es válido" | 8 ms | ✅ Pasa | [CP-11](evidencias/CP-11.png) |
| CP-12 | Lógica de negocio | `POST /sales` | Pedir 50 piezas con stock 3 | 400 "Stock insuficiente" y el stock no cambia | **400** "Stock insuficiente… hay 3 y se piden 50". La petición VER confirma stock = 3 | 13 ms | ✅ Pasa | [CP-12](evidencias/CP-12.png) |
| CP-13 | Lógica de negocio | `POST /auth/register` | Correo ya registrado (el del cliente de PREP) | 400 de duplicado | **400** "Ese correo ya está registrado" | 7 ms | ✅ Pasa | [CP-13](evidencias/CP-13.png) |
| CP-14 | Lógica de negocio | `GET /products/999999` | Id inexistente | 404 | **404** "Producto no encontrado" | 9 ms | ✅ Pasa | [CP-14](evidencias/CP-14.png) |
| CP-15 | Seguridad (acceso) | `GET /products` | Sin encabezado `Authorization` | 401 | **401 Unauthorized** | 2 ms | ✅ Pasa (ver H-04) | [CP-15](evidencias/CP-15.png) |
| CP-16 | Seguridad (acceso) | `GET /products` | JWT con la firma alterada | 401 | **401 Unauthorized** | 2 ms | ✅ Pasa | [CP-16](evidencias/CP-16.png) |
| CP-17 | Seguridad (acceso) | `GET /products` | Token de un cliente (`ROLE_USER`) en un endpoint solo para ADMIN | 403 | **403 Forbidden** "Access Denied" | 8 ms | ✅ Pasa | [CP-17](evidencias/CP-17.png) |
| CP-18 | Seguridad (inyección) | `POST /auth/login` | `email: "' OR 1=1 --"`, `password: "' OR '1'='1"` | Rechazo (400/401) sin token | **400** "email: El correo no tiene un formato válido". No inicia sesión | 3 ms | ✅ Pasa | [CP-18](evidencias/CP-18.png) |
| CP-19 | Seguridad (inyección) | `GET /products/1 OR 1=1` | SQL en un parámetro de ruta | 400 sin errores de BD | **400** "El valor del parámetro 'id' no es válido" | 7 ms | ✅ Pasa | [CP-19](evidencias/CP-19.png) |
| CP-20 | Seguridad (sanitización) | `POST /auth/register` | `nombre: "<script>alert(1)</script>"` | 400 | **400** "nombre: El nombre solo puede llevar letras…" | 5 ms | ✅ Pasa | [CP-20](evidencias/CP-20.png) |
| CP-21 | Seguridad (sanitización) | `POST /products` | `nombre: "<script>alert('xss')</script>"` | 400, o el texto neutralizado | 1.ª ejecución: **201**, el script se guardó (**defecto H-01**). Tras la corrección: **400** "nombre: El nombre no puede contener los caracteres < o >" | 8 ms | ✅ Pasa tras la corrección | [CP-21-antes](evidencias/CP-21-antes.png) · [CP-21](evidencias/CP-21.png) |
| CP-22 | Buenas prácticas REST | `PATCH /products` | Verbo no soportado en la colección | 405 | **405** "Método no permitido para esta ruta" | 8 ms | ✅ Pasa | [CP-22](evidencias/CP-22.png) |
| CP-23 | Buenas prácticas REST | `POST /products` | `Content-Type: text/plain` | 415 | **415** "Envía el cuerpo como application/json" | 6 ms | ✅ Pasa | [CP-23](evidencias/CP-23.png) |
| CP-24 | Buenas prácticas (CORS) | `OPTIONS /plans` | Preflight desde `https://sitio-malicioso.com` | 403 sin `Access-Control-Allow-Origin` | **403**, sin encabezado CORS | 2 ms | ✅ Pasa | [CP-24](evidencias/CP-24.png) |
| CP-25 | Buenas prácticas (CORS) | `OPTIONS /plans` | Preflight desde `http://localhost:5173` | 200 con `Access-Control-Allow-Origin` del cliente | **200**, encabezado correcto | 3 ms | ✅ Pasa | [CP-25](evidencias/CP-25.png) |
| CP-26 | Buenas prácticas (rendimiento) | `GET /plans` | Endpoint público | 200, `application/json`, menos de 2000 ms | **200**, arreglo JSON | 6 ms | ✅ Pasa | [CP-26](evidencias/CP-26.png) |

### Resumen por categoría

| Categoría | Casos | Pasan |
| --- | --- | --- |
| Funcionales (camino feliz) | CP-01 a CP-06 | 6/6 |
| Validación de tipos y esquemas | CP-07 a CP-11 | 5/5 |
| Lógica de negocio | CP-12 a CP-14 | 3/3 |
| Seguridad (acceso, inyección, sanitización, exposición de información) | CP-15 a CP-21 | 7/7 (CP-21 después de la corrección) |
| Buenas prácticas REST (verbos, encabezados, CORS, tiempos) | CP-22 a CP-26 | 5/5 |
| **Total** | **26 casos · 94 aserciones automáticas** | **26/26 · 94/94** |

La **exposición de información** se revisa en todos los casos de error (4xx) con el test *"No expone trazas, SQL ni credenciales"*. Ninguna respuesta mostró *stack traces*, nombres de tablas, consultas SQL ni credenciales. El `GlobalExceptionHandler` registra el detalle en el log del servidor y al cliente solo le devuelve un mensaje genérico.

## 6. Hallazgos (defectos y observaciones)

| ID | Severidad | Caso | Descripción | Estado |
| --- | --- | --- | --- | --- |
| **H-01** | Alta | CP-21 | **XSS almacenado en productos.** `POST /products` aceptaba `<script>alert('xss')</script>` en `nombre`, respondía **201** y lo guardaba tal cual (evidencia `CP-21-antes.png`). El registro de clientes sí validaba el nombre, pero `ProductoRequest` no. Si otro cliente consumiera la API sin escapar el HTML, el script se ejecutaría. | **Corregido.** Se agregó `@Pattern(regexp = "^[^<>]*$")` a `nombre`, `categoria` y `tamano` en `ProductoRequest.java`. Al volver a ejecutar, CP-21 responde **400**. |
| H-02 | Baja | CP-06 | `DELETE /products/{id}` responde **200** con un mensaje en lugar de **204 No Content**. Es intencional: el mensaje indica si el producto se *eliminó* o solo se *desactivó* porque tiene ventas. Está documentado en el OpenAPI, así que se mantiene. | Aceptado (decisión de diseño) |
| H-03 | Baja | CP-07 | Cuando un campo numérico trae texto, la API sí responde 400, pero el mensaje es genérico ("no es un JSON válido") y no dice qué campo falló. | Mejora pendiente: leer el campo de `InvalidFormatException` en el handler |
| H-04 | Baja | CP-15, CP-16 | Las respuestas 401 que genera Spring Security (sin token o con token inválido) llegan **sin cuerpo JSON**, a diferencia del resto de errores. No expone información, pero rompe la uniformidad del formato `ApiError`. | Mejora pendiente: `AuthenticationEntryPoint` propio que devuelva `ApiError` |

## 7. Conclusiones

- La API respondió de forma controlada en los 26 escenarios. Ningún dato inválido, malicioso o mal formado provocó un error 500 ni detuvo el servidor.
- El control de acceso con JWT y roles funciona: sin token o con token alterado responde 401, y con un rol sin permisos responde 403.
- Las pruebas de inyección SQL se rechazan desde la validación de entrada. Además, JPA usa consultas parametrizadas, así que el valor nunca llega a armarse como SQL.
- Las pruebas encontraron un defecto real de seguridad (H-01, XSS almacenado), que se corrigió y se volvió a verificar antes de la entrega.
- Los tiempos de respuesta en local quedaron entre 2 y 73 ms, muy por debajo del umbral de 2000 ms.
