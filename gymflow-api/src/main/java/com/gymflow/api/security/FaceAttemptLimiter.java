package com.gymflow.api.security;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class FaceAttemptLimiter {

    private static final int MAX_FALLOS = 5;
    private static final Duration VENTANA = Duration.ofMinutes(5);

    // clave del dispositivo -> momentos de los últimos fallos
    private final Map<String, Deque<Instant>> fallos = new ConcurrentHashMap<>();

    public void verificarPermitido(String clave) {
        Deque<Instant> cola = fallos.get(clave);
        if (cola == null) {
            return;
        }
        synchronized (cola) {
            limpiar(cola);
            if (cola.size() >= MAX_FALLOS) {
                throw new AccessDeniedException(
                        "Demasiados intentos fallidos. Espera unos minutos o usa tu correo y contraseña.");
            }
        }
    }

    public void registrarFallo(String clave) {
        Deque<Instant> cola = fallos.computeIfAbsent(clave, k -> new ArrayDeque<>());
        synchronized (cola) {
            limpiar(cola);
            cola.addLast(Instant.now());
        }
    }

    public void reiniciar(String clave) {
        fallos.remove(clave);
    }

    private void limpiar(Deque<Instant> cola) {
        Instant limite = Instant.now().minus(VENTANA);
        while (!cola.isEmpty() && cola.peekFirst().isBefore(limite)) {
            cola.removeFirst();
        }
    }
}