import { Link, Navigate, Route, Routes } from "react-router-dom";
import { useAuth } from "./AuthContext";
import ProtectedRoute from "./components/ProtectedRoute";
import Login from "./pages/Login";
import Perfil from "./pages/Perfil";
import Caja from "./pages/Caja";
import Admin from "./pages/Admin";
import Planes from "./pages/Planes";
import Productos from "./pages/Productos";
import Ventas from "./pages/Ventas";
import Corte from "./pages/Corte";

function Encabezado() {
  const { usuario, cerrarSesion } = useAuth();
  const esAdmin = usuario?.rol === "ROLE_ADMIN";

  return (
    <header className="encabezado">
      <span className="marca">GymFlow</span>
      <Link to="/caja">Acceso con rostro</Link>
      {esAdmin && (
        <>
          <Link to="/admin">Clientes</Link>
          <Link to="/planes">Planes</Link>
          <Link to="/productos">Productos</Link>
          <Link to="/ventas">Punto de venta</Link>
          <Link to="/corte">Corte del día</Link>
        </>
      )}
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
        <Route
          path="/planes"
          element={
            <ProtectedRoute rol="ROLE_ADMIN">
              <Planes />
            </ProtectedRoute>
          }
        />
        <Route
          path="/productos"
          element={
            <ProtectedRoute rol="ROLE_ADMIN">
              <Productos />
            </ProtectedRoute>
          }
        />
        <Route
          path="/ventas"
          element={
            <ProtectedRoute rol="ROLE_ADMIN">
              <Ventas />
            </ProtectedRoute>
          }
        />
        <Route
          path="/corte"
          element={
            <ProtectedRoute rol="ROLE_ADMIN">
              <Corte />
            </ProtectedRoute>
          }
        />
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </>
  );
}