import { useState } from "react";
import { Link, Navigate } from "react-router-dom";
import { api } from "../api";
import { useAuth } from "../AuthContext";

export default function Login() {
  const { usuario, iniciarSesion } = useAuth();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [enviando, setEnviando] = useState(false);

  // Si ya hay sesión, mandamos a cada rol a su pantalla
  if (usuario) {
    return <Navigate to={usuario.rol === "ROLE_ADMIN" ? "/admin" : "/perfil"} replace />;
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