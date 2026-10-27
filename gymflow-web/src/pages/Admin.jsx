import { useState } from "react";
import FaceCamera from "../components/FaceCamera";
import { api } from "../api";
import { useAuth } from "../AuthContext";

export default function Admin() {
  const { token } = useAuth();
  const [clienteId, setClienteId] = useState("");
  const [consentimiento, setConsentimiento] = useState(false);
  const [mensaje, setMensaje] = useState("");
  const [error, setError] = useState("");

  // Se ejecuta antes de capturar: valida el formulario
  function validar() {
    setMensaje("");
    setError("");
    if (!clienteId) throw new Error("Escribe el ID del cliente");
    if (!consentimiento) throw new Error("Marca el consentimiento del cliente");
  }

  async function guardarRostro(descriptor) {
    const r = await api(`/users/${clienteId}/face`, {
      method: "POST",
      token,
      body: { descriptor, consentimiento },
    });
    setMensaje(r.mensaje);
  }

  async function eliminarRostro() {
    setMensaje("");
    setError("");
    if (!clienteId) {
      setError("Escribe el ID del cliente");
      return;
    }
    if (!window.confirm(`¿Eliminar el rostro registrado del cliente ${clienteId}?`)) return;

    try {
      const r = await api(`/users/${clienteId}/face`, { method: "DELETE", token });
      setMensaje(r.mensaje);
    } catch (err) {
      setError(err.message);
    }
  }

  return (
    <div className="contenedor">
      <h1>Administración · Rostro del cliente</h1>

      <div className="formulario">
        <input
          type="number"
          placeholder="ID del cliente"
          value={clienteId}
          onChange={(e) => setClienteId(e.target.value)}
        />
        <label>
          <input
            type="checkbox"
            checked={consentimiento}
            onChange={(e) => setConsentimiento(e.target.checked)}
          />{" "}
          El cliente aceptó que se guarde el descriptor de su rostro (no se guarda la foto)
        </label>
      </div>

      <FaceCamera
        onBeforeCapture={validar}
        onDescriptor={guardarRostro}
        textoBoton="Capturar y guardar rostro"
        mensajeExito="Rostro guardado"
      />

      <p>
        <button onClick={eliminarRostro}>Eliminar rostro del cliente</button>
      </p>

      {mensaje && <p className="ok">{mensaje}</p>}
      {error && <p className="error">{error}</p>}
    </div>
  );
}