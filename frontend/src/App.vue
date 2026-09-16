<script setup>
import { RouterLink, RouterView } from 'vue-router'
</script>

<template>
  <header class="border-bottom bg-body-tertiary">
    <nav
      class="container d-flex flex-wrap align-items-center gap-3 py-3"
      aria-label="Navegacion principal"
    >
      <RouterLink class="me-auto fw-semibold text-body text-decoration-none" to="/polizas">
        Gestion de polizas
      </RouterLink>

      <div class="nav nav-underline">
        <RouterLink class="nav-link" active-class="active" to="/importar">
          Importar
        </RouterLink>
        <RouterLink class="nav-link" active-class="active" to="/polizas">
          Polizas
        </RouterLink>
      </div>
    </nav>
  </header>

  <main class="container py-4">
    <RouterView />
  </main>
</template>
