package com.gymflow.api.service;

import com.gymflow.api.dto.CorteDiarioResponse;
import com.gymflow.api.dto.MovimientoCajaRequest;
import com.gymflow.api.dto.MovimientoCajaResponse;
import com.gymflow.api.dto.PagoResponse;
import com.gymflow.api.dto.VentaResumenResponse;
import com.gymflow.api.entity.EstadoPago;
import com.gymflow.api.entity.MetodoPago;
import com.gymflow.api.entity.MovimientoCaja;
import com.gymflow.api.entity.Pago;
import com.gymflow.api.entity.TipoMovimiento;
import com.gymflow.api.entity.Venta;
import com.gymflow.api.repository.MovimientoCajaRepository;
import com.gymflow.api.repository.PagoRepository;
import com.gymflow.api.repository.VentaRepository;
import com.gymflow.api.util.Fechas;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class CajaService {

    private final MovimientoCajaRepository movimientoRepository;
    private final VentaRepository ventaRepository;
    private final PagoRepository pagoRepository;

    public CajaService(MovimientoCajaRepository movimientoRepository,
                       VentaRepository ventaRepository,
                       PagoRepository pagoRepository) {
        this.movimientoRepository = movimientoRepository;
        this.ventaRepository = ventaRepository;
        this.pagoRepository = pagoRepository;
    }

    @Transactional
    public MovimientoCajaResponse registrarSalida(MovimientoCajaRequest request) {
        MovimientoCaja movimiento = new MovimientoCaja();
        movimiento.setTipo(TipoMovimiento.SALIDA);
        movimiento.setConcepto(request.concepto().trim());
        movimiento.setMonto(request.monto());
        movimiento.setFecha(Fechas.ahora());

        return MovimientoCajaResponse.from(movimientoRepository.save(movimiento));
    }

    // Si no se manda fecha, se usa la de hoy
    @Transactional(readOnly = true)
    public CorteDiarioResponse corteDiario(LocalDate fecha) {
        LocalDate dia = (fecha != null) ? fecha : Fechas.hoy();
        LocalDateTime desde = dia.atStartOfDay();
        LocalDateTime hasta = dia.plusDays(1).atStartOfDay();

        List<Venta> ventas = ventaRepository
                .findByFechaGreaterThanEqualAndFechaLessThanOrderByFechaAsc(desde, hasta);
        List<MovimientoCaja> salidas = movimientoRepository
                .findByFechaGreaterThanEqualAndFechaLessThanOrderByFechaAsc(desde, hasta);

        // Solo cuentan los pagos aprobados (un pago pendiente o rechazado no es dinero cobrado)
        List<Pago> pagos = pagoRepository
                .findByFechaGreaterThanEqualAndFechaLessThanOrderByFechaAsc(desde, hasta).stream()
                .filter(p -> p.getEstado() == EstadoPago.APROBADO)
                .toList();

        BigDecimal totalVentas = ventas.stream()
                .map(Venta::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalSalidas = salidas.stream()
                .map(MovimientoCaja::getMonto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal ingresosEfectivo = sumar(pagos, MetodoPago.EFECTIVO);
        BigDecimal ingresosEnLinea = sumar(pagos, MetodoPago.MERCADO_PAGO);

        BigDecimal balance = totalVentas
                .add(ingresosEfectivo)
                .add(ingresosEnLinea)
                .subtract(totalSalidas);

        return new CorteDiarioResponse(
                dia,
                ventas.size(),
                totalVentas,
                ingresosEfectivo,
                ingresosEnLinea,
                totalSalidas,
                balance,
                ventas.stream().map(VentaResumenResponse::from).toList(),
                pagos.stream().map(PagoResponse::from).toList(),
                salidas.stream().map(MovimientoCajaResponse::from).toList()
        );
    }

    private BigDecimal sumar(List<Pago> pagos, MetodoPago metodo) {
        return pagos.stream()
                .filter(p -> p.getMetodo() == metodo)
                .map(Pago::getMonto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}