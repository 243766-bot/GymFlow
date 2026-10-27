package com.gymflow.api.service;

import com.gymflow.api.dto.AuthResponse;
import com.gymflow.api.dto.FaceLoginRequest;
import com.gymflow.api.dto.FaceRegisterRequest;
import com.gymflow.api.dto.UsuarioResponse;
import com.gymflow.api.entity.PlantillaFacial;
import com.gymflow.api.entity.Usuario;
import com.gymflow.api.exception.BadRequestException;
import com.gymflow.api.exception.NotFoundException;
import com.gymflow.api.exception.UnauthorizedException;
import com.gymflow.api.repository.PlantillaFacialRepository;
import com.gymflow.api.repository.UsuarioRepository;
import com.gymflow.api.security.FaceAttemptLimiter;
import com.gymflow.api.security.JwtService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FaceAuthService {

    private static final int TAMANO_DESCRIPTOR = 128;

    private final PlantillaFacialRepository plantillaRepository;
    private final UsuarioRepository usuarioRepository;
    private final FaceChallengeService challengeService;
    private final FaceAttemptLimiter limiter;
    private final JwtService jwtService;
    private final double umbral;
    private final long tokenExpirationMs;

    public FaceAuthService(PlantillaFacialRepository plantillaRepository,
                           UsuarioRepository usuarioRepository,
                           FaceChallengeService challengeService,
                           FaceAttemptLimiter limiter,
                           JwtService jwtService,
                           @Value("${face.threshold:0.5}") double umbral,
                           @Value("${face.token-expiration-ms:900000}") long tokenExpirationMs) {
        this.plantillaRepository = plantillaRepository;
        this.usuarioRepository = usuarioRepository;
        this.challengeService = challengeService;
        this.limiter = limiter;
        this.jwtService = jwtService;
        this.umbral = umbral;
        this.tokenExpirationMs = tokenExpirationMs;
    }

    // ---------- Registro del rostro (lo hace el administrador) ----------

    @Transactional
    public void registrar(Long usuarioId, FaceRegisterRequest request) {
        if (!Boolean.TRUE.equals(request.consentimiento())) {
            throw new BadRequestException("Se necesita el consentimiento del cliente para guardar su rostro");
        }
        validarValores(request.descriptor());

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));

        // Si ya tenía rostro registrado, se reemplaza
        PlantillaFacial plantilla = plantillaRepository.findByUsuarioId(usuarioId)
                .orElseGet(PlantillaFacial::new);

        plantilla.setUsuario(usuario);
        plantilla.setDescriptor(serializar(request.descriptor()));
        plantilla.setConsentimiento(true);
        plantilla.setFechaRegistro(LocalDateTime.now());
        plantillaRepository.save(plantilla);
    }

    @Transactional
    public void eliminar(Long usuarioId) {
        if (!usuarioRepository.existsById(usuarioId)) {
            throw new NotFoundException("Usuario no encontrado");
        }
        plantillaRepository.deleteByUsuarioId(usuarioId);
    }

    // ---------- Login facial (lo usa la pantalla de la caja) ----------

    @Transactional(readOnly = true)
    public AuthResponse login(FaceLoginRequest request, String claveDispositivo) {
        limiter.verificarPermitido(claveDispositivo);

        if (!challengeService.consumir(request.challenge())) {
            limiter.registrarFallo(claveDispositivo);
            throw new BadRequestException("El desafío no es válido o ya expiró. Pide uno nuevo.");
        }
        validarValores(request.descriptor());

        double[] muestra = aArreglo(request.descriptor());

        PlantillaFacial mejor = null;
        double mejorDistancia = Double.MAX_VALUE;

        for (PlantillaFacial p : plantillaRepository.findByUsuarioActivoTrue()) {
            double[] guardado = parsear(p.getDescriptor());
            if (guardado == null) {
                continue; // plantilla dañada: se ignora
            }
            double d = distancia(muestra, guardado);
            if (d < mejorDistancia) {
                mejorDistancia = d;
                mejor = p;
            }
        }

        if (mejor == null || mejorDistancia > umbral) {
            limiter.registrarFallo(claveDispositivo);
            throw new UnauthorizedException(
                    "No se reconoció el rostro. Intenta de nuevo o entra con tu correo y contraseña.");
        }

        limiter.reiniciar(claveDispositivo);

        Usuario usuario = mejor.getUsuario();
        String token = jwtService.generateToken(usuario, tokenExpirationMs);
        return new AuthResponse(token, "Bearer", UsuarioResponse.from(usuario));
    }

    // ---------- Utilidades ----------

    private void validarValores(List<Double> descriptor) {
        for (Double v : descriptor) {
            if (v == null || !Double.isFinite(v)) {
                throw new BadRequestException("El descriptor contiene valores no válidos");
            }
        }
    }

    private String serializar(List<Double> descriptor) {
        return descriptor.stream().map(String::valueOf).collect(Collectors.joining(","));
    }

    private double[] aArreglo(List<Double> descriptor) {
        double[] arreglo = new double[descriptor.size()];
        for (int i = 0; i < arreglo.length; i++) {
            arreglo[i] = descriptor.get(i);
        }
        return arreglo;
    }

    private double[] parsear(String texto) {
        try {
            String[] partes = texto.split(",");
            if (partes.length != TAMANO_DESCRIPTOR) {
                return null;
            }
            double[] arreglo = new double[TAMANO_DESCRIPTOR];
            for (int i = 0; i < TAMANO_DESCRIPTOR; i++) {
                arreglo[i] = Double.parseDouble(partes[i]);
            }
            return arreglo;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    // Distancia euclidiana: mientras más pequeña, más parecidos son los rostros
    private double distancia(double[] a, double[] b) {
        double suma = 0;
        for (int i = 0; i < a.length; i++) {
            double diff = a[i] - b[i];
            suma += diff * diff;
        }
        return Math.sqrt(suma);
    }
}