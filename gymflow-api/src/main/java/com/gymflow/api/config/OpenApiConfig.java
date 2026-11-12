package com.gymflow.api.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.tags.Tag;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    private static final String ESQUEMA_JWT = "bearerAuth";

    // Información general y botón "Authorize" (JWT en el encabezado Authorization: Bearer ...)
    @Bean
    OpenAPI gymFlowOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("GymFlow API")
                        .version("1.0.0")
                        .description("API REST para administrar clientes, membresías, inventario, "
                                + "ventas y caja de un gimnasio, con inicio de sesión por correo "
                                + "y por reconocimiento facial."))
                .components(new Components().addSecuritySchemes(ESQUEMA_JWT,
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList(ESQUEMA_JWT));
    }

    // Ordena los endpoints por tema y quita el candado a los públicos
    @Bean
    OpenApiCustomizer organizarEndpoints() {
        return openApi -> {
            openApi.getPaths().forEach((ruta, item) -> {
                boolean esPublica = ruta.startsWith("/api/v1/auth/");
                String etiqueta = etiquetaPara(ruta);

                item.readOperations().forEach(operacion -> {
                    operacion.setTags(List.of(etiqueta));
                    if (esPublica) {
                        operacion.setSecurity(List.of()); // sin candado: no pide token
                    }
                });

                // GET /plans también es público
                if (ruta.equals("/api/v1/plans") && item.getGet() != null) {
                    item.getGet().setSecurity(List.of());
                }
            });

            openApi.setTags(etiquetas());
        };
    }

    private static String etiquetaPara(String ruta) {
        if (ruta.startsWith("/api/v1/auth/face")) return "Login facial";
        if (ruta.startsWith("/api/v1/auth")) return "Autenticación";
        if (ruta.endsWith("/face")) return "Rostro de clientes";
        if (ruta.startsWith("/api/v1/users")) return "Clientes y perfil";
        if (ruta.startsWith("/api/v1/plans")) return "Planes";
        if (ruta.startsWith("/api/v1/memberships")) return "Membresías";
        if (ruta.startsWith("/api/v1/payments")) return "Pagos";
        if (ruta.startsWith("/api/v1/products")) return "Productos";
        if (ruta.startsWith("/api/v1/sales")) return "Ventas";
        if (ruta.startsWith("/api/v1/cash")) return "Caja";
        return "Otros";
    }

    // El orden de esta lista es el orden en que salen los grupos en la página
    private static List<Tag> etiquetas() {
        return List.of(
                new Tag().name("Autenticación").description("Registro e inicio de sesión con correo y contraseña. Públicos."),
                new Tag().name("Login facial").description("Desafío de un solo uso e inicio de sesión con el rostro (pantalla de la caja). Públicos."),
                new Tag().name("Clientes y perfil").description("Perfil propio y CRUD de clientes con baneo. Solo ADMIN, salvo /users/me."),
                new Tag().name("Rostro de clientes").description("Registro, consulta y eliminación del rostro de un cliente. Solo ADMIN."),
                new Tag().name("Planes").description("Planes de membresía. La consulta es pública; el resto, solo ADMIN."),
                new Tag().name("Membresías").description("Alta, edición y renovación (ADMIN) y consulta de la membresía propia (USER)."),
                new Tag().name("Pagos").description("Historial de pagos del cliente. Los cobros en efectivo se registran al asignar o renovar una membresía."),
                new Tag().name("Productos").description("Inventario. Solo ADMIN."),
                new Tag().name("Ventas").description("Registro de ventas con descuento automático de stock. Solo ADMIN."),
                new Tag().name("Caja").description("Salidas de dinero y corte diario. Solo ADMIN.")
        );
    }
}