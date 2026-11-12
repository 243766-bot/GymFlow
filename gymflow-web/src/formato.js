// Convierte "2026-11-27" en "27 de noviembre de 2026"
export function formatearFecha(iso) {
  if (!iso) return "";
  const [anio, mes, dia] = iso.split("-").map(Number);
  return new Date(anio, mes - 1, dia).toLocaleDateString("es-MX", {
    day: "numeric",
    month: "long",
    year: "numeric",
  });
}

// Convierte 695 en "$695.00"
export function formatearMoneda(valor) {
  return Number(valor).toLocaleString("es-MX", { style: "currency", currency: "MXN" });
}

// Convierte "2026-11-02T14:35:10" en "02:35 p. m."
export function formatearHora(iso) {
  if (!iso) return "";
  return new Date(iso).toLocaleTimeString("es-MX", { hour: "2-digit", minute: "2-digit" });
}

// Convierte "2026-11-02T14:35:10" en "2 nov 2026, 2:35 p. m."
export function formatearFechaHora(iso) {
  if (!iso) return "";
  return new Date(iso).toLocaleString("es-MX", { dateStyle: "medium", timeStyle: "short" });
}

// La fecha de hoy como "2026-11-02", lista para los campos de fecha y para la API
export function hoyISO() {
  const ahora = new Date();
  const mes = String(ahora.getMonth() + 1).padStart(2, "0");
  const dia = String(ahora.getDate()).padStart(2, "0");
  return `${ahora.getFullYear()}-${mes}-${dia}`;
}

// Convierte EFECTIVO o MERCADO_PAGO en un texto para mostrar
export function etiquetaMetodo(metodo) {
  if (metodo === "EFECTIVO") return "Efectivo";
  if (metodo === "MERCADO_PAGO") return "Mercado Pago";
  return metodo;
}