package com.gymflow.api.controller;

import com.gymflow.api.dto.BanRequest;
import com.gymflow.api.dto.ClienteResponse;
import com.gymflow.api.dto.MensajeResponse;
import com.gymflow.api.dto.UsuarioCreateRequest;
import com.gymflow.api.dto.UsuarioResponse;
import com.gymflow.api.dto.UsuarioUpdateRequest;
import com.gymflow.api.exception.ApiError;
import com.gymflow.api.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "Clientes y perfil", description = "Consulta del perfil propio y administración CRUD de clientes con baneo (ADMIN).")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UsuarioService usuarioService;

    public UserController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @Operation(summary = "Obtener perfil del usuario autenticado", description = "Devuelve los datos básicos del usuario dueño del token JWT actual.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Perfil obtenido correctamente",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UsuarioResponse.class))),
            @ApiResponse(responseCode = "401", description = "Token JWT ausente o inválido",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class)))
    })
    @GetMapping("/me")
    public UsuarioResponse me(Authentication authentication) {
        return usuarioService.perfil(authentication.getName());
    }

    @Operation(summary = "Listar todos los usuarios/clientes", description = "Obtiene el listado completo de usuarios registrados junto con el estado de su rostro y membresía. Solo ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listado obtenido correctamente",
                    content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = ClienteResponse.class)))),
            @ApiResponse(responseCode = "401", description = "No autenticado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "403", description = "Requiere rol ADMIN",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class)))
    })
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<ClienteResponse> listar() {
        return usuarioService.listar();
    }

    @Operation(summary = "Consultar detalle de un cliente por ID", description = "Devuelve información detallada del cliente, plantilla facial y membresía vigente. Solo ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cliente encontrado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ClienteResponse.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "403", description = "Requiere rol ADMIN",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class)))
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ClienteResponse obtener(@Parameter(description = "ID único del usuario", example = "2") @PathVariable Long id) {
        return usuarioService.obtener(id);
    }

    @Operation(summary = "Crear un nuevo usuario o administrador", description = "Permite al administrador dar de alta un cliente (ROLE_USER) u otro administrador (ROLE_ADMIN).")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Usuario creado exitosamente",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ClienteResponse.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o correo duplicado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "403", description = "Requiere rol ADMIN",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class)))
    })
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ClienteResponse> crear(@Valid @RequestBody UsuarioCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioService.crear(request));
    }

    @Operation(summary = "Actualizar datos de un usuario", description = "Modifica nombre, correo, teléfono y opcionalmente la contraseña de un usuario. Solo ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuario actualizado correctamente",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ClienteResponse.class))),
            @ApiResponse(responseCode = "400", description = "Datos de validación incorrectos",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "403", description = "Requiere rol ADMIN",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class)))
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ClienteResponse actualizar(@Parameter(description = "ID único del usuario", example = "2") @PathVariable Long id,
                                      @Valid @RequestBody UsuarioUpdateRequest request) {
        return usuarioService.actualizar(id, request);
    }

    @Operation(summary = "Banear o reactivar acceso de un usuario", description = "Suspende (baneado=true) o reactiva (baneado=false) la cuenta de un cliente. Solo ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Estado de acceso actualizado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ClienteResponse.class))),
            @ApiResponse(responseCode = "400", description = "Solicitud inválida (ej. intentar banearse a sí mismo)",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "403", description = "Requiere rol ADMIN",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class)))
    })
    @PatchMapping("/{id}/ban")
    @PreAuthorize("hasRole('ADMIN')")
    public ClienteResponse banear(@Parameter(description = "ID único del usuario", example = "2") @PathVariable Long id,
                                  @Valid @RequestBody BanRequest request,
                                  Authentication authentication) {
        return usuarioService.cambiarBan(id, request.baneado(), authentication.getName());
    }

    @Operation(summary = "Eliminar usuario", description = "Elimina permanentemente la cuenta de un usuario del sistema. Solo ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuario eliminado correctamente",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = MensajeResponse.class))),
            @ApiResponse(responseCode = "400", description = "No se puede eliminar por registros asociados o autoeliminación",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "403", description = "Requiere rol ADMIN",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class)))
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MensajeResponse> eliminar(@Parameter(description = "ID único del usuario", example = "2") @PathVariable Long id,
                                                    Authentication authentication) {
        usuarioService.eliminar(id, authentication.getName());
        return ResponseEntity.ok(new MensajeResponse("Usuario eliminado correctamente"));
    }
}