<script setup>
import { computed, onBeforeUnmount, ref } from 'vue'
import { RouterLink } from 'vue-router'

import EtiquetaEstatus from '../components/EtiquetaEstatus.vue'
import ZonaCargaArchivo from '../components/ZonaCargaArchivo.vue'
import { ErrorApi, importarArchivo } from '../services/api'
import { validarArchivoImportacion } from '../utils/archivo'
import { etiquetaCampoImportacion, formatearImporte } from '../utils/formato'

const archivo = ref(null)
const estado = ref('idle')
const errorSeleccion = ref('')
const errorRespuesta = ref(null)
const estatusError = ref(null)
const resultado = ref(null)
let controlador = null

const esErrorValidacion = computed(() => estatusError.value === 400)

const erroresGlobales = computed(
  () => errorRespuesta.value?.errores?.filter((error) => error.fila == null) || [],
)

const erroresPorFila = computed(
  () => errorRespuesta.value?.errores?.filter((error) => error.fila != null) || [],
)

function limpiarResultado() {
  estado.value = 'idle'
  errorRespuesta.value = null
  estatusError.value = null
  resultado.value = null
}

function seleccionarArchivo(nuevoArchivo) {
  const error = validarArchivoImportacion(nuevoArchivo)
  if (error) {
    errorSeleccion.value = error
    return
  }

  archivo.value = nuevoArchivo
  errorSeleccion.value = ''
  limpiarResultado()
}

function quitarArchivo() {
  archivo.value = null
  errorSeleccion.value = ''
  limpiarResultado()
}

function mostrarErrorSeleccion(mensaje) {
  errorSeleccion.value = mensaje
}

async function importar() {
  const error = validarArchivoImportacion(archivo.value)
  if (error) {
    errorSeleccion.value = error
    return
  }

  estado.value = 'loading'
  errorSeleccion.value = ''
  errorRespuesta.value = null
  estatusError.value = null
  resultado.value = null
  controlador = new AbortController()

  try {
    resultado.value = await importarArchivo(archivo.value, { signal: controlador.signal })
    estado.value = 'success'
  } catch (errorApi) {
    if (errorApi.name === 'AbortError') return

    estatusError.value = errorApi instanceof ErrorApi ? errorApi.estatus : null
    errorRespuesta.value =
      errorApi instanceof ErrorApi
        ? errorApi.datos
        : {
            mensaje: 'No fue posible conectar con el servidor.',
            errores: [],
          }
    estado.value = 'error'
  } finally {
    controlador = null
  }
}

function prepararOtraImportacion() {
  quitarArchivo()
  document.querySelector('.zona-carga__objetivo')?.focus()
}

onBeforeUnmount(() => controlador?.abort())
</script>

<template>
  <section aria-labelledby="titulo-importacion">
    <header class="cabecera-pagina">
      <div class="cabecera-pagina__texto">
        <span class="sobretitulo">Nueva póliza</span>
        <h1 id="titulo-importacion" tabindex="-1">Importar operaciones</h1>
        <p>Selecciona un archivo de operaciones para validarlo y generar una póliza contable.</p>
      </div>
    </header>

    <ZonaCargaArchivo
      :archivo="archivo"
      :deshabilitado="estado === 'loading'"
      @seleccionar="seleccionarArchivo"
      @error="mostrarErrorSeleccion"
      @quitar="quitarArchivo"
    />

    <div v-if="errorSeleccion" class="alerta-contenido" role="alert">
      <h2>Revisa el archivo</h2>
      <p>{{ errorSeleccion }}</p>
    </div>

    <div class="barra-accion">
      <button
        type="button"
        class="btn btn-primary px-4"
        :disabled="!archivo || estado === 'loading'"
        :aria-busy="estado === 'loading'"
        @click="importar"
      >
        <span
          v-if="estado === 'loading'"
          class="spinner-border spinner-border-sm me-2"
          aria-hidden="true"
        ></span>
        {{ estado === 'loading' ? 'Importando…' : 'Importar archivo' }}
      </button>
    </div>

    <div v-if="estado === 'loading'" class="visually-hidden" role="status" aria-live="polite">
      El archivo se está importando.
    </div>

    <section
      v-if="estado === 'success'"
      class="resumen-exito"
      aria-labelledby="titulo-exito"
      aria-live="polite"
    >
      <div class="resumen-exito__cabecera">
        <div>
          <h2 id="titulo-exito">Póliza generada</h2>
          <p>
            Se procesaron {{ resultado.registrosProcesados }} registros del archivo
            {{ archivo.name }}.
          </p>
        </div>
        <EtiquetaEstatus :valor="resultado.estatus" />
      </div>

      <div class="banda-balance" aria-label="Totales de la póliza">
        <div class="banda-balance__dato">
          <span class="banda-balance__etiqueta">Debe</span>
          <span class="banda-balance__importe">{{ formatearImporte(resultado.totalDebe) }}</span>
        </div>
        <div class="banda-balance__dato">
          <span class="banda-balance__etiqueta">Haber</span>
          <span class="banda-balance__importe">{{ formatearImporte(resultado.totalHaber) }}</span>
        </div>
      </div>

      <div class="d-flex flex-wrap align-items-center gap-3 mt-4">
        <RouterLink
          class="btn btn-primary"
          :to="{ name: 'poliza-detalle', params: { id: resultado.idPoliza } }"
        >
          Ver póliza #{{ resultado.idPoliza }}
        </RouterLink>
        <button type="button" class="btn btn-outline-secondary" @click="prepararOtraImportacion">
          Importar otro archivo
        </button>
      </div>
    </section>

    <section
      v-if="estado === 'error'"
      class="alerta-contenido"
      aria-labelledby="titulo-error-importacion"
      role="alert"
    >
      <h2 id="titulo-error-importacion">No se pudo importar el archivo</h2>
      <p>{{ errorRespuesta.mensaje }}</p>
      <p v-if="esErrorValidacion" class="small">
        Corrige el archivo y vuelve a seleccionarlo.
      </p>
      <ul v-if="erroresGlobales.length">
        <li v-for="(error, indice) in erroresGlobales" :key="indice">
          {{ error.mensaje }}
        </li>
      </ul>
      <button
        v-if="!esErrorValidacion"
        type="button"
        class="btn btn-outline-secondary btn-sm mt-3"
        @click="importar"
      >
        Reintentar
      </button>
    </section>

    <section v-if="estado === 'error' && erroresPorFila.length" class="errores-tabla">
      <h2 class="seccion-titulo">Errores por fila</h2>
      <div
        class="tabla-contenedor"
        role="region"
        aria-label="Errores encontrados en el archivo"
        tabindex="0"
      >
        <table class="table tabla-contable">
          <caption class="visually-hidden">
            Detalle de errores de validación por fila y campo
          </caption>
          <thead>
            <tr>
              <th scope="col">Fila</th>
              <th scope="col">Campo</th>
              <th scope="col">Detalle</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(error, indice) in erroresPorFila" :key="`${error.fila}-${error.campo}-${indice}`">
              <td class="identificador">{{ error.fila }}</td>
              <td>{{ etiquetaCampoImportacion(error.campo) }}</td>
              <td>{{ error.mensaje }}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </section>
  </section>
</template>
