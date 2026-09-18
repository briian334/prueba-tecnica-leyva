export class ErrorApi extends Error {
  constructor(estatus, datos) {
    super(datos?.mensaje || 'No se pudo completar la solicitud.')
    this.name = 'ErrorApi'
    this.estatus = estatus
    this.datos = datos
  }
}

async function solicitarJson(url, opciones = {}) {
  const respuesta = await fetch(url, opciones)
  const datos = await respuesta.json()

  if (!respuesta.ok) {
    throw new ErrorApi(respuesta.status, datos)
  }

  return datos
}

export function importarArchivo(archivo, { signal } = {}) {
  const cuerpo = new FormData()
  cuerpo.append('archivo', archivo)

  return solicitarJson('/api/importaciones', {
    method: 'POST',
    body: cuerpo,
    signal,
  })
}

export function listarPolizas({ signal } = {}) {
  return solicitarJson('/api/polizas', { signal })
}

export function obtenerPoliza(id, { signal } = {}) {
  return solicitarJson(`/api/polizas/${encodeURIComponent(id)}`, { signal })
}
