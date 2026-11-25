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

// Mismas terminaciones que acepta el servidor (ver Validaciones.java)
const TERMINACIONES =
  "com\\.mx|org\\.mx|net\\.mx|edu\\.mx|gob\\.mx|com\\.ar|com\\.co|com\\.br|com\\.pe|com\\.es" +
  "|co\\.uk|org\\.uk|com\\.au|com|net|org|edu|gob|gov|info|biz|io|co|mx|us|es|ar|cl|pe|br|ca|uk|de|fr|it|pt";
const REGEX_CORREO = new RegExp(
  "^(?=[^@]{1,64}@)[a-z0-9]+([._%+-][a-z0-9]+)*@(?!(?:com|net|org|edu|gob|gov|co)\\.)[a-z0-9]([a-z0-9-]{0,61}[a-z0-9])?\\.(" + TERMINACIONES + ")$",
  "i"
);

// true si el correo tiene un dominio con terminacion real (gmail.com, upchiapas.edu.mx...)
export function correoValido(correo) {
  return REGEX_CORREO.test(correo.trim());
}

// Deja solo digitos y un punto decimal (maximo "decimales" decimales y 8 enteros). "1e5" -> "15"
export function soloMonto(valor, decimales = 2) {
  let limpio = valor.replace(/[^0-9.]/g, "");
  const [enteros, ...resto] = limpio.split(".");
  limpio = enteros.slice(0, 8);
  if (resto.length > 0 && decimales > 0) {
    limpio += "." + resto.join("").slice(0, decimales);
  }
  return limpio;
}

// Para campos type="number": evita escribir e, E, + y - (y el punto si el campo es entero)
export function bloquearNoNumericas(e, entero = false) {
  const prohibidas = ["e", "E", "+", "-"];
  if (entero) prohibidas.push(".", ",");
  if (prohibidas.includes(e.key)) e.preventDefault();
}
