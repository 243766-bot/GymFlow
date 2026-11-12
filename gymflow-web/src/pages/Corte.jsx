import { useCallback, useEffect, useState } from "react";
import { api } from "../api";
import { useAuth } from "../AuthContext";
import {
  etiquetaMetodo,
  formatearFecha,
  formatearHora,
  formatearMoneda,
  hoyISO,
} from "../formato";

export default function Corte() {
  const { token } = useAuth();
  const [fecha, setFecha] = useState(hoyISO());
  const [corte, setCorte] = useState(null);
  const [error, setError] = useState("");
  const [mensaje, setMensaje] = useState("");
  const [concepto, setConcepto] = useState("");
  const [monto, setMonto] = useState("");
  const [guardando, setGuardando] = useState(false);

  // Las salidas se registran con la fecha y hora de ahora, así que solo se pueden agregar al corte de hoy
  const esHoy = fecha === hoyISO();

  const cargar = useCallback(async () => {
    try {
      const datos = await api(`/cashreport/daily?date=${fecha}`, { token });
      setCorte(datos);
      setError("");
    } catch (err) {
      setError(err.message);
    }
  }, [fecha, token]);

  useEffect(() => {
    cargar();
  }, [cargar]);

  function cambiarFecha(valor) {
    if (!valor) return; // no dejamos el campo vacío
    setCorte(null);
    setMensaje("");
    setFecha(valor);
  }

  async function registrarSalida(e) {
    e.preventDefault();
    setError("");
    setMensaje("");
    setGuardando(true);
    try {
      await api("/cash-movements", {
        method: "POST",
        token,
        body: { concepto, monto: Number(monto) },
      });
      setConcepto("");
      setMonto("");
      setMensaje("Salida registrada correctamente");
      await cargar();
    } catch (err) {
      setError(err.message);
    } finally {
      setGuardando(false);
    }
  }

  const totalMembresias = corte
    ? Number(corte.ingresosMembresiasEfectivo) + Number(corte.ingresosMembresiasEnLinea)
    : 0;

  return (
    <div className="contenedor ancho">
      <h1>Corte del día</h1>

      <label>
        Fecha:{" "}
        <input type="date" value={fecha} max={hoyISO()} onChange={(e) => cambiarFecha(e.target.value)} />
      </label>

      {mensaje && <p className="ok">{mensaje}</p>}
      {error && <p className="error">{error}</p>}

      {!corte ? (
        !error && <p>Cargando...</p>
      ) : (
        <>
          <h2>{formatearFecha(corte.fecha)}</h2>

          <div className="resumen">
            <div className="tarjeta">
              <span className="pequeno">Ventas de productos ({corte.numeroVentas})</span>
              <p className="valor ok">{formatearMoneda(corte.totalVentas)}</p>
            </div>
            <div className="tarjeta">
              <span className="pequeno">Membresías cobradas</span>
              <p className="valor ok">{formatearMoneda(totalMembresias)}</p>
              <span className="pequeno">
                Efectivo {formatearMoneda(corte.ingresosMembresiasEfectivo)} · En línea{" "}
                {formatearMoneda(corte.ingresosMembresiasEnLinea)}
              </span>
            </div>
            <div className="tarjeta">
              <span className="pequeno">Salidas de dinero</span>
              <p className="valor error">{formatearMoneda(corte.totalSalidas)}</p>
            </div>
            <div className="tarjeta">
              <span className="pequeno">Balance del día</span>
              <p className={`valor ${Number(corte.balance) >= 0 ? "ok" : "error"}`}>
                {formatearMoneda(corte.balance)}
              </p>
              <span className="pequeno">Ventas + membresías − salidas</span>
            </div>
          </div>

          <h2>Ventas</h2>
          {corte.ventas.length === 0 ? (
            <p>No hubo ventas este día.</p>
          ) : (
            <div className="tabla-scroll">
              <table className="tabla">
                <thead>
                  <tr>
                    <th>#</th>
                    <th>Hora</th>
                    <th>Cliente</th>
                    <th>Atendió</th>
                    <th className="derecha">Artículos</th>
                    <th className="derecha">Total</th>
                  </tr>
                </thead>
                <tbody>
                  {corte.ventas.map((v) => (
                    <tr key={v.id}>
                      <td>{v.id}</td>
                      <td>{formatearHora(v.fecha)}</td>
                      <td>{v.clienteNombre ?? "—"}</td>
                      <td>{v.atendidoPor}</td>
                      <td className="derecha">{v.articulos}</td>
                      <td className="derecha">{formatearMoneda(v.total)}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}

          <h2>Pagos de membresías</h2>
          {corte.pagos.length === 0 ? (
            <p>No hubo cobros de membresías este día.</p>
          ) : (
            <div className="tabla-scroll">
              <table className="tabla">
                <thead>
                  <tr>
                    <th>Hora</th>
                    <th>Cliente</th>
                    <th>Concepto</th>
                    <th>Método</th>
                    <th className="derecha">Monto</th>
                  </tr>
                </thead>
                <tbody>
                  {corte.pagos.map((p) => (
                    <tr key={p.id}>
                      <td>{formatearHora(p.fecha)}</td>
                      <td>{p.usuarioNombre}</td>
                      <td>{p.concepto}</td>
                      <td>{etiquetaMetodo(p.metodo)}</td>
                      <td className="derecha">{formatearMoneda(p.monto)}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}

          <h2>Salidas de dinero</h2>
          {corte.salidas.length === 0 ? (
            <p>No hubo salidas este día.</p>
          ) : (
            <div className="tabla-scroll">
              <table className="tabla">
                <thead>
                  <tr>
                    <th>Hora</th>
                    <th>Concepto</th>
                    <th className="derecha">Monto</th>
                  </tr>
                </thead>
                <tbody>
                  {corte.salidas.map((s) => (
                    <tr key={s.id}>
                      <td>{formatearHora(s.fecha)}</td>
                      <td>{s.concepto}</td>
                      <td className="derecha">{formatearMoneda(s.monto)}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </>
      )}

      {esHoy && (
        <div className="tarjeta">
          <h2>Registrar salida de dinero</h2>
          <form className="formulario horizontal" onSubmit={registrarSalida}>
            <input
              type="text"
              placeholder="Concepto (ej. compra de garrafón)"
              value={concepto}
              onChange={(e) => setConcepto(e.target.value)}
              required
              style={{ minWidth: 260 }}
            />
            <input
              type="number"
              placeholder="Monto"
              min="0.01"
              step="0.01"
              value={monto}
              onChange={(e) => setMonto(e.target.value)}
              required
            />
            <button type="submit" disabled={guardando}>
              {guardando ? "Guardando..." : "Registrar salida"}
            </button>
          </form>
        </div>
      )}
    </div>
  );
}