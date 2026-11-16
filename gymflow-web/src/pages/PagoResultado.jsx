import { useEffect, useState } from "react";
import { Link, Navigate, useLocation, useSearchParams } from "react-router-dom";
import { api } from "../api";
import { useAuth } from "../AuthContext";
import { formatearFecha, formatearMoneda } from "../formato";

const CLAVE_PAGO = "gymflow_pago_pendiente";
const MAX_CONSULTAS = 10; // 10 consultas, una cada 3 segundos
const ESPERA_MS = 3000;

export default function PagoResultado() {
  const { token, usuario, cargando } = useAuth();
  const location = useLocation();
  const [params] = useSearchParams();

  // Mercado Pago regresa al cliente con nuestro número de pago en "external_reference".
  // Si no viene, usamos el que guardamos antes de mandarlo a pagar.
  const pagoId = params.get("external_reference") || sessionStorage.getItem(CLAVE_PAGO);

  const [resultado, setResultado] = useState(null);
  const [error, setError] = useState("");
  const [agotado, setAgotado] = useState(false); // se acabaron los reintentos y sigue pendiente
  const [ronda, setRonda] = useState(0); // sube cuando el cliente presiona "Revisar de nuevo"

  const sesionLista = Boolean(token && usuario);

  useEffect(() => {
    if (!sesionLista || !pagoId) return;

    let cancelado = false;
    let temporizador;

    async function consultar(intento) {
      try {
        // El estado lo confirma el backend con Mercado Pago, no lo que diga la dirección
        const r = await api(`/payments/${pagoId}/status`, { token });
        if (cancelado) return;

        setResultado(r);
        setError("");

        if (r.pago.estado === "PENDIENTE") {
          if (intento < MAX_CONSULTAS) {
            temporizador = setTimeout(() => consultar(intento + 1), ESPERA_MS);
          } else {
            setAgotado(true);
          }
        } else {
          sessionStorage.removeItem(CLAVE_PAGO);
        }
      } catch (err) {
        if (!cancelado) setError(err.message);
      }
    }

    consultar(1);

    return () => {
      cancelado = true;
      clearTimeout(temporizador);
    };
  }, [sesionLista, pagoId, token, ronda]);

  function revisarDeNuevo() {
    setAgotado(false);
    setRonda((r) => r + 1);
  }

  if (cargando) {
    return <p className="contenedor">Cargando...</p>;
  }

  // Sin sesión: a iniciar sesión, y después se regresa a esta misma pantalla
  if (!sesionLista) {
    return <Navigate to="/login" replace state={{ from: location }} />;
  }

  if (!pagoId) {
    return (
      <div className="contenedor">
        <h1>Resultado del pago</h1>
        <p>No encontramos el pago que buscas.</p>
        <Link to="/perfil">Ir a mi perfil</Link>
      </div>
    );
  }

  if (error) {
    return (
      <div className="contenedor">
        <h1>Resultado del pago</h1>
        <p className="error">{error}</p>
        <Link to="/perfil">Ir a mi perfil</Link>
      </div>
    );
  }

  if (!resultado) {
    return (
      <div className="contenedor">
        <h1>Resultado del pago</h1>
        <p>Confirmando tu pago con Mercado Pago...</p>
      </div>
    );
  }

  const { pago, membresia } = resultado;

  return (
    <div className="contenedor">
      <h1>Resultado del pago</h1>

      {pago.estado === "APROBADO" && (
        <div className="tarjeta ok">
          <h2>¡Pago aprobado!</h2>
          <p>
            {pago.concepto} · {formatearMoneda(pago.monto)}
          </p>
          {membresia && (
            <>
              <p>
                Tu membresía <strong>{membresia.plan.nombre}</strong> está{" "}
                <strong>{membresia.estado}</strong>.
              </p>
              <p>Vence el {formatearFecha(membresia.fechaVencimiento)}.</p>
            </>
          )}
          <Link to="/perfil">Ir a mi perfil</Link>
        </div>
      )}

      {pago.estado === "RECHAZADO" && (
        <div className="tarjeta error">
          <h2>El pago no se completó</h2>
          <p>Mercado Pago no aprobó el pago, así que no se cobró nada ni cambió tu membresía.</p>
          <Link to="/perfil">Intentar de nuevo desde mi perfil</Link>
        </div>
      )}

      {pago.estado === "PENDIENTE" && (
        <div className="tarjeta">
          {agotado ? (
            <>
              <h2>Tu pago sigue en proceso</h2>
              <p>
                Mercado Pago todavía no confirma el pago. Si ya pagaste, puede tardar unos minutos. Puedes
                revisar de nuevo o volver más tarde: tu membresía se activa sola en cuanto se apruebe.
              </p>
              <p>
                <button onClick={revisarDeNuevo}>Revisar de nuevo</button>
              </p>
              <Link to="/perfil">Ir a mi perfil</Link>
            </>
          ) : (
            <>
              <h2>Confirmando tu pago...</h2>
              <p>Esto toma unos segundos. No cierres esta página.</p>
            </>
          )}
        </div>
      )}
    </div>
  );
}