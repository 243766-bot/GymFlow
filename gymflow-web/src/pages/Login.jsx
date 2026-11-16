import { useState } from "react";
import { Link, Navigate, useLocation } from "react-router-dom";
import { api } from "../api";
import { useAuth } from "../AuthContext";

export default function Login() {
  const { usuario, iniciarSesion } = useAuth();
  const location = useLocation();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [enviando, setEnviando] = useState(false);

  // Si ya hay sesión: regresa a la pantalla de donde venía (si venía de alguna) o a la de su rol
  if (usuario) {
    const origen = location.state?.from;
    const destino = origen
      ? `${origen.pathname}${origen.search}`
      : usuario.rol === "ROLE_ADMIN"
        ? "/admin"
        : "/perfil";
    return <Navigate to={destino} replace />;
  }

  async function enviar(e) {
    e.preventDefault();
    setError("");
    setEnviando(true);
    try {
      const respuesta = await api("/auth/login", {
        method: "POST",
        body: { email, password },
      });
      iniciarSesion(respuesta);
    } catch (err) {
      setError(err.message);
    } finally {
      setEnviando(false);
    }
  }

  return (
    <div className="contenedor">
      <h1>Iniciar sesión</h1>
      <form className="formulario" onSubmit={enviar}>
        <input
          type="email"
          placeholder="Correo"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
          required
        />
        <input
          type="password"
          placeholder="Contraseña"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          required
        />
        <button type="submit" disabled={enviando}>
          {enviando ? "Entrando..." : "Entrar"}
        </button>
        {error && <p className="error">{error}</p>}
      </form>
      <p>
        ¿Vienes al gimnasio? <Link to="/caja">Ir a la caja (acceso con rostro)</Link>
      </p>
    </div>
  );
}