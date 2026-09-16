<script setup>
defineProps({
  id: {
    type: String,
    required: true,
  },
})
</script>

<template>
  <section aria-labelledby="titulo-detalle">
    <h1 id="titulo-detalle" class="h3">Detalle de poliza</h1>
    <p class="text-body-secondary">
      Identificador: <span class="text-body fw-semibold">{{ id }}</span>
    </p>
  </section>
</template>
