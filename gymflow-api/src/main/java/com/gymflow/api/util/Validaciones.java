package com.gymflow.api.util;

/**
 * Reglas de validacion compartidas por los DTO.
 */
public final class Validaciones {

    private Validaciones() {}

    // Terminaciones aceptadas: de dos niveles (com.mx) o de uno (com, mx, org...)
    private static final String TERMINACIONES =
            "com\\.mx|org\\.mx|net\\.mx|edu\\.mx|gob\\.mx|com\\.ar|com\\.co|com\\.br|com\\.pe|com\\.es"
            + "|co\\.uk|org\\.uk|com\\.au"
            + "|com|net|org|edu|gob|gov|info|biz|io|co|mx|us|es|ar|cl|pe|br|ca|uk|de|fr|it|pt";

    /**
     * nombre@dominio.terminacion, con UN solo nombre de dominio antes de una terminacion real.
     * Rechaza cosas como usuario@gill.com.mx.fr.de o usuario@gmail.com.mx.tv.
     */
    public static final String EMAIL =
            "^(?i)(?=[^@]{1,64}@)[a-z0-9]+([._%+-][a-z0-9]+)*"
            + "@(?!(?:com|net|org|edu|gob|gov|co)\\.)[a-z0-9]([a-z0-9-]{0,61}[a-z0-9])?\\.(" + TERMINACIONES + ")$";

    public static final String EMAIL_MENSAJE =
            "El correo debe tener un dominio v\u00e1lido, por ejemplo nombre@gmail.com o nombre@dominio.com.mx";
}
