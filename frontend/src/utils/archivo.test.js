import test from 'node:test'
import assert from 'node:assert/strict'

import { formatearTamanoArchivo, validarArchivoImportacion } from './archivo.js'

test('acepta archivos TXT y XLSX sin depender de mayusculas en la extension', () => {
  assert.equal(validarArchivoImportacion({ name: 'operaciones.TXT', size: 20 }), null)
  assert.equal(validarArchivoImportacion({ name: 'operaciones.xlsx', size: 20 }), null)
})

test('rechaza archivos vacios o con una extension no admitida', () => {
  assert.equal(validarArchivoImportacion({ name: 'operaciones.txt', size: 0 }), 'El archivo está vacío.')
  assert.equal(
    validarArchivoImportacion({ name: 'operaciones.csv', size: 20 }),
    'El formato no es compatible. Selecciona un archivo TXT o XLSX.',
  )
})

test('formatea el tamano seleccionado de forma compacta', () => {
  assert.equal(formatearTamanoArchivo(1024), '1 KB')
  assert.equal(formatearTamanoArchivo(1536), '1.5 KB')
})
