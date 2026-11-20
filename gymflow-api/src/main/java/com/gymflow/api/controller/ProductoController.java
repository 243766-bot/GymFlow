package com.gymflow.api.controller;

import com.gymflow.api.dto.MensajeResponse;
import com.gymflow.api.dto.ProductoRequest;
import com.gymflow.api.dto.ProductoResponse;
import com.gymflow.api.exception.ApiError;
import com.gymflow.api.service.ProductoService;
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
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Productos", description = "Administración del inventario de productos y suplementos del gimnasio. Solo ADMIN.")
@SecurityRequirement(name = "bearerAuth")
public class ProductoController {

    private final ProductoService productoService;

    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    @Operation(summary = "Listar productos del inventario", description = "Obtiene todos los productos registrados (activos e inactivos). Solo ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Inventario obtenido",
                    content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = ProductoResponse.class)))),
            @ApiResponse(responseCode = "403", description = "Requiere rol ADMIN",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class)))
    })
    @GetMapping
    public List<ProductoResponse> listar() {
        return productoService.listar();
    }

    @Operation(summary = "Obtener producto por ID", description = "Consulta el detalle y existencias de un producto específico. Solo ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Producto encontrado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProductoResponse.class))),
            @ApiResponse(responseCode = "403", description = "Requiere rol ADMIN",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class)))
    })
    @GetMapping("/{id}")
    public ProductoResponse obtener(@Parameter(description = "ID del producto", example = "1") @PathVariable Long id) {
        return productoService.obtener(id);
    }

    @Operation(summary = "Registrar nuevo producto", description = "Da de alta un producto en el catálogo con precio y stock inicial. Solo ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Producto creado exitosamente",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProductoResponse.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "403", description = "Requiere rol ADMIN",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class)))
    })
    @PostMapping
    public ResponseEntity<ProductoResponse> crear(@Valid @RequestBody ProductoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productoService.crear(request));
    }

    @Operation(summary = "Actualizar un producto", description = "Modifica nombre, categoría, tamaño, precio, stock o estado activo de un producto. Solo ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Producto actualizado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProductoResponse.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "403", description = "Requiere rol ADMIN",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class)))
    })
    @PutMapping("/{id}")
    public ProductoResponse actualizar(@Parameter(description = "ID del producto", example = "1") @PathVariable Long id,
                                       @Valid @RequestBody ProductoRequest request) {
        return productoService.actualizar(id, request);
    }

    @Operation(summary = "Eliminar o desactivar producto", description = "Elimina el producto si no tiene ventas registradas, o lo marca como inactivo para conservar historial. Solo ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Producto eliminado o desactivado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = MensajeResponse.class))),
            @ApiResponse(responseCode = "403", description = "Requiere rol ADMIN",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<MensajeResponse> eliminar(@Parameter(description = "ID del producto", example = "1") @PathVariable Long id) {
        return ResponseEntity.ok(new MensajeResponse(productoService.eliminar(id)));
    }
}