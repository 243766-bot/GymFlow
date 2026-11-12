package com.gymflow.api.service;

import com.gymflow.api.dto.VentaItemRequest;
import com.gymflow.api.dto.VentaRequest;
import com.gymflow.api.dto.VentaResponse;
import com.gymflow.api.entity.DetalleVenta;
import com.gymflow.api.entity.Producto;
import com.gymflow.api.entity.Usuario;
import com.gymflow.api.entity.Venta;
import com.gymflow.api.exception.BadRequestException;
import com.gymflow.api.exception.NotFoundException;
import com.gymflow.api.repository.ProductoRepository;
import com.gymflow.api.repository.UsuarioRepository;
import com.gymflow.api.repository.VentaRepository;
import com.gymflow.api.util.Fechas;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class VentaService {

    private final VentaRepository ventaRepository;
    private final ProductoRepository productoRepository;
    private final UsuarioRepository usuarioRepository;

    public VentaService(VentaRepository ventaRepository,
                        ProductoRepository productoRepository,
                        UsuarioRepository usuarioRepository) {
        this.ventaRepository = ventaRepository;
        this.productoRepository = productoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    // Si algo falla (por ejemplo, no hay stock), TODO se deshace: no se vende ni se descuenta nada
    @Transactional
    public VentaResponse registrar(VentaRequest request, String emailAdmin) {
        Usuario admin = usuarioRepository.findByEmail(emailAdmin)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));

        Usuario cliente = null;
        if (request.clienteId() != null) {
            cliente = usuarioRepository.findById(request.clienteId())
                    .orElseThrow(() -> new NotFoundException("Cliente no encontrado"));
        }

        // Si el mismo producto viene dos veces, se suman las cantidades
        Map<Long, Integer> cantidades = new LinkedHashMap<>();
        for (VentaItemRequest item : request.items()) {
            cantidades.merge(item.productoId(), item.cantidad(), Integer::sum);
        }

        // Trae los productos y los bloquea mientras dura la venta
        Map<Long, Producto> productos = productoRepository.findAllByIdForUpdate(cantidades.keySet())
                .stream()
                .collect(Collectors.toMap(Producto::getId, Function.identity()));

        Venta venta = new Venta();
        venta.setAtendidoPor(admin);
        venta.setCliente(cliente);
        venta.setFecha(Fechas.ahora());

        BigDecimal total = BigDecimal.ZERO;

        for (Map.Entry<Long, Integer> entrada : cantidades.entrySet()) {
            Producto producto = productos.get(entrada.getKey());
            int cantidad = entrada.getValue();

            if (producto == null) {
                throw new NotFoundException("El producto " + entrada.getKey() + " no existe");
            }
            if (!producto.isActivo()) {
                throw new BadRequestException("El producto '" + producto.getNombre() + "' está desactivado");
            }
            if (producto.getStock() < cantidad) {
                throw new BadRequestException("Stock insuficiente de '" + producto.getNombre()
                        + "': hay " + producto.getStock() + " y se piden " + cantidad);
            }

            // Descuento automático del inventario
            producto.setStock(producto.getStock() - cantidad);

            DetalleVenta detalle = new DetalleVenta();
            detalle.setVenta(venta);
            detalle.setProducto(producto);
            detalle.setCantidad(cantidad);
            detalle.setPrecioUnitario(producto.getPrecio()); // precio de este momento
            venta.getDetalles().add(detalle);

            total = total.add(producto.getPrecio().multiply(BigDecimal.valueOf(cantidad)));
        }

        venta.setTotal(total);
        return VentaResponse.from(ventaRepository.save(venta));
    }

    // Todas las ventas (la más reciente primero) o solo las de un día
    @Transactional(readOnly = true)
    public List<VentaResponse> listar(LocalDate fecha) {
        List<Venta> ventas = (fecha == null)
                ? ventaRepository.findAllByOrderByFechaDesc()
                : ventaRepository.findByFechaGreaterThanEqualAndFechaLessThanOrderByFechaAsc(
                        fecha.atStartOfDay(), fecha.plusDays(1).atStartOfDay());

        return ventas.stream().map(VentaResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public VentaResponse obtener(Long id) {
        return ventaRepository.findById(id)
                .map(VentaResponse::from)
                .orElseThrow(() -> new NotFoundException("Venta no encontrada"));
    }
}