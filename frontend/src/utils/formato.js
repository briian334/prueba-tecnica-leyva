const formateadorImporte = new Intl.NumberFormat('es-MX', {
  minimumFractionDigits: 2,
  maximumFractionDigits: 2,
})

const formateadorFecha = new Intl.DateTimeFormat('es-MX', {
  day: '2-digit',
  month: 'short',
  year: 'numeric',
  timeZone: 'UTC',
})

const formateadorFechaHora = new Intl.DateTimeFormat('es-MX', {
  dateStyle: 'medium',
  timeStyle: 'short',
})

const etiquetasCampo = {
  fecha: 'Fecha',
  referencia: 'Referencia',
  concepto: 'Concepto',
  cuenta_cargo: 'Cuenta de cargo',
  cuenta_abono: 'Cuenta de abono',
  importe: 'Importe',
}

export function formatearImporte(valor) {
  return formateadorImporte.format(Number(valor))
}

export function formatearFecha(valor) {
  if (!valor) return '—'

  const [anio, mes, dia] = valor.split('-').map(Number)
  return formateadorFecha.format(new Date(Date.UTC(anio, mes - 1, dia)))
}

export function formatearFechaHora(valor) {
  if (!valor) return '—'

  const [fecha, hora = '00:00:00'] = valor.split('T')
  const [anio, mes, dia] = fecha.split('-').map(Number)
  const [horas, minutos, segundos = 0] = hora.split(':').map(Number)
  return formateadorFechaHora.format(new Date(anio, mes - 1, dia, horas, minutos, segundos))
}

export function etiquetaCampoImportacion(campo) {
  if (!campo) return 'General'
  return etiquetasCampo[campo] || campo.replaceAll('_', ' ')
}
