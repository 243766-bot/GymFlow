package com.gymflow.api.client;

import com.gymflow.api.exception.ExternalServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.math.BigDecimal;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class MercadoPagoClient {

    private static final Logger log = LoggerFactory.getLogger(MercadoPagoClient.class);

    // Lo que necesitamos de la preferencia que crea Mercado Pago
    public record Preferencia(String id, String urlPago) {
    }

    // Lo que necesitamos de un pago: su ID, su estado (approved, pending, rejected...) y el monto
    public record PagoMp(String id, String estado, BigDecimal monto) {
    }

    private final RestClient restClient;
    private final String accessToken;

    public MercadoPagoClient(@Value("${mercadopago.access-token:}") String accessToken) {
        this.accessToken = (accessToken == null) ? "" : accessToken.trim();

        // Tiempos máximos de espera, para que la API no se quede colgada si Mercado Pago tarda
        HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
        JdkClientHttpRequestFactory fabrica = new JdkClientHttpRequestFactory(http);
        fabrica.setReadTimeout(Duration.ofSeconds(15));

        this.restClient = RestClient.builder()
                .baseUrl("https://api.mercadopago.com")
                .requestFactory(fabrica)
                .build();
    }

    // Crea la preferencia de pago y devuelve el enlace al que se manda al cliente
    public Preferencia crearPreferencia(String titulo, BigDecimal precio, String referencia, String urlRegreso) {
        verificarToken();

        Map<String, Object> articulo = Map.of(
                "title", titulo,
                "quantity", 1,
                "currency_id", "MXN",
                "unit_price", precio);

        Map<String, Object> cuerpo = Map.of(
                "items", List.of(articulo),
                "external_reference", referencia,
                "back_urls", Map.of(
                        "success", urlRegreso,
                        "failure", urlRegreso,
                        "pending", urlRegreso));

        try {
            Map<?, ?> respuesta = restClient.post()
                    .uri("/checkout/preferences")
                    .header("Authorization", "Bearer " + accessToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(cuerpo)
                    .retrieve()
                    .body(Map.class);

            if (respuesta == null || respuesta.get("init_point") == null) {
                throw new ExternalServiceException("Mercado Pago no devolvió el enlace de pago");
            }
            return new Preferencia(
                    String.valueOf(respuesta.get("id")),
                    String.valueOf(respuesta.get("init_point")));

        } catch (RestClientResponseException e) {
            // Mercado Pago respondió con un error: lo dejamos en el log para poder revisarlo
            log.warn("Mercado Pago rechazó la preferencia: {} {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new ExternalServiceException("Mercado Pago rechazó la solicitud de cobro");
        } catch (RestClientException e) {
            log.warn("No se pudo contactar a Mercado Pago", e);
            throw new ExternalServiceException("No se pudo comunicar con Mercado Pago");
        }
    }

    // Busca el pago de una referencia. Si hay uno aprobado se queda con ese; si no, con el más reciente.
    public Optional<PagoMp> buscarPagoPorReferencia(String referencia) {
        verificarToken();

        try {
            Map<?, ?> respuesta = restClient.get()
                    .uri(uri -> uri.path("/v1/payments/search")
                            .queryParam("external_reference", referencia)
                            .queryParam("sort", "date_created")
                            .queryParam("criteria", "desc")
                            .build())
                    .header("Authorization", "Bearer " + accessToken)
                    .retrieve()
                    .body(Map.class);

            Object resultados = (respuesta == null) ? null : respuesta.get("results");
            if (!(resultados instanceof List<?> lista) || lista.isEmpty()) {
                return Optional.empty(); // el cliente todavía no intentó pagar
            }

            PagoMp elegido = null;
            for (Object elemento : lista) {
                if (!(elemento instanceof Map<?, ?> datos)) {
                    continue;
                }
                PagoMp actual = convertir(datos);
                if (elegido == null) {
                    elegido = actual; // el más reciente
                }
                if ("approved".equals(actual.estado())) {
                    elegido = actual;
                    break;
                }
            }
            return Optional.ofNullable(elegido);

        } catch (RestClientResponseException e) {
            log.warn("Mercado Pago rechazó la consulta: {} {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new ExternalServiceException("Mercado Pago rechazó la consulta del pago");
        } catch (RestClientException e) {
            log.warn("No se pudo contactar a Mercado Pago", e);
            throw new ExternalServiceException("No se pudo comunicar con Mercado Pago");
        }
    }

    private PagoMp convertir(Map<?, ?> datos) {
        Object monto = datos.get("transaction_amount");
        return new PagoMp(
                String.valueOf(datos.get("id")),
                String.valueOf(datos.get("status")),
                (monto == null) ? null : new BigDecimal(String.valueOf(monto)));
    }

    private void verificarToken() {
        if (accessToken.isBlank()) {
            throw new ExternalServiceException(
                    "El cobro en línea aún no está configurado: falta el token de Mercado Pago");
        }
    }
}