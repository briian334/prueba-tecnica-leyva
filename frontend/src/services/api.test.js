import test from 'node:test'
import assert from 'node:assert/strict'

import { ErrorApi, importarArchivo, listarPolizas, obtenerPoliza } from './api.js'

function respuestaJson(datos, estatus = 200) {
  return new Response(JSON.stringify(datos), {
    status: estatus,
    headers: { 'Content-Type': 'application/json' },
  })
}

test('lista polizas usando el contrato resumido', async (t) => {
  const polizas = [
    {
      id: 15,
      fecha: '2026-09-16',
      concepto: 'Importación de operaciones',
      archivoOrigen: 'operaciones.xlsx',
      totalDebe: 250.75,
      totalHaber: 250.75,
      estatus: 'GENERADA',
    },
  ]
  t.mock.method(globalThis, 'fetch', async (url, opciones) => {
    assert.equal(url, '/api/polizas')
    assert.equal(opciones?.signal, 'senal-listado')
    return respuestaJson(polizas)
  })

  assert.deepEqual(await listarPolizas({ signal: 'senal-listado' }), polizas)
})

test('envia exactamente un archivo en el campo multipart acordado', async (t) => {
  const resultado = {
    idPoliza: 21,
    registrosProcesados: 3,
    totalDebe: 15000.5,
    totalHaber: 15000.5,
    estatus: 'GENERADA',
  }
  const archivo = new File(['contenido'], 'operaciones.txt', { type: 'text/plain' })
  t.mock.method(globalThis, 'fetch', async (url, opciones) => {
    assert.equal(url, '/api/importaciones')
    assert.equal(opciones.method, 'POST')
    assert.equal(opciones.body.get('archivo'), archivo)
    return respuestaJson(resultado, 201)
  })

  assert.deepEqual(await importarArchivo(archivo), resultado)
})

test('conserva estatus y cuerpo estructurado cuando la API responde un error', async (t) => {
  const error = {
    mensaje: 'No se encontró la póliza con id 99',
    errores: [],
  }
  t.mock.method(globalThis, 'fetch', async () => respuestaJson(error, 404))

  await assert.rejects(
    obtenerPoliza(99),
    (excepcion) =>
      excepcion instanceof ErrorApi &&
      excepcion.estatus === 404 &&
      excepcion.datos.mensaje === error.mensaje,
  )
})
