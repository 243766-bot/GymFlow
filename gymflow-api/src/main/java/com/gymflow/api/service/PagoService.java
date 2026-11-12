package com.gymflow.api.service;

import com.gymflow.api.dto.PagoResponse;
import com.gymflow.api.entity.EstadoPago;
import com.gymflow.api.entity.Membresia;
import com.gymflow.api.entity.MetodoPago;
import com.gymflow.api.entity.Pago;
import com.gymflow.api.entity.Usuario;
import com.gymflow.api.exception.NotFoundException;
import com.gymflow.api.repository.PagoRepository;
import com.gymflow.api.repository.UsuarioRepository;
import com.gymflow.api.util.Fechas;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PagoService {

    private final PagoRepository pagoRepository;
    private final UsuarioRepository usuarioRepository;

    public PagoService(PagoRepository pagoRepository, UsuarioRepository usuarioRepository) {
        this.pagoRepository = pagoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    // Lo llama MembresiaService dentro de su misma transacción:
    // si algo falla, no queda ni la membresía ni el pago
    @Transactional
    public Pago registrarEfectivo(Membresia membresia) {
        Pago pago = new Pago();
        pago.setUsuario(membresia.getUsuario());
        pago.setMembresia(membresia);
        pago.setConcepto("Membresía " + membresia.getPlan().getNombre());
        pago.setMonto(membresia.getPlan().getPrecio());
        pago.setMetodo(MetodoPago.EFECTIVO);
        pago.setEstado(EstadoPago.APROBADO);
        pago.setFecha(Fechas.ahora());
        return pagoRepository.save(pago);
    }

    // Los pagos del cliente que envía el token (el más reciente primero)
    @Transactional(readOnly = true)
    public List<PagoResponse> misPagos(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));

        return pagoRepository.findByUsuarioIdOrderByFechaDesc(usuario.getId()).stream()
                .map(PagoResponse::from)
                .toList();
    }
}