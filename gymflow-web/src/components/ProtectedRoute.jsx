import { Navigate } from "react-router-dom";
import { useAuth } from "../AuthContext";

export default function ProtectedRoute({ children, rol }) {
  const { token, usuario, cargando } = useAuth();

  if (cargando) {
    return <p className="contenedor">Cargando...</p>;
  }

  if (!token || !usuario) {
    return <Navigate to="/login" replace />;
  }

  if (rol && usuario.rol !== rol) {
    return <p className="contenedor error">No tienes permiso para ver esta página.</p>;
  }

  return children;
}