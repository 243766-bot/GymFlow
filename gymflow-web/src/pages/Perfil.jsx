import { useCallback, useEffect, useState } from "react";
import { api } from "../api";
import { useAuth } from "../AuthContext";
import {
  etiquetaMetodo,
  formatearFecha,
  formatearFechaHora,
  formatearMoneda,
} from "../formato";

const CLAVE_PAGO = "gymflow_pago_pendiente";
const UN_DIA_MS = 24 * 60 * 60 * 1000;

function claseEstadoPago(estado) {
  if (estado === "APROBADO") return "ok";
  if (estado === "RECHAZADO") return "error";
  return "";
}

export default function Perfil() {
  const { usuario, token } = useAuth();
  const esCliente = usuario.rol === "ROLE_USER";

  const [membresia, setMembresia] = useState(null);
  const [pagos, setPagos] = useState([]);
  const [planes, setPlanes] = useState([]);
  const [planElegido, setPlanElegido] = useState("");
  const [pagando, setPagando] = useState(false);
  const [verificando, setVerificando] = useState(null); // ID del pago que se está verificando
  const [errorPago, setErrorPago] = useState("");
  const [aviso, setAviso] = useState(esCliente ? "Cargando membresía..." : "");

  // Carga la membresía y los pagos; devuelve la lista de pagos
  const cargarDatos = useCallback(async () => {
    try {
      setMembresia(await api("/memberships/me", { token }));
      setAviso("");
    } catch (err) {
      setMembresia(null);
      setAviso(err.status === 404 ? "Aún no tienes una membresía." : err.message);
    }

    try {
      const lista = await api("/payments/me", { token });
      setPagos(lista);
      return lista;
    } catch {
      setPagos([]);
      return [];
    }
  }, [token]);

  useEffect(() => {
    if (!esCliente) return;

    let cancelado = false;

    async function iniciar() {
      const lista = await cargarDatos();
      if (cancelado) return;

      // Pagos de Mercado Pago recientes que siguen pendientes: se le pregunta a Mercado Pago
      const limite = Date.now() - UN_DIA_MS;
      const pendientes = lista
        .filter(
          (p) =>
            p.metodo === "MERCADO_PAGO" &&
            p.estado === "PENDIENTE" &&
            new Date(p.fecha).getTime() > limite
        )
        .slice(0, 3);

      if (pendientes.length === 0) return;

      await Promise.allSettled(pendientes.map((p) => api(`/payments/${p.id}/status`, { token })));
      if (!cancelado) await cargarDatos();
    }

    iniciar();

    api("/plans")
      .then((p) => {
        if (!cancelado) setPlanes(p);
      })
      .catch(() => {
        if (!cancelado) setPlanes([]);
      });

    return () => {
      cancelado = true;
    };
  }, [esCliente, token, cargarDatos]);

  // Botón "Verificar" de un pago pendiente
  async function verificarPago(pago) {
    setErrorPago("");
    setVerificando(pago.id);
    try {
      await api(`/payments/${pago.id}/status`, { token });
      await cargarDatos();
    } catch (err) {
      setErrorPago(err.message);
    } finally {
      setVerificando(null);
    }
  }

  // Crea el cobro en el backend y manda al cliente a la página de pago de Mercado Pago
  async function pagarEnLinea() {
    if (!planElegido) {
      setErrorPago("Elige un plan");
      return;
    }
    setErrorPago("");
    setPagando(true);
    try {
      const r = await api("/payments/checkout", {
        method: "POST",
        token,
        body: { planId: Number(planElegido) },
      });
      // Guardamos el número de pago por si Mercado Pago no lo devuelve en la dirección
      sessionStorage.setItem(CLAVE_PAGO, String(r.pagoId));
      window.location.href = r.urlPago;
    } catch (err) {
      setErrorPago(err.message);
      setPagando(false);
    }
  }

  const vigente = membresia?.estado === "ACTIVA";

  return (
    <div className="contenedor">
      <h1>Mi perfil</h1>

      <div className="tarjeta">
        <p><strong>ID:</strong> {usuario.id}</p>
        <p><strong>Nombre:</strong> {usuario.nombre}</p>
        <p><strong>Correo:</strong> {usuario.email}</p>
        <p><strong>Rol:</strong> {usuario.rol}</p>
      </div>

      {esCliente && (
        <>
          <div className="tarjeta">
            <h2>Mi membresía</h2>
            {membresia ? (
              <>
                <p>
                  <strong>Estado:</strong>{" "}
                  <span className={vigente ? "ok" : "error"}>{membresia.estado}</span>
                </p>
                <p><strong>Plan:</strong> {membresia.plan.nombre}</p>
                <p><strong>Inicio:</strong> {formatearFecha(membresia.fechaInicio)}</p>
                <p><strong>Vence:</strong> {formatearFecha(membresia.fechaVencimiento)}</p>
              </>
            ) : (
              <p>{aviso}</p>
            )}
          </div>

          <div className="tarjeta">
            <h2>{vigente ? "Renovar en línea" : "Pagar membresía en línea"}</h2>

            {planes.length === 0 ? (
              <p>Por ahora no hay planes disponibles.</p>
            ) : (
              <div className="formulario horizontal">
                <select value={planElegido} onChange={(e) => setPlanElegido(e.target.value)}>
                  <option value="">Elige un plan</option>
                  {planes.map((p) => (
                    <option key={p.id} value={p.id}>
                      {p.nombre} · {p.duracionDias} días · {formatearMoneda(p.precio)}
                    </option>
                  ))}
                </select>
                <button onClick={pagarEnLinea} disabled={pagando}>
                  {pagando ? "Redirigiendo..." : "Pagar con Mercado Pago"}
                </button>
              </div>
            )}

            {errorPago && <p className="error">{errorPago}</p>}
            <p className="pequeno">
              Serás enviado a Mercado Pago para pagar. Al aprobarse el pago, tu membresía se activa o se
              renueva automáticamente.
            </p>
          </div>

          <div className="tarjeta">
            <h2>Mis pagos</h2>
            {pagos.length === 0 ? (
              <p>Aún no tienes pagos registrados.</p>
            ) : (
              <div className="tabla-scroll">
                <table className="tabla">
                  <thead>
                    <tr>
                      <th>Fecha</th>
                      <th>Concepto</th>
                      <th>Método</th>
                      <th>Estado</th>
                      <th className="derecha">Monto</th>
                    </tr>
                  </thead>
                  <tbody>
                    {pagos.map((p) => (
                      <tr key={p.id}>
                        <td>{formatearFechaHora(p.fecha)}</td>
                        <td>{p.concepto}</td>
                        <td>{etiquetaMetodo(p.metodo)}</td>
                        <td className={claseEstadoPago(p.estado)}>
                          {p.estado}
                          {p.metodo === "MERCADO_PAGO" && p.estado === "PENDIENTE" && (
                            <>
                              {" "}
                              <button onClick={() => verificarPago(p)} disabled={verificando === p.id}>
                                {verificando === p.id ? "Verificando..." : "Verificar"}
                              </button>
                            </>
                          )}
                        </td>
                        <td className="derecha">{formatearMoneda(p.monto)}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </div>
        </>
      )}
    </div>
  );
}