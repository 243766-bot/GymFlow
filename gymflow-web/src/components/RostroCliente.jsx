import { useEffect, useState } from "react";
import FaceCamera from "./FaceCamera";
import { api } from "../api";

function formatearFechaHora(iso) {
  if (!iso) return "";
  return new Date(iso).toLocaleString("es-MX", { dateStyle: "long", timeStyle: "short" });
}

export default function RostroCliente({ cliente, token, onCambio }) {
  const [consentimiento, setConsentimiento] = useState(false);
  const [retomando, setRetomando] = useState(false); // true cuando el admin pidió "Tomar otra foto"
  const [info, setInfo] = useState(null);
  const [version, setVersion] = useState(0);
  const [mensaje, setMensaje] = useState("");
  const [error, setError] = useState("");

  // La cámara solo se ve si no hay rostro registrado o si se pidió tomar otra foto
  const mostrarCamara = retomando || !cliente.rostroRegistrado;
  const infoActual = cliente.rostroRegistrado ? info : null;

  // Si el cliente tiene rostro, pedimos su foto y la fecha de registro
  useEffect(() => {
    if (!cliente.rostroRegistrado) return;

    let cancelado = false;
    api(`/users/${cliente.id}/face`, { token })
      .then((i) => {
        if (!cancelado) setInfo(i);
      })
      .catch((err) => {
        if (!cancelado) setError(err.message);
      });

    return () => {
      cancelado = true;
    };
  }, [cliente.id, cliente.rostroRegistrado, token, version]);

  // Se ejecuta antes de capturar: valida el consentimiento
  function validar() {
    setMensaje("");
    setError("");
    if (!consentimiento) throw new Error("Marca el consentimiento del cliente");
  }

  async function guardarRostro(descriptor, _contexto, foto) {
    const r = await api(`/users/${cliente.id}/face`, {
      method: "POST",
      token,
      body: { descriptor, consentimiento, foto },
    });
    setMensaje(r.mensaje);
    setConsentimiento(false);
    await onCambio(); // recarga la lista de clientes
    setVersion((v) => v + 1); // vuelve a pedir la foto nueva
    setRetomando(false); // oculta la cámara
  }

  async function eliminarRostro() {
    setMensaje("");
    setError("");
    if (!window.confirm(`¿Eliminar el rostro y la foto de ${cliente.nombre}?`)) return;

    try {
      const r = await api(`/users/${cliente.id}/face`, { method: "DELETE", token });
      setMensaje(r.mensaje);
      setInfo(null);
      setRetomando(false);
      await onCambio();
    } catch (err) {
      setError(err.message);
    }
  }

  return (
    <div className="tarjeta">
      <h2>Rostro de {cliente.nombre}</h2>

      {mostrarCamara ? (
        <>
          {!cliente.rostroRegistrado && <p>Este cliente aún no tiene rostro registrado.</p>}

          <label>
            <input
              type="checkbox"
              checked={consentimiento}
              onChange={(e) => setConsentimiento(e.target.checked)}
            />{" "}
            El cliente aceptó que se guarden el descriptor de su rostro y una foto pequeña
          </label>

          <FaceCamera
            onBeforeCapture={validar}
            onDescriptor={guardarRostro}
            textoBoton="Capturar y guardar rostro"
            mensajeExito="Rostro guardado"
          />

          {cliente.rostroRegistrado && (
            <p>
              <button onClick={() => setRetomando(false)}>Cancelar</button>
            </p>
          )}
        </>
      ) : (
        <>
          <p>
            Rostro registrado: <strong>Sí</strong>
            {infoActual?.fechaRegistro && <> · {formatearFechaHora(infoActual.fechaRegistro)}</>}
          </p>

          {infoActual?.foto ? (
            <img
              src={infoActual.foto}
              alt={`Rostro de ${cliente.nombre}`}
              style={{ maxWidth: 240, borderRadius: 12 }}
            />
          ) : (
            infoActual && (
              <p>
                Este rostro se registró sin foto guardada. Usa <strong>Tomar otra foto</strong>{" "}
                para agregarla.
              </p>
            )
          )}

          <div className="acciones" style={{ marginTop: 12 }}>
            <button onClick={() => setRetomando(true)}>Tomar otra foto</button>
            <button onClick={eliminarRostro}>Eliminar rostro</button>
          </div>
        </>
      )}

      {mensaje && <p className="ok">{mensaje}</p>}
      {error && <p className="error">{error}</p>}
    </div>
  );
}