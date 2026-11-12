import { useEffect, useRef, useState } from "react";
import FaceCamera from "../components/FaceCamera";
import { api } from "../api";
import { formatearFecha } from "../formato";

export default function Caja() {
  const [resultado, setResultado] = useState(null);
  const temporizador = useRef(null);

  useEffect(() => {
    return () => clearTimeout(temporizador.current);
  }, []);

  // 1. Antes de capturar: pedir un desafío de un solo uso
  async function pedirDesafio() {
    setResultado(null);
    clearTimeout(temporizador.current);
    const respuesta = await api("/auth/face/challenge", { method: "POST" });
    return respuesta.challenge;
  }

  // 2. Con el descriptor y el desafío: intentar el login facial
  async function identificar(descriptor, challenge) {
    const respuesta = await api("/auth/face/login", {
      method: "POST",
      body: { challenge, descriptor },
    });

    // En la caja no guardamos la sesión del cliente: solo mostramos sus datos
    setResultado({ usuario: respuesta.usuario, membresia: respuesta.membresia });
    temporizador.current = setTimeout(() => setResultado(null), 10000);
  }

  const membresia = resultado?.membresia;
  const activa = membresia?.estado === "ACTIVA";

  return (
    <div className="contenedor">
      <h1>Caja · Acceso con rostro</h1>

      {resultado && (
        <div className={`tarjeta ${activa ? "ok" : "error"}`}>
          <h2>¡Hola, {resultado.usuario.nombre}!</h2>
          {membresia ? (
            <>
              <p><strong>Membresía:</strong> {membresia.estado}</p>
              <p><strong>Plan:</strong> {membresia.plan.nombre}</p>
              <p><strong>Vence:</strong> {formatearFecha(membresia.fechaVencimiento)}</p>
              {activa && <p>Te quedan {membresia.diasRestantes} días.</p>}
            </>
          ) : (
            <p>Aún no tienes una membresía. Pasa a recepción.</p>
          )}
        </div>
      )}

      <FaceCamera
        onBeforeCapture={pedirDesafio}
        onDescriptor={identificar}
        textoBoton="Identificarme"
        mensajeExito="Identificación correcta"
      />
    </div>
  );
}