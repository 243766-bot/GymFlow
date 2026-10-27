package com.gymflow.api.service;

import com.gymflow.api.exception.BadRequestException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class FaceChallengeService {

    private static final int MAX_DESAFIOS_ACTIVOS = 5000;

    // desafío -> momento en que expira
    private final Map<String, Instant> desafios = new ConcurrentHashMap<>();
    private final SecureRandom random = new SecureRandom();
    private final long ttlSegundos;

    public FaceChallengeService(@Value("${face.challenge-ttl-seconds:60}") long ttlSegundos) {
        this.ttlSegundos = ttlSegundos;
    }

    public long getTtlSegundos() {
        return ttlSegundos;
    }

    public String crear() {
        limpiarExpirados();
        if (desafios.size() >= MAX_DESAFIOS_ACTIVOS) {
            throw new BadRequestException("Demasiadas solicitudes. Intenta de nuevo en un momento.");
        }

        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        String desafio = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);

        desafios.put(desafio, Instant.now().plusSeconds(ttlSegundos));
        return desafio;
    }

    // Un desafío solo sirve una vez: al consumirlo se borra
    public boolean consumir(String desafio) {
        Instant expira = desafios.remove(desafio);
        return expira != null && expira.isAfter(Instant.now());
    }

    private void limpiarExpirados() {
        Instant ahora = Instant.now();
        desafios.entrySet().removeIf(e -> e.getValue().isBefore(ahora));
    }
}