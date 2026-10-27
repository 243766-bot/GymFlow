import { useEffect, useRef, useState } from "react";
import * as faceapi from "@vladmandic/face-api";

export default function FaceCamera({ onDescriptor }) {
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
        setStatus("Cámara lista. Mira al frente y presiona capturar.");
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
    setStatus("Analizando rostro... (la primera vez puede tardar unos segundos)");

    try {
      const video = videoRef.current;

      if (!video || video.readyState < 2) {
        setStatus("El video aún no está listo. Intenta de nuevo.");
        return;
      }

      const inicio = performance.now();

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

      // El descriptor son 128 números; lo pasamos a un arreglo normal
      onDescriptor(Array.from(result.descriptor));
      const segundos = ((performance.now() - inicio) / 1000).toFixed(1);
      setStatus(`Rostro capturado correctamente (${segundos} s).`);
    } catch (err) {
      console.error(err);
      setStatus("Error al analizar el rostro. Revisa la consola (F12).");
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
        {busy ? "Analizando..." : "Capturar rostro"}
      </button>
    </div>
  );
}