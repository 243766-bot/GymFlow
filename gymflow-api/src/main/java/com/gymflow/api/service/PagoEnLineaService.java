package com.gymflow.api.service;

import com.gymflow.api.client.MercadoPagoClient;
import com.gymflow.api.dto.CheckoutResponse;
import com.gymflow.api.dto.MembresiaResponse;
import com.gymflow.api.dto.PagoEstadoResponse;
import com.gymflow.api.dto.PagoResponse;
import com.gymflow.api.entity.EstadoPago;
import com.gymflow.api.entity.Membresia;
import com.gymflow.api.entity.MetodoPago;
import com.gymflow.api.entity.Pago;
import com.gymflow.api.entity.Plan;
import com.gymflow.api.entity.Usuario;
import com.gymflow.api.exception.NotFoundException;
import com.gymflow.api.repository.PagoRepository;
import com.gymflow.api.repository.PlanRepository;
import com.gymflow.api.repository.UsuarioRepository;
import com.gymflow.api.util.Fechas;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class PagoEnLineaService {

    private static final Logger log = LoggerFactory.getLogger(PagoEnLineaService.class);

    private final PagoRepository pagoRepository;
    private final UsuarioRepository usuarioRepository;
    private final PlanRepository planRepository;
    private final MembresiaService membresiaService;
    private final MercadoPagoClient mercadoPago;
    private final String urlFrontend;

    public PagoEnLineaService(PagoRepository pagoRepository,
                              UsuarioRepository usuarioRepository,
                              PlanRepository planRepository,
                              MembresiaService membresiaService,
                              MercadoPagoClient mercadoPago,
                              @Value("${app.frontend-url}") String urlFrontend) {
        this.pagoRepository = pagoRepository;
        this.usuarioRepository = usuarioRepository;
        this.planRepository = planRepository;
        this.membresiaService = membresiaService;
        this.mercadoPago = mercadoPago;
        this.urlFrontend = urlFrontend;
    }

    // ---------- 1. El cliente pide pagar un plan ----------

    // Si Mercado Pago falla, la transacción se deshace y no queda ningún pago pendiente suelto
    @Transactional
    public CheckoutResponse crearCobro(String emailCliente, Long planId) {
        Usuario usuario = usuarioRepository.findByEmail(emailCliente)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));
        Plan plan = planRepository.findById(planId)
                .orElseThrow(() -> new NotFoundException("Plan no encontrado"));

        Pago pago = new Pago();
        pago.setUsuario(usuario);
        pago.setPlan(plan);
        pago.setConcepto("Membresía " + plan.getNombre());
        pago.setMonto(plan.getPrecio());
        pago.setMetodo(MetodoPago.MERCADO_PAGO);
        pago.setEstado(EstadoPago.PENDIENTE);
        pago.setFecha(Fechas.ahora());
        pago = pagoRepository.save(pago); // aquí se le asigna su número

        // El número del pago viaja como "referencia externa" para reconocerlo después
        MercadoPagoClient.Preferencia preferencia = mercadoPago.crearPreferencia(
                pago.getConcepto(),
                plan.getPrecio(),
                String.valueOf(pago.getId()),
                urlFrontend + "/pago/resultado");

        return new CheckoutResponse(pago.getId(), preferencia.id(), preferencia.urlPago());
    }

    // ---------- 2. Consultar el estado (y activar la membresía si ya se pagó) ----------

    @Transactional
    public PagoEstadoResponse consultarEstado(Long pagoId, String email, boolean esAdmin) {
        // Bloquea el pago: dos consultas a la vez no pueden activar la membresía dos veces
        Pago pago = pagoRepository.findByIdForUpdate(pagoId)
                .orElseThrow(() -> new NotFoundException("Pago no encontrado"));

        // Un cliente solo puede ver sus propios pagos (para los demás, el pago "no existe")
        if (!esAdmin && !pago.getUsuario().getEmail().equalsIgnoreCase(email)) {
            throw new NotFoundException("Pago no encontrado");
        }

        // Los pagos en efectivo ya nacen aprobados; los de Mercado Pago se confirman con ellos
        if (pago.getMetodo() == MetodoPago.MERCADO_PAGO && pago.getEstado() != EstadoPago.APROBADO) {
            sincronizar(pago);
        }

        MembresiaResponse membresia = (pago.getMembresia() == null)
                ? null
                : MembresiaResponse.from(pago.getMembresia());

        return new PagoEstadoResponse(PagoResponse.from(pago), membresia);
    }

    // Le pregunta a Mercado Pago cómo va el pago y actualiza el nuestro
    private void sincronizar(Pago pago) {
        Optional<MercadoPagoClient.PagoMp> encontrado =
                mercadoPago.buscarPagoPorReferencia(String.valueOf(pago.getId()));

        if (encontrado.isEmpty()) {
            return; // el cliente todavía no intentó pagar: sigue PENDIENTE
        }

        MercadoPagoClient.PagoMp datos = encontrado.get();
        pago.setIdExternoMercadoPago(datos.id());
        EstadoPago nuevoEstado = traducir(datos.estado());

        if (nuevoEstado == EstadoPago.APROBADO) {
            // Seguridad: lo pagado debe cubrir el precio del plan
            if (datos.monto() == null || datos.monto().compareTo(pago.getMonto()) < 0) {
                log.warn("El pago {} de Mercado Pago no cubre el monto esperado ({})",
                        datos.id(), pago.getMonto());
                pagoRepository.save(pago);
                return;
            }

            Membresia membresia = membresiaService.aplicarPlanPagado(pago.getUsuario(), pago.getPlan());
            pago.setMembresia(membresia);
            pago.setFecha(Fechas.ahora()); // el corte cuenta el día en que se aprobó
        }

        pago.setEstado(nuevoEstado);
        pagoRepository.save(pago);
    }

    private EstadoPago traducir(String estadoMercadoPago) {
        return switch (estadoMercadoPago) {
            case "approved" -> EstadoPago.APROBADO;
            case "rejected", "cancelled" -> EstadoPago.RECHAZADO;
            default -> EstadoPago.PENDIENTE; // pending, in_process, authorized...
        };
    }
}