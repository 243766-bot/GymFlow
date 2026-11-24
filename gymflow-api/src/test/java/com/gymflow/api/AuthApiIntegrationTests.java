package com.gymflow.api;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Pruebas de integración de la API de GymFlow.
 * Levantan la aplicación completa (Spring Security, JPA y PostgreSQL)
 * y llaman a los endpoints REST como lo haría un cliente real.
 * En GitHub Actions se ejecutan contra un PostgreSQL temporal.
 */
@SpringBootTest
@AutoConfigureMockMvc
class AuthApiIntegrationTests {

    @Autowired
    private MockMvc mvc;

    // Correo distinto en cada ejecución para no chocar con registros previos
    private String correoNuevo() {
        return "ci-" + UUID.randomUUID().toString().substring(0, 8) + "@gymflow.com";
    }

    private String cuerpoRegistro(String email, String telefono) {
        return """
                {"nombre":"Cliente CI","email":"%s","password":"Prueba2026","telefono":"%s"}
                """.formatted(email, telefono);
    }

    private String registrarYObtenerToken(String email) throws Exception {
        MvcResult r = mvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpoRegistro(email, "9611234567")))
                .andExpect(status().isCreated())
                .andReturn();
        Matcher m = Pattern.compile("\"token\"\\s*:\\s*\"([^\"]+)\"")
                .matcher(r.getResponse().getContentAsString());
        assertThat(m.find()).as("la respuesta debe traer un token").isTrue();
        return m.group(1);
    }

    @Test
    @DisplayName("POST /auth/register crea al cliente y devuelve un JWT (201)")
    void registroDevuelveToken() throws Exception {
        mvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpoRegistro(correoNuevo(), "9611234567")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    @DisplayName("POST /auth/register rechaza un teléfono que no tiene 10 dígitos (400)")
    void registroConTelefonoInvalido() throws Exception {
        mvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpoRegistro(correoNuevo(), "96abc")))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /auth/login con credenciales correctas devuelve un JWT (200)")
    void loginCorrecto() throws Exception {
        String email = correoNuevo();
        registrarYObtenerToken(email);

        mvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","password":"Prueba2026"}
                                """.formatted(email)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    @DisplayName("POST /auth/login con contraseña incorrecta responde 401")
    void loginIncorrecto() throws Exception {
        String email = correoNuevo();
        registrarYObtenerToken(email);

        mvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","password":"OtraClave99"}
                                """.formatted(email)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /users/me sin token responde 401 y con token responde 200")
    void perfilProtegidoConJwt() throws Exception {
        mvc.perform(get("/api/v1/users/me"))
                .andExpect(status().isUnauthorized());

        String email = correoNuevo();
        String token = registrarYObtenerToken(email);

        mvc.perform(get("/api/v1/users/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email));
    }

    @Test
    @DisplayName("GET /plans es público (200)")
    void planesPublicos() throws Exception {
        mvc.perform(get("/api/v1/plans"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /v3/api-docs publica el contrato OpenAPI (200)")
    void contratoOpenApi() throws Exception {
        mvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.openapi").exists());
    }
}
