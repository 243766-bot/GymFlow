import { useEffect, useRef, useState } from "react";
import FaceCamera from "../components/FaceCamera";
import { api } from "../api";

export default function Caja() {
  const [cliente, setCliente] = useState(null);
  const temporizador = useRef(null);

  useEffect(() => {
    return () => clearTimeout(temporizador.current);
  }, []);

  // 1. Antes de capturar: pedir un desafío de un solo uso
  async function pedirDesafio() {
    setCliente(null);
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

    // En la caja no guardamos la sesión del cliente: solo mostramos quién es
    setCliente(respuesta.usuario);
    temporizador.current = setTimeout(() => setCliente(null), 8000);
  }

  return (
    <div className="contenedor">
      <h1>Caja · Acceso con rostro</h1>

      {cliente && (
        <div className="tarjeta ok">
          <h2>¡Bienvenido, {cliente.nombre}!</h2>
          <p>{cliente.email}</p>
          <p>Membresía: próximamente</p>
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