package com.gymflow.api.service;

import com.gymflow.api.dto.PlanRequest;
import com.gymflow.api.dto.PlanResponse;
import com.gymflow.api.entity.Plan;
import com.gymflow.api.exception.BadRequestException;
import com.gymflow.api.exception.NotFoundException;
import com.gymflow.api.repository.MembresiaRepository;
import com.gymflow.api.repository.PlanRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PlanService {

    private final PlanRepository planRepository;
    private final MembresiaRepository membresiaRepository;

    public PlanService(PlanRepository planRepository, MembresiaRepository membresiaRepository) {
        this.planRepository = planRepository;
        this.membresiaRepository = membresiaRepository;
    }

    @Transactional(readOnly = true)
    public List<PlanResponse> listar() {
        return planRepository.findAll(Sort.by("precio")).stream()
                .map(PlanResponse::from)
                .toList();
    }

    @Transactional
    public PlanResponse crear(PlanRequest request) {
        String nombre = request.nombre().trim();
        if (planRepository.existsByNombreIgnoreCase(nombre)) {
            throw new BadRequestException("Ya existe un plan con ese nombre");
        }

        Plan plan = new Plan();
        aplicar(plan, nombre, request);
        return PlanResponse.from(planRepository.save(plan));
    }

    @Transactional
    public PlanResponse actualizar(Long id, PlanRequest request) {
        Plan plan = planRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Plan no encontrado"));

        String nombre = request.nombre().trim();
        if (planRepository.existsByNombreIgnoreCaseAndIdNot(nombre, id)) {
            throw new BadRequestException("Ya existe un plan con ese nombre");
        }

        aplicar(plan, nombre, request);
        return PlanResponse.from(planRepository.save(plan));
    }

    @Transactional
    public void eliminar(Long id) {
        if (!planRepository.existsById(id)) {
            throw new NotFoundException("Plan no encontrado");
        }
        if (membresiaRepository.existsByPlanId(id)) {
            throw new BadRequestException("No se puede eliminar: hay membresías que usan este plan");
        }
        planRepository.deleteById(id);
    }

    private void aplicar(Plan plan, String nombre, PlanRequest request) {
        plan.setNombre(nombre);
        plan.setDuracionDias(request.duracionDias());
        plan.setPrecio(request.precio());
    }
}