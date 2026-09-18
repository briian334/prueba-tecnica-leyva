<script setup>
import { nextTick, onBeforeUnmount, ref, watch } from 'vue'
import { RouterLink } from 'vue-router'

import EtiquetaEstatus from '../components/EtiquetaEstatus.vue'
import { ErrorApi, obtenerPoliza } from '../services/api'
import { formatearFecha, formatearFechaHora, formatearImporte } from '../utils/formato'

const props = defineProps({
  id: {
    type: String,
    required: true,
  },
})

const estado = ref('loading')
const poliza = ref(null)
let controlador = null

async function cargarPoliza() {
  controlador?.abort()
  const controladorActual = new AbortController()
  controlador = controladorActual
  estado.value = 'loading'
  poliza.value = null

  try {
    poliza.value = await obtenerPoliza(props.id, { signal: controladorActual.signal })
    estado.value = 'success'
    document.title = `Póliza #${poliza.value.id} — Gestión de pólizas`
  } catch (error) {
    if (error.name === 'AbortError') return
    estado.value = error instanceof ErrorApi && error.estatus === 404 ? 'notFound' : 'error'
    document.title = `${estado.value === 'notFound' ? 'Póliza no encontrada' : 'Error'} — Gestión de pólizas`
  } finally {
    if (controlador === controladorActual) controlador = null
  }

  await nextTick()
  document.querySelector('#titulo-detalle')?.focus()
}

watch(() => props.id, cargarPoliza, { immediate: true })
onBeforeUnmount(() => controlador?.abort())
</script>

<template>
  <section aria-labelledby="titulo-detalle">
    <RouterLink class="detalle-navegacion" to="/polizas">Volver a pólizas</RouterLink>

    <div v-if="estado === 'loading'" class="estado-pagina" role="status" aria-live="polite">
      <h1 id="titulo-detalle" class="visually-hidden" tabindex="-1">Detalle de póliza</h1>
      <span class="spinner-border mb-3" aria-hidden="true"></span>
      <span>Cargando detalle de la póliza…</span>
    </div>

    <div v-else-if="estado === 'notFound'" class="estado-pagina">
      <h1 id="titulo-detalle" tabindex="-1">Póliza no encontrada</h1>
      <p>La póliza solicitada no existe o ya no está disponible.</p>
      <RouterLink class="btn btn-outline-secondary" to="/polizas">Ir al listado</RouterLink>
    </div>

    <div v-else-if="estado === 'error'" class="estado-pagina" role="alert">
      <h1 id="titulo-detalle" tabindex="-1">No se pudo cargar la póliza</h1>
      <p>Revisa la conexión con el servidor y vuelve a intentarlo.</p>
      <button type="button" class="btn btn-outline-secondary" @click="cargarPoliza">
        Reintentar
      </button>
    </div>

    <template v-else>
      <header class="cabecera-pagina">
        <div class="cabecera-pagina__texto">
          <span class="sobretitulo">Detalle contable</span>
          <h1 id="titulo-detalle" tabindex="-1">Póliza #{{ poliza.id }}</h1>
          <p>{{ poliza.concepto }}</p>
        </div>
        <EtiquetaEstatus :valor="poliza.estatus" />
      </header>

      <dl class="metadatos-poliza">
        <div>
          <dt>Fecha</dt>
          <dd>{{ formatearFecha(poliza.fecha) }}</dd>
        </div>
        <div>
          <dt>Fecha de creación</dt>
          <dd>{{ formatearFechaHora(poliza.fechaCreacion) }}</dd>
        </div>
        <div>
          <dt>Archivo origen</dt>
          <dd>{{ poliza.archivoOrigen }}</dd>
        </div>
      </dl>

      <div class="banda-balance" aria-label="Totales de la póliza">
        <div class="banda-balance__dato">
          <span class="banda-balance__etiqueta">Debe</span>
          <span class="banda-balance__importe">{{ formatearImporte(poliza.totalDebe) }}</span>
        </div>
        <div class="banda-balance__dato">
          <span class="banda-balance__etiqueta">Haber</span>
          <span class="banda-balance__importe">{{ formatearImporte(poliza.totalHaber) }}</span>
        </div>
      </div>

      <h2 class="seccion-titulo">Movimientos</h2>
      <div
        class="tabla-contenedor"
        role="region"
        aria-label="Movimientos de la póliza"
        tabindex="0"
      >
        <table class="table tabla-contable">
          <caption class="visually-hidden">Movimientos contables de la póliza #{{ poliza.id }}</caption>
          <thead>
            <tr>
              <th scope="col">Cuenta</th>
              <th scope="col">Referencia</th>
              <th scope="col">Concepto</th>
              <th scope="col" class="text-end">Debe</th>
              <th scope="col" class="text-end">Haber</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="movimiento in poliza.movimientos" :key="movimiento.id">
              <th scope="row">{{ movimiento.cuenta }}</th>
              <td class="identificador">{{ movimiento.referencia }}</td>
              <td>{{ movimiento.concepto }}</td>
              <td class="columna-importe">{{ formatearImporte(movimiento.debe) }}</td>
              <td class="columna-importe">{{ formatearImporte(movimiento.haber) }}</td>
            </tr>
          </tbody>
          <tfoot>
            <tr>
              <th scope="row" colspan="3">Totales</th>
              <td class="columna-importe">{{ formatearImporte(poliza.totalDebe) }}</td>
              <td class="columna-importe">{{ formatearImporte(poliza.totalHaber) }}</td>
            </tr>
          </tfoot>
        </table>
      </div>
    </template>
  </section>
</template>
