import { useCallback, useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { api } from "../api";
import { formatearFecha, formatearMoneda } from "../formato";

function etiquetaPlan(p) {
  return `${p.nombre} · ${p.duracionDias} días · ${formatearMoneda(p.precio)}`;
}

export default function MembresiaCliente({ cliente, token, onCambio }) {
  const [planes, setPlanes] = useState([]);
  const [historial, setHistorial] = useState([]); // la más reciente va primero
  const [cargando, setCargando] = useState(true);
  const [planNuevo, setPlanNuevo] = useState("");
  const [fechaInicio, setFechaInicio] = useState("");
  const [planRenovar, setPlanRenovar] = useState(""); // "" = el mismo plan
  const [cobrar, setCobrar] = useState(true); // registrar el cobro en efectivo
  const [mensaje, setMensaje] = useState("");
  const [error, setError] = useState("");
  const [trabajando, setTrabajando] = useState(false);

  const cargar = useCallback(async () => {
    try {
      const [listaPlanes, listaMembresias] = await Promise.all([
        api("/plans"),
        api(`/memberships?usuarioId=${cliente.id}`, { token }),
      ]);
      setPlanes(listaPlanes);
      setHistorial(listaMembresias);
    } catch (err) {
      setError(err.message);
    } finally {
      setCargando(false);
    }
  }, [cliente.id, token]);

  useEffect(() => {
    cargar();
  }, [cargar]);

  // Ejecuta una acción, muestra el resultado y recarga el panel y la tabla de clientes
  async function ejecutar(accion, textoExito) {
    setError("");
    setMensaje("");
    setTrabajando(true);
    try {
      await accion();
      setMensaje(textoExito);
      await cargar();
      await onCambio();
    } catch (err) {
      setError(err.message);
    } finally {
      setTrabajando(false);
    }
  }

  function asignar(e) {
    e.preventDefault();
    if (!planNuevo) {
      setError("Elige un plan");
      return;
    }
    return ejecutar(
      async () => {
        await api("/memberships", {
          method: "POST",
          token,
          body: {
            usuarioId: cliente.id,
            planId: Number(planNuevo),
            fechaInicio: fechaInicio || null,
            cobrarEnEfectivo: cobrar,
          },
        });
        setPlanNuevo("");
        setFechaInicio("");
      },
      cobrar ? "Membresía asignada y cobro registrado" : "Membresía asignada sin cobro"
    );
  }

  function renovar(actual) {
    return ejecutar(
      async () => {
        await api(`/memberships/${actual.id}/renew`, {
          method: "POST",
          token,
          body: { planId: planRenovar ? Number(planRenovar) : null, cobrarEnEfectivo: cobrar },
        });
        setPlanRenovar("");
      },
      cobrar ? "Membresía renovada y cobro registrado" : "Membresía renovada sin cobro"
    );
  }

  function cancelar(actual) {
    if (!window.confirm(`¿Cancelar la membresía de ${cliente.nombre}?`)) return;
    return ejecutar(async () => {
      await api(`/memberships/${actual.id}`, {
        method: "PUT",
        token,
        body: { estado: "CANCELADA" },
      });
    }, "Membresía cancelada");
  }

  const actual = historial[0];
  const vigente = actual?.estado === "ACTIVA";

  // Cuánto se cobraría según el plan elegido
  const planNuevoElegido = planes.find((p) => String(p.id) === planNuevo);
  const planRenovarElegido = planes.find((p) => String(p.id) === planRenovar);
  const montoNuevo = planNuevoElegido?.precio;
  const montoRenovar = planRenovarElegido ? planRenovarElegido.precio : actual?.plan.precio;

  function casillaCobro(monto) {
    return (
      <label>
        <input type="checkbox" checked={cobrar} onChange={(e) => setCobrar(e.target.checked)} />{" "}
        Registrar cobro en efectivo{monto != null && <> ({formatearMoneda(monto)})</>}
      </label>
    );
  }

  // Formulario para crear una membresía nueva (se usa cuando no hay una vigente)
  const formularioNueva = (
    <form className="formulario horizontal" onSubmit={asignar}>
      <select value={planNuevo} onChange={(e) => setPlanNuevo(e.target.value)} required>
        <option value="">Elige un plan</option>
        {planes.map((p) => (
          <option key={p.id} value={p.id}>
            {etiquetaPlan(p)}
          </option>
        ))}
      </select>
      <label>
        Inicio (opcional):{" "}
        <input type="date" value={fechaInicio} onChange={(e) => setFechaInicio(e.target.value)} />
      </label>
      {casillaCobro(montoNuevo)}
      <button type="submit" disabled={trabajando}>
        {trabajando ? "Guardando..." : "Asignar membresía"}
      </button>
    </form>
  );

  return (
    <div className="tarjeta">
      <h2>Membresía de {cliente.nombre}</h2>

      {mensaje && <p className="ok">{mensaje}</p>}
      {error && <p className="error">{error}</p>}

      {cargando ? (
        <p>Cargando...</p>
      ) : planes.length === 0 ? (
        <p>
          Todavía no hay planes. Primero crea uno en <Link to="/planes">Planes</Link>.
        </p>
      ) : !actual ? (
        <>
          <p>Este cliente aún no tiene membresía.</p>
          {formularioNueva}
        </>
      ) : (
        <>
          <p>
            Estado: <strong className={vigente ? "ok" : "error"}>{actual.estado}</strong>
            {vigente && <> · le quedan {actual.diasRestantes} días</>}
          </p>
          <p>
            Plan: <strong>{actual.plan.nombre}</strong> · Inicio: {formatearFecha(actual.fechaInicio)} ·
            Vence: {formatearFecha(actual.fechaVencimiento)}
          </p>

          <h3>{vigente ? "Renovar" : "Renovar (empieza hoy y la reactiva)"}</h3>
          <div className="formulario horizontal">
            <select value={planRenovar} onChange={(e) => setPlanRenovar(e.target.value)}>
              <option value="">Mismo plan ({actual.plan.nombre})</option>
              {planes.map((p) => (
                <option key={p.id} value={p.id}>
                  {etiquetaPlan(p)}
                </option>
              ))}
            </select>
            {casillaCobro(montoRenovar)}
            <button onClick={() => renovar(actual)} disabled={trabajando}>
              Renovar
            </button>
            {vigente && (
              <button onClick={() => cancelar(actual)} disabled={trabajando}>
                Cancelar membresía
              </button>
            )}
          </div>
          <p className="pequeno">
            Si la membresía sigue vigente, los días nuevos se suman a su vencimiento; si ya venció, cuenta desde hoy.
          </p>

          {!vigente && (
            <>
              <h3>Nueva membresía</h3>
              {formularioNueva}
            </>
          )}
        </>
      )}

      {historial.length > 0 && (
        <>
          <h3>Historial</h3>
          <div className="tabla-scroll">
            <table className="tabla">
              <thead>
                <tr>
                  <th>Plan</th>
                  <th>Inicio</th>
                  <th>Vence</th>
                  <th>Estado</th>
                </tr>
              </thead>
              <tbody>
                {historial.map((m) => (
                  <tr key={m.id}>
                    <td>{m.plan.nombre}</td>
                    <td>{formatearFecha(m.fechaInicio)}</td>
                    <td>{formatearFecha(m.fechaVencimiento)}</td>
                    <td className={m.estado === "ACTIVA" ? "ok" : "error"}>{m.estado}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </>
      )}

      <p className="pequeno">
        El cobro se registra por el precio del plan y aparece en el corte del día.
      </p>
    </div>
  );
}