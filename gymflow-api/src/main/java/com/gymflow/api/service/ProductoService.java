package com.gymflow.api.service;

import com.gymflow.api.dto.ProductoRequest;
import com.gymflow.api.dto.ProductoResponse;
import com.gymflow.api.entity.Producto;
import com.gymflow.api.exception.NotFoundException;
import com.gymflow.api.repository.DetalleVentaRepository;
import com.gymflow.api.repository.ProductoRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final DetalleVentaRepository detalleVentaRepository;

    public ProductoService(ProductoRepository productoRepository,
                           DetalleVentaRepository detalleVentaRepository) {
        this.productoRepository = productoRepository;
        this.detalleVentaRepository = detalleVentaRepository;
    }

    @Transactional(readOnly = true)
    public List<ProductoResponse> listar() {
        return productoRepository.findAll(Sort.by("nombre")).stream()
                .map(ProductoResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProductoResponse obtener(Long id) {
        return ProductoResponse.from(buscar(id));
    }

    @Transactional
    public ProductoResponse crear(ProductoRequest request) {
        Producto producto = new Producto();
        aplicar(producto, request);
        return ProductoResponse.from(productoRepository.save(producto));
    }

    @Transactional
    public ProductoResponse actualizar(Long id, ProductoRequest request) {
        Producto producto = buscar(id);
        aplicar(producto, request);
        return ProductoResponse.from(productoRepository.save(producto));
    }

    // Devuelve el mensaje que se le muestra al administrador
    @Transactional
    public String eliminar(Long id) {
        Producto producto = buscar(id);

        // Si ya se vendió, borrarlo rompería el historial de ventas: se desactiva
        if (detalleVentaRepository.existsByProductoId(id)) {
            producto.setActivo(false);
            productoRepository.save(producto);
            return "El producto tiene ventas registradas, así que se desactivó en lugar de eliminarse";
        }

        productoRepository.delete(producto);
        return "Producto eliminado correctamente";
    }

    private Producto buscar(Long id) {
        return productoRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Producto no encontrado"));
    }

    private void aplicar(Producto producto, ProductoRequest request) {
        producto.setNombre(request.nombre().trim());
        producto.setCategoria(request.categoria().trim());
        producto.setTamano(limpiar(request.tamano()));
        producto.setPrecio(request.precio());
        producto.setStock(request.stock());
        if (request.activo() != null) {
            producto.setActivo(request.activo());
        }
    }

    // Un texto vacío se guarda como "sin dato"
    private String limpiar(String texto) {
        return (texto == null || texto.isBlank()) ? null : texto.trim();
    }
}