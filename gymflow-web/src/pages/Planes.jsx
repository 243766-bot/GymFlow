import { useCallback, useEffect, useState } from "react";
import { api } from "../api";
import { useAuth } from "../AuthContext";
import { formatearMoneda } from "../formato";

const FORM_VACIO = { nombre: "", duracionDias: "", precio: "" };

export default function Planes() {
  const { token } = useAuth();
  const [planes, setPlanes] = useState([]);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState("");
  const [mensaje, setMensaje] = useState("");
  const [form, setForm] = useState(FORM_VACIO);
  const [editandoId, setEditandoId] = useState(null); // null = estamos creando uno nuevo
  const [guardando, setGuardando] = useState(false);

  const cargar = useCallback(async () => {
    try {
      setPlanes(await api("/plans"));
    } catch (err) {
      setError(err.message);
    } finally {
      setCargando(false);
    }
  }, []);

  useEffect(() => {
    cargar();
  }, [cargar]);

  function cambiarCampo(campo, valor) {
    setForm((anterior) => ({ ...anterior, [campo]: valor }));
  }

  function cancelarEdicion() {
    setEditandoId(null);
    setForm(FORM_VACIO);
  }

  function editar(p) {
    setMensaje("");
    setError("");
    setEditandoId(p.id);
    setForm({
      nombre: p.nombre,
      duracionDias: String(p.duracionDias),
      precio: String(p.precio),
    });
    window.scrollTo({ top: 0, behavior: "smooth" });
  }

  async function guardar(e) {
    e.preventDefault();
    setError("");
    setMensaje("");
    setGuardando(true);

    const cuerpo = {
      nombre: form.nombre,
      duracionDias: Number(form.duracionDias),
      precio: Number(form.precio),
    };

    try {
      if (editandoId) {
        await api(`/plans/${editandoId}`, { method: "PUT", token, body: cuerpo });
        setMensaje("Plan actualizado correctamente");
      } else {
        await api("/plans", { method: "POST", token, body: cuerpo });
        setMensaje("Plan creado correctamente");
      }
      cancelarEdicion();
      await cargar();
    } catch (err) {
      setError(err.message);
    } finally {
      setGuardando(false);
    }
  }

  async function eliminar(p) {
    if (!window.confirm(`¿Eliminar el plan "${p.nombre}"?`)) return;
    setError("");
    setMensaje("");
    try {
      const r = await api(`/plans/${p.id}`, { method: "DELETE", token });
      setMensaje(r.mensaje);
      if (editandoId === p.id) cancelarEdicion();
      await cargar();
    } catch (err) {
      // Si el plan ya tiene membresías, el servidor explica por qué no se puede eliminar
      setError(err.message);
    }
  }

  return (
    <div className="contenedor ancho">
      <h1>Planes de membresía</h1>

      {mensaje && <p className="ok">{mensaje}</p>}
      {error && <p className="error">{error}</p>}

      <div className="tarjeta">
        <h2>{editandoId ? "Editar plan" : "Nuevo plan"}</h2>
        <form className="formulario horizontal" onSubmit={guardar}>
          <input
            type="text"
            placeholder="Nombre (Mensual, Trimestral...)"
            value={form.nombre}
            onChange={(e) => cambiarCampo("nombre", e.target.value)}
            required
          />
          <input
            type="number"
            placeholder="Duración en días"
            min="1"
            max="3650"
            step="1"
            value={form.duracionDias}
            onChange={(e) => cambiarCampo("duracionDias", e.target.value)}
            required
          />
          <input
            type="number"
            placeholder="Precio"
            min="0.01"
            step="0.01"
            value={form.precio}
            onChange={(e) => cambiarCampo("precio", e.target.value)}
            required
          />
          <button type="submit" disabled={guardando}>
            {guardando ? "Guardando..." : editandoId ? "Guardar cambios" : "Crear plan"}
          </button>
          {editandoId && (
            <button type="button" onClick={cancelarEdicion}>
              Cancelar
            </button>
          )}
        </form>
      </div>

      {cargando ? (
        <p>Cargando...</p>
      ) : planes.length === 0 ? (
        <p>Aún no hay planes. Crea el primero con el formulario de arriba.</p>
      ) : (
        <div className="tabla-scroll">
          <table className="tabla">
            <thead>
              <tr>
                <th>Nombre</th>
                <th className="derecha">Duración</th>
                <th className="derecha">Precio</th>
                <th>Acciones</th>
              </tr>
            </thead>
            <tbody>
              {planes.map((p) => (
                <tr key={p.id}>
                  <td>{p.nombre}</td>
                  <td className="derecha">{p.duracionDias} días</td>
                  <td className="derecha">{formatearMoneda(p.precio)}</td>
                  <td>
                    <div className="acciones">
                      <button onClick={() => editar(p)}>Editar</button>
                      <button onClick={() => eliminar(p)}>Eliminar</button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}