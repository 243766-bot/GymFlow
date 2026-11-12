import { useEffect, useState } from "react";
import { api } from "../api";
import { useAuth } from "../AuthContext";
import { etiquetaMetodo, formatearFecha, formatearFechaHora, formatearMoneda } from "../formato";

export default function Perfil() {
  const { usuario, token } = useAuth();
  const esCliente = usuario.rol === "ROLE_USER";

  const [membresia, setMembresia] = useState(null);
  const [pagos, setPagos] = useState([]);
  const [aviso, setAviso] = useState(esCliente ? "Cargando membresía..." : "");

  useEffect(() => {
    if (!esCliente) return;

    let cancelado = false;

    api("/memberships/me", { token })
      .then((m) => {
        if (!cancelado) {
          setMembresia(m);
          setAviso("");
        }
      })
      .catch((err) => {
        if (!cancelado) {
          setAviso(err.status === 404 ? "Aún no tienes una membresía." : err.message);
        }
      });

    api("/payments/me", { token })
      .then((p) => {
        if (!cancelado) setPagos(p);
      })
      .catch(() => {
        if (!cancelado) setPagos([]);
      });

    return () => {
      cancelado = true;
    };
  }, [esCliente, token]);

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
                  <span className={membresia.estado === "ACTIVA" ? "ok" : "error"}>{membresia.estado}</span>
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
                        <td className={p.estado === "APROBADO" ? "ok" : "error"}>{p.estado}</td>
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