import { useAuth } from "../AuthContext";

export default function Perfil() {
  const { usuario } = useAuth();

  return (
    <div className="contenedor">
      <h1>Mi perfil</h1>
      <div className="tarjeta">
        <p><strong>ID:</strong> {usuario.id}</p>
        <p><strong>Nombre:</strong> {usuario.nombre}</p>
        <p><strong>Correo:</strong> {usuario.email}</p>
        <p><strong>Rol:</strong> {usuario.rol}</p>
      </div>
      <p>Aquí aparecerán tu membresía y tu historial de pagos.</p>
    </div>
  );
}