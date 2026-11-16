package com.gymflow.api.exception;

// Un servicio externo (como Mercado Pago) falló o no está configurado
public class ExternalServiceException extends RuntimeException {
    public ExternalServiceException(String message) {
        super(message);
    }
}