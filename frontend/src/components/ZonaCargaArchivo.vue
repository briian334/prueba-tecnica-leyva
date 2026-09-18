<script setup>
import { ref } from 'vue'

import { formatearTamanoArchivo } from '../utils/archivo'

defineProps({
  archivo: {
    type: File,
    default: null,
  },
  deshabilitado: {
    type: Boolean,
    default: false,
  },
})

const emit = defineEmits(['seleccionar', 'error', 'quitar'])

const inputArchivo = ref(null)
const arrastrando = ref(false)

function abrirSelector() {
  inputArchivo.value?.click()
}

function procesarArchivos(listaArchivos) {
  const archivos = Array.from(listaArchivos || [])

  if (archivos.length !== 1) {
    emit('error', 'Selecciona exactamente un archivo TXT o XLSX.')
    return
  }

  emit('seleccionar', archivos[0])
}

function seleccionarDesdeInput(evento) {
  procesarArchivos(evento.target.files)
  evento.target.value = ''
}

function soltar(evento) {
  arrastrando.value = false
  procesarArchivos(evento.dataTransfer.files)
}
</script>

<template>
  <div class="zona-carga">
    <input
      ref="inputArchivo"
      class="visually-hidden"
      type="file"
      tabindex="-1"
      accept=".txt,.xlsx"
      :disabled="deshabilitado"
      aria-label="Seleccionar archivo TXT o XLSX"
      aria-describedby="formatos-archivo"
      @change="seleccionarDesdeInput"
    />

    <button
      type="button"
      class="zona-carga__objetivo"
      :class="{ 'zona-carga__objetivo--activo': arrastrando }"
      :disabled="deshabilitado"
      aria-describedby="formatos-archivo"
      @click="abrirSelector"
      @dragenter.prevent="arrastrando = true"
      @dragover.prevent="arrastrando = true"
      @dragleave.self="arrastrando = false"
      @drop.prevent="soltar"
    >
      <svg class="zona-carga__icono" viewBox="0 0 24 24" aria-hidden="true">
        <path d="M7 3.75h7l3 3v13.5H7z" />
        <path d="M14 3.75v3h3M12 16v-6m-2.25 2.25L12 10l2.25 2.25" />
      </svg>
      <span class="zona-carga__titulo">
        {{ archivo ? 'Arrastra otro archivo para reemplazarlo' : 'Arrastra el archivo aquí' }}
      </span>
      <span class="zona-carga__accion">
        o haz click para {{ archivo ? 'cambiarlo' : 'seleccionarlo' }}
      </span>
      <span id="formatos-archivo" class="zona-carga__formatos">Formatos admitidos: TXT · XLSX</span>
    </button>

    <div v-if="archivo" class="archivo-seleccionado" aria-live="polite">
      <div class="archivo-seleccionado__datos">
        <span class="archivo-seleccionado__tipo" aria-hidden="true">
          {{ archivo.name.split('.').pop()?.toUpperCase() }}
        </span>
        <span class="archivo-seleccionado__nombre">{{ archivo.name }}</span>
        <span class="archivo-seleccionado__tamano">
          {{ formatearTamanoArchivo(archivo.size) }}
        </span>
      </div>
      <div class="archivo-seleccionado__acciones">
        <button
          type="button"
          class="btn btn-sm btn-outline-secondary"
          :disabled="deshabilitado"
          @click="abrirSelector"
        >
          Cambiar
        </button>
        <button
          type="button"
          class="btn btn-sm btn-link text-danger"
          :disabled="deshabilitado"
          @click="emit('quitar')"
        >
          Quitar
        </button>
      </div>
    </div>
  </div>
</template>
