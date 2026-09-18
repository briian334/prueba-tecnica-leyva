<script setup>
import { onBeforeUnmount, onMounted, ref } from 'vue'
import { RouterLink } from 'vue-router'

import EtiquetaEstatus from '../components/EtiquetaEstatus.vue'
import { listarPolizas } from '../services/api'
import { formatearFecha, formatearImporte } from '../utils/formato'

const estado = ref('loading')
const polizas = ref([])
let controlador = null

async function cargarPolizas() {
  controlador?.abort()
  const controladorActual = new AbortController()
  controlador = controladorActual
  estado.value = 'loading'

  try {
    polizas.value = await listarPolizas({ signal: controladorActual.signal })
    estado.value = polizas.value.length ? 'success' : 'empty'
  } catch (error) {
    if (error.name === 'AbortError') return
    estado.value = 'error'
  } finally {
    if (controlador === controladorActual) controlador = null
  }
}

onMounted(cargarPolizas)
onBeforeUnmount(() => controlador?.abort())
</script>

<template>
  <section aria-labelledby="titulo-polizas">
    <header class="cabecera-pagina">
      <div class="cabecera-pagina__texto">
        <span class="sobretitulo">Consulta</span>
        <h1 id="titulo-polizas" tabindex="-1">Pólizas</h1>
        <p>Consulta las pólizas generadas a partir de archivos de operaciones.</p>
      </div>
      <RouterLink class="btn btn-primary" to="/importar">Importar archivo</RouterLink>
    </header>

    <div v-if="estado === 'loading'" class="estado-pagina" role="status" aria-live="polite">
      <span class="spinner-border mb-3" aria-hidden="true"></span>
      <span>Cargando pólizas…</span>
    </div>

    <div v-else-if="estado === 'error'" class="estado-pagina" role="alert">
      <h2>No se pudieron cargar las pólizas</h2>
      <p>Revisa la conexión con el servidor y vuelve a intentarlo.</p>
      <button type="button" class="btn btn-outline-secondary" @click="cargarPolizas">
        Reintentar
      </button>
    </div>

    <div v-else-if="estado === 'empty'" class="estado-pagina">
      <h2>Aún no hay pólizas</h2>
      <p>Importa un archivo TXT o XLSX para generar la primera póliza.</p>
      <RouterLink class="btn btn-primary" to="/importar">Importar archivo</RouterLink>
    </div>

    <div
      v-else
      class="tabla-contenedor"
      role="region"
      aria-labelledby="titulo-polizas"
      tabindex="0"
    >
      <table class="table tabla-contable">
        <caption class="visually-hidden">Pólizas generadas por importación</caption>
        <thead>
          <tr>
            <th scope="col">ID</th>
            <th scope="col">Fecha</th>
            <th scope="col">Concepto</th>
            <th scope="col">Archivo origen</th>
            <th scope="col" class="text-end">Total debe</th>
            <th scope="col" class="text-end">Total haber</th>
            <th scope="col">Estatus</th>
            <th scope="col" aria-label="Acción"></th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="poliza in polizas" :key="poliza.id">
            <th scope="row" class="identificador">#{{ poliza.id }}</th>
            <td>{{ formatearFecha(poliza.fecha) }}</td>
            <td>{{ poliza.concepto }}</td>
            <td class="texto-secundario">{{ poliza.archivoOrigen }}</td>
            <td class="columna-importe">{{ formatearImporte(poliza.totalDebe) }}</td>
            <td class="columna-importe">{{ formatearImporte(poliza.totalHaber) }}</td>
            <td><EtiquetaEstatus :valor="poliza.estatus" /></td>
            <td class="text-end">
              <RouterLink
                class="enlace-accion"
                :to="{ name: 'poliza-detalle', params: { id: poliza.id } }"
              >
                Ver detalle
              </RouterLink>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </section>
</template>
