package com.gymflow.api.service;

import com.gymflow.api.entity.EstadoPago;
import com.gymflow.api.entity.MetodoPago;
import com.gymflow.api.entity.Pago;
import com.gymflow.api.exception.ExternalServiceException;
import com.gymflow.api.repository.PagoRepository;
import com.gymflow.api.util.Fechas;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class SincronizadorDePagos {

    private static final Logger log = LoggerFactory.getLogger(SincronizadorDePagos.class);

    private final PagoRepository pagoRepository;
    private final PagoEnLineaService pagoEnLineaService;

    public SincronizadorDePagos(PagoRepository pagoRepository, PagoEnLineaService pagoEnLineaService) {
        this.pagoRepository = pagoRepository;
        this.pagoEnLineaService = pagoEnLineaService;
    }

    // Cada minuto revisa los pagos de Mercado Pago de las últimas 24 horas que siguen pendientes.
    // Si ya se aprobaron, activa la membresía aunque el cliente nunca haya vuelto al sitio.
    @Scheduled(initialDelay = 30_000, fixedDelay = 60_000)
    public void revisarPendientes() {
        LocalDateTime desde = Fechas.ahora().minusHours(24);
        List<Pago> pendientes = pagoRepository.findByMetodoAndEstadoAndFechaGreaterThanEqual(
                MetodoPago.MERCADO_PAGO, EstadoPago.PENDIENTE, desde);

        for (Pago pago : pendientes) {
            try {
                // Se llama como administrador: cada pago se revisa en su propia transacción
                pagoEnLineaService.consultarEstado(pago.getId(), "", true);
            } catch (ExternalServiceException e) {
                // Sin token o sin conexión con Mercado Pago: se vuelve a intentar en la siguiente vuelta
                log.debug("No se pudo revisar pagos pendientes: {}", e.getMessage());
                return;
            } catch (Exception e) {
                log.warn("No se pudo revisar el pago {}", pago.getId(), e);
            }
        }
    }
}