import { Link, Navigate, Route, Routes } from "react-router-dom";
import { useAuth } from "./AuthContext";
import ProtectedRoute from "./components/ProtectedRoute";
import Login from "./pages/Login";
import Perfil from "./pages/Perfil";
import Caja from "./pages/Caja";
import Admin from "./pages/Admin";

function Encabezado() {
  const { usuario, cerrarSesion } = useAuth();

  return (
    <header className="encabezado">
      <span className="marca">GymFlow</span>
      <Link to="/caja">Caja</Link>
      {usuario?.rol === "ROLE_ADMIN" && <Link to="/admin">Admin</Link>}
      {usuario && <Link to="/perfil">Mi perfil</Link>}
      {usuario ? (
        <button onClick={cerrarSesion}>Salir</button>
      ) : (
        <Link to="/login">Entrar</Link>
      )}
    </header>
  );
}

// La ruta "/" manda a cada quien a su pantalla
function Inicio() {
  const { token, usuario, cargando } = useAuth();

  if (cargando) return <p className="contenedor">Cargando...</p>;
  if (!token || !usuario) return <Navigate to="/login" replace />;

  return <Navigate to={usuario.rol === "ROLE_ADMIN" ? "/admin" : "/perfil"} replace />;
}

export default function App() {
  return (
    <>
      <Encabezado />
      <Routes>
        <Route path="/" element={<Inicio />} />
        <Route path="/login" element={<Login />} />
        <Route path="/caja" element={<Caja />} />
        <Route
          path="/perfil"
          element={
            <ProtectedRoute>
              <Perfil />
            </ProtectedRoute>
          }
        />
        <Route
          path="/admin"
          element={
            <ProtectedRoute rol="ROLE_ADMIN">
              <Admin />
            </ProtectedRoute>
          }
        />
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </>
  );
}