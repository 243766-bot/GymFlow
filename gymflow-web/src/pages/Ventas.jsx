import { useCallback, useEffect, useMemo, useState } from "react";
import { api } from "../api";
import { useAuth } from "../AuthContext";
import { formatearHora, formatearMoneda, hoyISO } from "../formato";

export default function Ventas() {
  const { token } = useAuth();
  const [productos, setProductos] = useState([]);
  const [clientes, setClientes] = useState([]);
  const [ventasHoy, setVentasHoy] = useState([]);
  const [carrito, setCarrito] = useState([]); // [{ id, nombre, tamano, precio, stock, cantidad }]
  const [clienteId, setClienteId] = useState("");
  const [busqueda, setBusqueda] = useState("");
  const [error, setError] = useState("");
  const [mensaje, setMensaje] = useState("");
  const [cobrando, setCobrando] = useState(false);

  const cargarProductos = useCallback(async () => {
    try {
      setProductos(await api("/products", { token }));
    } catch (err) {
      setError(err.message);
    }
  }, [token]);

  const cargarVentasHoy = useCallback(async () => {
    try {
      setVentasHoy(await api(`/sales?fecha=${hoyISO()}`, { token }));
    } catch (err) {
      setError(err.message);
    }
  }, [token]);

  const cargarClientes = useCallback(async () => {
    try {
      const lista = await api("/users", { token });
      // Solo clientes con la cuenta activa (no administradores ni baneados)
      setClientes(lista.filter((c) => c.rol === "ROLE_USER" && c.activo));
    } catch (err) {
      setError(err.message);
    }
  }, [token]);

  useEffect(() => {
    cargarProductos();
    cargarVentasHoy();
    cargarClientes();
  }, [cargarProductos, cargarVentasHoy, cargarClientes]);

  // Solo se pueden vender productos activos y con existencias
  const disponibles = productos.filter((p) => {
    if (!p.activo || p.stock <= 0) return false;
    const texto = `${p.nombre} ${p.categoria}`.toLowerCase();
    return texto.includes(busqueda.toLowerCase());
  });

  const total = useMemo(
    () => carrito.reduce((suma, i) => suma + i.precio * i.cantidad, 0),
    [carrito]
  );

  function agregar(p) {
    setMensaje("");
    setError("");
    setCarrito((actual) => {
      const existente = actual.find((i) => i.id === p.id);
      if (existente) {
        if (existente.cantidad >= p.stock) return actual; // no se puede pasar del stock
        return actual.map((i) => (i.id === p.id ? { ...i, cantidad: i.cantidad + 1 } : i));
      }
      return [
        ...actual,
        {
          id: p.id,
          nombre: p.nombre,
          tamano: p.tamano,
          precio: Number(p.precio),
          stock: p.stock,
          cantidad: 1,
        },
      ];
    });
  }

  function cambiarCantidad(id, valor) {
    setCarrito((actual) =>
      actual.map((i) => {
        if (i.id !== id) return i;
        const cantidad = Math.max(1, Math.min(i.stock, Number(valor) || 1));
        return { ...i, cantidad };
      })
    );
  }

  function quitar(id) {
    setCarrito((actual) => actual.filter((i) => i.id !== id));
  }

  async function cobrar() {
    setError("");
    setMensaje("");
    setCobrando(true);
    try {
      const venta = await api("/sales", {
        method: "POST",
        token,
        body: {
          clienteId: clienteId ? Number(clienteId) : null,
          items: carrito.map((i) => ({ productoId: i.id, cantidad: i.cantidad })),
        },
      });
      setCarrito([]);
      setClienteId("");
      setMensaje(`Venta #${venta.id} registrada por ${formatearMoneda(venta.total)}`);
      await Promise.all([cargarProductos(), cargarVentasHoy()]);
    } catch (err) {
      setError(err.message);
      await cargarProductos(); // por si el stock cambió mientras armabas la venta
    } finally {
      setCobrando(false);
    }
  }

  return (
    <div className="contenedor ancho">
      <h1>Punto de venta</h1>

      {mensaje && <p className="ok">{mensaje}</p>}
      {error && <p className="error">{error}</p>}

      <div className="dos-columnas">
        {/* Columna izquierda: productos */}
        <div>
          <h2>Productos</h2>
          <input
            type="text"
            placeholder="Buscar por nombre o categoría"
            value={busqueda}
            onChange={(e) => setBusqueda(e.target.value)}
            style={{ width: "100%", boxSizing: "border-box", marginBottom: 12 }}
          />

          {disponibles.length === 0 ? (
            <p>No hay productos disponibles para vender.</p>
          ) : (
            <div className="tabla-scroll">
              <table className="tabla">
                <thead>
                  <tr>
                    <th>Producto</th>
                    <th className="derecha">Precio</th>
                    <th className="derecha">Stock</th>
                    <th></th>
                  </tr>
                </thead>
                <tbody>
                  {disponibles.map((p) => (
                    <tr key={p.id}>
                      <td>
                        {p.nombre}
                        {p.tamano && <span className="pequeno"> · {p.tamano}</span>}
                      </td>
                      <td className="derecha">{formatearMoneda(p.precio)}</td>
                      <td className="derecha">{p.stock}</td>
                      <td>
                        <button onClick={() => agregar(p)}>Agregar</button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>

        {/* Columna derecha: carrito */}
        <div>
          <h2>Venta actual</h2>

          {carrito.length === 0 ? (
            <p>El carrito está vacío. Agrega productos de la lista.</p>
          ) : (
            <div className="tabla-scroll">
              <table className="tabla">
                <thead>
                  <tr>
                    <th>Producto</th>
                    <th>Cant.</th>
                    <th className="derecha">Subtotal</th>
                    <th></th>
                  </tr>
                </thead>
                <tbody>
                  {carrito.map((i) => (
                    <tr key={i.id}>
                      <td>
                        {i.nombre}
                        <br />
                        <span className="pequeno">{formatearMoneda(i.precio)} c/u</span>
                      </td>
                      <td>
                        <input
                          type="number"
                          min="1"
                          max={i.stock}
                          value={i.cantidad}
                          onChange={(e) => cambiarCantidad(i.id, e.target.value)}
                          style={{ width: 70 }}
                        />
                      </td>
                      <td className="derecha">{formatearMoneda(i.precio * i.cantidad)}</td>
                      <td>
                        <button onClick={() => quitar(i.id)}>Quitar</button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}

          <div className="tarjeta">
            <label>
              Cliente (opcional):{" "}
              <select value={clienteId} onChange={(e) => setClienteId(e.target.value)}>
                <option value="">Sin cliente</option>
                {clientes.map((c) => (
                  <option key={c.id} value={c.id}>
                    {c.nombre} ({c.email})
                  </option>
                ))}
              </select>
            </label>

            <p style={{ fontSize: "1.4rem", fontWeight: 700 }}>Total: {formatearMoneda(total)}</p>

            <button onClick={cobrar} disabled={carrito.length === 0 || cobrando}>
              {cobrando ? "Cobrando..." : "Cobrar"}
            </button>
          </div>
        </div>
      </div>

      <h2>Ventas de hoy</h2>
      {ventasHoy.length === 0 ? (
        <p>Todavía no hay ventas hoy.</p>
      ) : (
        <div className="tabla-scroll">
          <table className="tabla">
            <thead>
              <tr>
                <th>#</th>
                <th>Hora</th>
                <th>Cliente</th>
                <th>Detalle</th>
                <th className="derecha">Total</th>
              </tr>
            </thead>
            <tbody>
              {ventasHoy.map((v) => (
                <tr key={v.id}>
                  <td>{v.id}</td>
                  <td>{formatearHora(v.fecha)}</td>
                  <td>{v.clienteNombre ?? "—"}</td>
                  <td>{v.detalles.map((d) => `${d.cantidad} × ${d.producto}`).join(", ")}</td>
                  <td className="derecha">{formatearMoneda(v.total)}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}