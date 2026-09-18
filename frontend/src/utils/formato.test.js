import test from 'node:test'
import assert from 'node:assert/strict'

import {
  etiquetaCampoImportacion,
  formatearFecha,
  formatearFechaHora,
  formatearImporte,
} from './formato.js'

test('formatea importes con separador de miles y dos decimales sin asumir moneda', () => {
  assert.equal(formatearImporte(1234.5), '1,234.50')
  assert.equal(formatearImporte('0.00'), '0.00')
})

test('formatea fechas de dominio sin desplazarlas por zona horaria', () => {
  assert.equal(formatearFecha('2026-09-16'), '16 sep 2026')
})

test('formatea fecha y hora local sin interpretar una zona inexistente', () => {
  assert.equal(formatearFechaHora('2026-09-16T18:30:45'), '16 sep 2026, 6:30 p.m.')
})

test('presenta nombres legibles para los campos de importacion', () => {
  assert.equal(etiquetaCampoImportacion('cuenta_cargo'), 'Cuenta de cargo')
  assert.equal(etiquetaCampoImportacion('cuenta_abono'), 'Cuenta de abono')
  assert.equal(etiquetaCampoImportacion(null), 'General')
})
