import { useCallback, useEffect, useState } from "react";
import { api } from "../api";
import { useAuth } from "../AuthContext";
import { formatearMoneda } from "../formato";

const STOCK_BAJO = 5;
const FORM_VACIO = { nombre: "", categoria: "", tamano: "", precio: "", stock: "", activo: true };

export default function Productos() {
  const { token } = useAuth();
  const [productos, setProductos] = useState([]);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState("");
  const [mensaje, setMensaje] = useState("");
  const [form, setForm] = useState(FORM_VACIO);
  const [editandoId, setEditandoId] = useState(null); // null = estamos creando uno nuevo
  const [guardando, setGuardando] = useState(false);

  const cargar = useCallback(async () => {
    try {
      setProductos(await api("/products", { token }));
    } catch (err) {
      setError(err.message);
    } finally {
      setCargando(false);
    }
  }, [token]);

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
      categoria: p.categoria,
      tamano: p.tamano ?? "",
      precio: String(p.precio),
      stock: String(p.stock),
      activo: p.activo,
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
      categoria: form.categoria,
      tamano: form.tamano || null,
      precio: Number(form.precio),
      stock: Number(form.stock),
      activo: form.activo,
    };

    try {
      if (editandoId) {
        await api(`/products/${editandoId}`, { method: "PUT", token, body: cuerpo });
        setMensaje("Producto actualizado correctamente");
      } else {
        await api("/products", { method: "POST", token, body: cuerpo });
        setMensaje("Producto creado correctamente");
      }
      cancelarEdicion();
      await cargar();
    } catch (err) {
      setError(err.message);
    } finally {
      setGuardando(false);
    }
  }

  async function reactivar(p) {
    setError("");
    setMensaje("");
    try {
      await api(`/products/${p.id}`, {
        method: "PUT",
        token,
        body: {
          nombre: p.nombre,
          categoria: p.categoria,
          tamano: p.tamano,
          precio: p.precio,
          stock: p.stock,
          activo: true,
        },
      });
      setMensaje(`${p.nombre} fue reactivado`);
      await cargar();
    } catch (err) {
      setError(err.message);
    }
  }

  async function eliminar(p) {
    if (!window.confirm(`¿Eliminar "${p.nombre}"?`)) return;
    setError("");
    setMensaje("");
    try {
      const r = await api(`/products/${p.id}`, { method: "DELETE", token });
      setMensaje(r.mensaje); // puede decir que se eliminó o que se desactivó por tener ventas
      if (editandoId === p.id) cancelarEdicion();
      await cargar();
    } catch (err) {
      setError(err.message);
    }
  }

  return (
    <div className="contenedor ancho">
      <h1>Productos</h1>

      {mensaje && <p className="ok">{mensaje}</p>}
      {error && <p className="error">{error}</p>}

      <div className="tarjeta">
        <h2>{editandoId ? "Editar producto" : "Nuevo producto"}</h2>
        <form className="formulario horizontal" onSubmit={guardar}>
          <input
            type="text"
            placeholder="Nombre"
            value={form.nombre}
            onChange={(e) => cambiarCampo("nombre", e.target.value)}
            required
          />
          <input
            type="text"
            placeholder="Categoría (Bebidas, Suplementos...)"
            value={form.categoria}
            onChange={(e) => cambiarCampo("categoria", e.target.value)}
            required
          />
          <input
            type="text"
            placeholder="Tamaño (opcional, ej. 600 ml)"
            value={form.tamano}
            onChange={(e) => cambiarCampo("tamano", e.target.value)}
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
          <input
            type="number"
            placeholder="Stock"
            min="0"
            step="1"
            value={form.stock}
            onChange={(e) => cambiarCampo("stock", e.target.value)}
            required
          />
          {editandoId && (
            <label>
              <input
                type="checkbox"
                checked={form.activo}
                onChange={(e) => cambiarCampo("activo", e.target.checked)}
              />{" "}
              Activo
            </label>
          )}
          <button type="submit" disabled={guardando}>
            {guardando ? "Guardando..." : editandoId ? "Guardar cambios" : "Crear producto"}
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
      ) : productos.length === 0 ? (
        <p>Aún no hay productos. Crea el primero con el formulario de arriba.</p>
      ) : (
        <div className="tabla-scroll">
          <table className="tabla">
            <thead>
              <tr>
                <th>Nombre</th>
                <th>Categoría</th>
                <th>Tamaño</th>
                <th className="derecha">Precio</th>
                <th className="derecha">Stock</th>
                <th>Estado</th>
                <th>Acciones</th>
              </tr>
            </thead>
            <tbody>
              {productos.map((p) => (
                <tr key={p.id}>
                  <td>{p.nombre}</td>
                  <td>{p.categoria}</td>
                  <td>{p.tamano ?? "—"}</td>
                  <td className="derecha">{formatearMoneda(p.precio)}</td>
                  <td className="derecha">
                    {p.stock}
                    {p.stock <= STOCK_BAJO && p.activo && <span className="etiqueta error">Stock bajo</span>}
                  </td>
                  <td className={p.activo ? "ok" : "error"}>{p.activo ? "Activo" : "Desactivado"}</td>
                  <td>
                    <div className="acciones">
                      <button onClick={() => editar(p)}>Editar</button>
                      {!p.activo && <button onClick={() => reactivar(p)}>Reactivar</button>}
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