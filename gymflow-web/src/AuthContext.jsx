import { createContext, useCallback, useContext, useEffect, useState } from "react";
import { api } from "./api";

const AuthContext = createContext(null);
const CLAVE_TOKEN = "gymflow_token";

export function AuthProvider({ children }) {
  const [token, setToken] = useState(() => localStorage.getItem(CLAVE_TOKEN));
  const [usuario, setUsuario] = useState(null);
  const [cargando, setCargando] = useState(Boolean(localStorage.getItem(CLAVE_TOKEN)));

  const cerrarSesion = useCallback(() => {
    localStorage.removeItem(CLAVE_TOKEN);
    setToken(null);
    setUsuario(null);
  }, []);

  const iniciarSesion = useCallback((auth) => {
    localStorage.setItem(CLAVE_TOKEN, auth.token);
    setToken(auth.token);
    setUsuario(auth.usuario);
    setCargando(false);
  }, []);

  // Al abrir la página con un token guardado, comprobamos que siga siendo válido
  useEffect(() => {
    if (!token || usuario) {
      setCargando(false);
      return;
    }

    let cancelado = false;
    api("/users/me", { token })
      .then((u) => {
        if (!cancelado) setUsuario(u);
      })
      .catch(() => {
        if (!cancelado) cerrarSesion();
      })
      .finally(() => {
        if (!cancelado) setCargando(false);
      });

    return () => {
      cancelado = true;
    };
  }, [token, usuario, cerrarSesion]);

  return (
    <AuthContext.Provider value={{ token, usuario, cargando, iniciarSesion, cerrarSesion }}>
      {children}
    </AuthContext.Provider>
  );
}

// eslint-disable-next-line react-refresh/only-export-components
export function useAuth() {
  return useContext(AuthContext);
}