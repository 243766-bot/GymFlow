import { useCallback, useEffect, useState } from "react";
import { api } from "../api";
import { useAuth } from "../AuthContext";
import RostroCliente from "../components/RostroCliente";
import MembresiaCliente from "../components/MembresiaCliente";
import { formatearFecha } from "../formato";

const FORMULARIO_VACIO = { nombre: "", email: "", password: "", telefono: "" };

export default function Admin() {
  const { token, usuario: yo } = useAuth();
  const [clientes, setClientes] = useState([]);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState("");
  const [mensaje, setMensaje] = useState("");
  const [panel, setPanel] = useState(null); // { tipo: "rostro" | "membresia", id: <ID del cliente> }
  const [nuevo, setNuevo] = useState(FORMULARIO_VACIO);
  const [creando, setCreando] = useState(false);

  const cargar = useCallback(async () => {
    try {
      const lista = await api("/users", { token });
      setClientes(lista);
    } catch (err) {
      setError(err.message);
    } finally {
      setCargando(false);
    }
  }, [token]);

  useEffect(() => {
    cargar();
  }, [cargar]);

  // Ejecuta una acción, muestra el error si falla y recarga la lista
  async function ejecutar(accion) {
    setError("");
    setMensaje("");
    try {
      await accion();
      await cargar();
    } catch (err) {
      setError(err.message);
    }
  }

  function alternarBan(c) {
    return ejecutar(async () => {
      await api(`/users/${c.id}/ban`, {
        method: "PATCH",
        token,
        body: { baneado: c.activo }, // si está activo, lo baneamos; si no, lo reactivamos
      });
      setMensaje(c.activo ? `${c.nombre} fue baneado` : `${c.nombre} fue reactivado`);
    });
  }

  function eliminar(c) {
    if (!window.confirm(`¿Eliminar a ${c.nombre}? Esta acción no se puede deshacer.`)) return;
    return ejecutar(async () => {
      await api(`/users/${c.id}`, { method: "DELETE", token });
      if (panel?.id === c.id) setPanel(null);
      setMensaje(`${c.nombre} fue eliminado`);
    });
  }

  async function crearCliente(e) {
    e.preventDefault();
    setError("");
    setMensaje("");
    setCreando(true);
    try {
      await api("/users", {
        method: "POST",
        token,
        body: { ...nuevo, telefono: nuevo.telefono || null },
      });
      setNuevo(FORMULARIO_VACIO);
      setMensaje("Cliente creado correctamente");
      await cargar();
    } catch (err) {
      setError(err.message);
    } finally {
      setCreando(false);
    }
  }

  function cambiarCampo(campo, valor) {
    setNuevo((anterior) => ({ ...anterior, [campo]: valor }));
  }

  const clientePanel = panel ? clientes.find((c) => c.id === panel.id) : null;

  return (
    <div className="contenedor ancho">
      <h1>Clientes</h1>

      {mensaje && <p className="ok">{mensaje}</p>}
      {error && <p className="error">{error}</p>}

      {/* Panel del cliente elegido: arriba de la tabla para que se vea de inmediato */}
      {clientePanel && (
        <div>
          <p>
            <button onClick={() => setPanel(null)}>Cerrar panel</button>
          </p>

          {panel.tipo === "rostro" && (
            <RostroCliente
              key={`rostro-${clientePanel.id}`}
              cliente={clientePanel}
              token={token}
              onCambio={cargar}
            />
          )}

          {panel.tipo === "membresia" && (
            <MembresiaCliente
              key={`membresia-${clientePanel.id}`}
              cliente={clientePanel}
              token={token}
              onCambio={cargar}
            />
          )}
        </div>
      )}

      {cargando ? (
        <p>Cargando...</p>
      ) : (
        <div className="tabla-scroll">
          <table className="tabla">
            <thead>
              <tr>
                <th>ID</th>
                <th>Nombre</th>
                <th>Correo</th>
                <th>Membresía</th>
                <th>Rostro</th>
                <th>Cuenta</th>
                <th>Acciones</th>
              </tr>
            </thead>
            <tbody>
              {clientes.map((c) => {
                const esYo = c.email === yo.email;
                return (
                  <tr key={c.id}>
                    <td>{c.id}</td>
                    <td>
                      {c.nombre}
                      {c.rol === "ROLE_ADMIN" && <span className="etiqueta">Admin</span>}
                    </td>
                    <td>{c.email}</td>
                    <td>
                      {c.membresia ? (
                        <>
                          <span className={c.membresia.estado === "ACTIVA" ? "ok" : "error"}>
                            {c.membresia.estado}
                          </span>{" "}
                          · {c.membresia.plan.nombre}
                          <br />
                          Vence: {formatearFecha(c.membresia.fechaVencimiento)}
                        </>
                      ) : (
                        "Sin membresía"
                      )}
                    </td>
                    <td>{c.rostroRegistrado ? "Sí" : "No"}</td>
                    <td className={c.activo ? "ok" : "error"}>{c.activo ? "Activa" : "Baneada"}</td>
                    <td>
                      <div className="acciones">
                        <button onClick={() => setPanel({ tipo: "membresia", id: c.id })}>
                          Membresía
                        </button>
                        <button onClick={() => setPanel({ tipo: "rostro", id: c.id })}>Rostro</button>
                        <button onClick={() => alternarBan(c)} disabled={esYo}>
                          {c.activo ? "Banear" : "Reactivar"}
                        </button>
                        <button onClick={() => eliminar(c)} disabled={esYo}>
                          Eliminar
                        </button>
                      </div>
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
      )}

      <h2>Nuevo cliente</h2>
      <form className="formulario" onSubmit={crearCliente}>
        <input
          type="text"
          placeholder="Nombre"
          value={nuevo.nombre}
          onChange={(e) => cambiarCampo("nombre", e.target.value)}
          required
        />
        <input
          type="email"
          placeholder="Correo"
          value={nuevo.email}
          onChange={(e) => cambiarCampo("email", e.target.value)}
          required
        />
        <input
          type="password"
          placeholder="Contraseña (mínimo 8 caracteres)"
          value={nuevo.password}
          onChange={(e) => cambiarCampo("password", e.target.value)}
          required
        />
        <input
          type="text"
          placeholder="Teléfono (opcional)"
          value={nuevo.telefono}
          onChange={(e) => cambiarCampo("telefono", e.target.value)}
        />
        <button type="submit" disabled={creando}>
          {creando ? "Creando..." : "Crear cliente"}
        </button>
      </form>
    </div>
  );
}