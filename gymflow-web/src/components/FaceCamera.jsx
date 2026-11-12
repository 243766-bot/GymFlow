import { useEffect, useRef, useState } from "react";
import * as faceapi from "@vladmandic/face-api";

// Recorta el rostro (con un margen) y lo reduce a una miniatura JPEG pequeña
function recortarRostro(video, caja) {
  const margen = 0.4;
  const x = Math.max(0, caja.x - caja.width * margen);
  const y = Math.max(0, caja.y - caja.height * margen);
  const ancho = Math.min(video.videoWidth - x, caja.width * (1 + margen * 2));
  const alto = Math.min(video.videoHeight - y, caja.height * (1 + margen * 2));

  const lado = 240;
  const canvas = document.createElement("canvas");
  canvas.width = lado;
  canvas.height = Math.round(lado * (alto / ancho));
  canvas.getContext("2d").drawImage(video, x, y, ancho, alto, 0, 0, canvas.width, canvas.height);

  return canvas.toDataURL("image/jpeg", 0.8);
}

export default function FaceCamera({
  onDescriptor,
  onBeforeCapture,
  textoBoton = "Capturar rostro",
  mensajeExito = "Rostro capturado correctamente",
}) {
  const videoRef = useRef(null);
  const [status, setStatus] = useState("Cargando modelos...");
  const [ready, setReady] = useState(false);
  const [busy, setBusy] = useState(false);

  useEffect(() => {
    let stream;
    let cancelled = false;

    async function start() {
      try {
        // 1. Esperar a que el motor de TensorFlow esté listo
        await faceapi.tf.ready();

        // 2. Cargar los modelos desde public/models
        await faceapi.nets.tinyFaceDetector.loadFromUri("/models");
        await faceapi.nets.faceLandmark68Net.loadFromUri("/models");
        await faceapi.nets.faceRecognitionNet.loadFromUri("/models");

        // 3. Pedir permiso y abrir la cámara
        stream = await navigator.mediaDevices.getUserMedia({
          video: { width: 640, height: 480, facingMode: "user" },
        });

        if (cancelled) {
          stream.getTracks().forEach((t) => t.stop());
          return;
        }

        videoRef.current.srcObject = stream;
        setReady(true);
        setStatus("Cámara lista. Mira al frente y presiona el botón.");
      } catch (err) {
        console.error(err);
        setStatus("No se pudo abrir la cámara o cargar los modelos.");
      }
    }

    start();

    // 4. Al salir de la pantalla, apagar la cámara
    return () => {
      cancelled = true;
      stream?.getTracks().forEach((t) => t.stop());
    };
  }, []);

  async function capture() {
    if (busy) return;
    setBusy(true);

    try {
      const video = videoRef.current;

      if (!video || video.readyState < 2) {
        setStatus("El video aún no está listo. Intenta de nuevo.");
        return;
      }

      // Paso previo opcional (pedir el desafío al servidor, validar un formulario...)
      let contexto;
      if (onBeforeCapture) {
        setStatus("Preparando...");
        contexto = await onBeforeCapture();
      }

      setStatus("Analizando rostro... (la primera vez puede tardar unos segundos)");

      const result = await faceapi
        .detectSingleFace(
          video,
          new faceapi.TinyFaceDetectorOptions({ inputSize: 320, scoreThreshold: 0.5 })
        )
        .withFaceLandmarks()
        .withFaceDescriptor();

      if (!result) {
        setStatus("No se detectó ningún rostro. Intenta con mejor luz.");
        return;
      }

      // Miniatura del rostro (solo la usa quien la necesite, como el registro del admin)
      const foto = recortarRostro(video, result.detection.box);

      // Entregamos el descriptor (128 números), el contexto y la miniatura
      setStatus("Enviando...");
      await onDescriptor(Array.from(result.descriptor), contexto, foto);
      setStatus(mensajeExito);
    } catch (err) {
      console.error(err);
      setStatus(err?.message || "Ocurrió un error. Intenta de nuevo.");
    } finally {
      setBusy(false);
    }
  }

  return (
    <div>
      <video
        ref={videoRef}
        autoPlay
        muted
        playsInline
        width={640}
        height={480}
        style={{
          borderRadius: 12,
          background: "#000",
          maxWidth: "100%",
          height: "auto",
        }}
      />
      <p>{status}</p>
      <button onClick={capture} disabled={!ready || busy}>
        {busy ? "Procesando..." : textoBoton}
      </button>
    </div>
  );
}