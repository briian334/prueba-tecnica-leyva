<script setup>
import { computed } from 'vue'

const props = defineProps({
  valor: {
    type: String,
    required: true,
  },
})

const texto = computed(() => props.valor.replaceAll('_', ' '))
</script>

<template>
  <span class="etiqueta-estatus">
    <span class="etiqueta-estatus__marca" aria-hidden="true"></span>
    {{ texto }}
  </span>
</template>
