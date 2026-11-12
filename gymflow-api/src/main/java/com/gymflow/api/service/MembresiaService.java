package com.gymflow.api.service;

import com.gymflow.api.dto.MembresiaRequest;
import com.gymflow.api.dto.MembresiaResponse;
import com.gymflow.api.dto.MembresiaUpdateRequest;
import com.gymflow.api.dto.RenovarRequest;
import com.gymflow.api.entity.EstadoMembresia;
import com.gymflow.api.entity.Membresia;
import com.gymflow.api.entity.Plan;
import com.gymflow.api.entity.Usuario;
import com.gymflow.api.exception.BadRequestException;
import com.gymflow.api.exception.NotFoundException;
import com.gymflow.api.repository.MembresiaRepository;
import com.gymflow.api.repository.PlanRepository;
import com.gymflow.api.repository.UsuarioRepository;
import com.gymflow.api.util.Fechas;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class MembresiaService {

    private final MembresiaRepository membresiaRepository;
    private final PlanRepository planRepository;
    private final UsuarioRepository usuarioRepository;
    private final PagoService pagoService;

    public MembresiaService(MembresiaRepository membresiaRepository,
                            PlanRepository planRepository,
                            UsuarioRepository usuarioRepository,
                            PagoService pagoService) {
        this.membresiaRepository = membresiaRepository;
        this.planRepository = planRepository;
        this.usuarioRepository = usuarioRepository;
        this.pagoService = pagoService;
    }

    // ---------- Consultas ----------

    @Transactional(readOnly = true)
    public List<MembresiaResponse> listar(Long usuarioId) {
        List<Membresia> lista = (usuarioId == null)
                ? membresiaRepository.findAllByOrderByIdDesc()
                : membresiaRepository.findByUsuarioIdOrderByIdDesc(usuarioId);
        return lista.stream().map(MembresiaResponse::from).toList();
    }

    // La membresía del cliente que envía el token
    @Transactional(readOnly = true)
    public MembresiaResponse miMembresia(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));

        return actualDeUsuario(usuario.getId())
                .orElseThrow(() -> new NotFoundException("Aún no tienes una membresía"));
    }

    // La usa también el login facial de la caja
    @Transactional(readOnly = true)
    public Optional<MembresiaResponse> actualDeUsuario(Long usuarioId) {
        return membresiaRepository.findFirstByUsuarioIdOrderByFechaVencimientoDesc(usuarioId)
                .map(MembresiaResponse::from);
    }

    // ---------- Cambios (solo administrador) ----------

    @Transactional
    public MembresiaResponse crear(MembresiaRequest request) {
        Usuario usuario = usuarioRepository.findById(request.usuarioId())
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));
        Plan plan = buscarPlan(request.planId());

        LocalDate hoy = Fechas.hoy();
        if (membresiaRepository.existsByUsuarioIdAndEstadoAndFechaVencimientoGreaterThanEqual(
                usuario.getId(), EstadoMembresia.ACTIVA, hoy)) {
            throw new BadRequestException("El cliente ya tiene una membresía vigente. Usa la renovación.");
        }

        LocalDate inicio = (request.fechaInicio() != null) ? request.fechaInicio() : hoy;

        Membresia membresia = new Membresia();
        membresia.setUsuario(usuario);
        membresia.setPlan(plan);
        membresia.setFechaInicio(inicio);
        membresia.setFechaVencimiento(inicio.plusDays(plan.getDuracionDias()));
        membresia.setEstado(EstadoMembresia.ACTIVA);

        Membresia guardada = membresiaRepository.save(membresia);

        // Si se pidió, se registra el cobro en efectivo por el precio del plan
        if (Boolean.TRUE.equals(request.cobrarEnEfectivo())) {
            pagoService.registrarEfectivo(guardada);
        }

        return MembresiaResponse.from(guardada);
    }

    @Transactional
    public MembresiaResponse actualizar(Long id, MembresiaUpdateRequest request) {
        Membresia membresia = buscar(id);

        // Solo se cambia lo que venga en la petición
        if (request.planId() != null) {
            membresia.setPlan(buscarPlan(request.planId()));
        }
        if (request.fechaInicio() != null) {
            membresia.setFechaInicio(request.fechaInicio());
        }
        if (request.fechaVencimiento() != null) {
            membresia.setFechaVencimiento(request.fechaVencimiento());
        }
        if (request.estado() != null) {
            membresia.setEstado(request.estado());
        }

        if (membresia.getFechaVencimiento().isBefore(membresia.getFechaInicio())) {
            throw new BadRequestException("La fecha de vencimiento no puede ser anterior a la de inicio");
        }

        return MembresiaResponse.from(membresiaRepository.save(membresia));
    }

    @Transactional
    public MembresiaResponse renovar(Long id, RenovarRequest request) {
        Membresia membresia = buscar(id);
        LocalDate hoy = Fechas.hoy();

        // Si se manda un plan, la renovación cambia a ese plan
        if (request != null && request.planId() != null) {
            membresia.setPlan(buscarPlan(request.planId()));
        }

        int dias = membresia.getPlan().getDuracionDias();
        boolean vigente = membresia.getEstado() == EstadoMembresia.ACTIVA
                && !membresia.getFechaVencimiento().isBefore(hoy);

        if (vigente) {
            // Aún vigente: los días nuevos se suman al vencimiento actual
            membresia.setFechaVencimiento(membresia.getFechaVencimiento().plusDays(dias));
        } else {
            // Vencida o cancelada: empieza de nuevo desde hoy
            membresia.setFechaInicio(hoy);
            membresia.setFechaVencimiento(hoy.plusDays(dias));
            membresia.setEstado(EstadoMembresia.ACTIVA);
        }

        Membresia guardada = membresiaRepository.save(membresia);

        // Si se pidió, se registra el cobro en efectivo por el precio del plan elegido
        if (request != null && Boolean.TRUE.equals(request.cobrarEnEfectivo())) {
            pagoService.registrarEfectivo(guardada);
        }

        return MembresiaResponse.from(guardada);
    }

    // ---------- Utilidades ----------

    private Membresia buscar(Long id) {
        return membresiaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Membresía no encontrada"));
    }

    private Plan buscarPlan(Long id) {
        return planRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Plan no encontrado"));
    }
}