<script setup>
import { RouterLink } from 'vue-router'
</script>

<template>
  <section aria-labelledby="titulo-no-encontrado">
    <div class="estado-pagina">
      <h1 id="titulo-no-encontrado" class="h2" tabindex="-1">Página no encontrada</h1>
      <p>La ruta que intentas consultar no existe.</p>
      <RouterLink class="btn btn-outline-secondary" to="/polizas">
        Volver a pólizas
      </RouterLink>
    </div>
  </section>
</template>
