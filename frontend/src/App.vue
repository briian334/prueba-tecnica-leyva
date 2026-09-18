<script setup>
import { RouterLink, RouterView } from 'vue-router'
</script>

<template>
  <header class="app-header">
    <nav
      class="container-xxl d-flex flex-wrap align-items-center gap-3 px-3 px-lg-4"
      aria-label="Navegación principal"
    >
      <RouterLink class="marca-app me-auto" to="/polizas">
        <span class="marca-app__titulo">Gestión de pólizas</span>
        <span class="marca-app__subtitulo">Control contable</span>
      </RouterLink>

      <div class="nav app-nav">
        <RouterLink class="nav-link" active-class="active" to="/importar">
          Importar
        </RouterLink>
        <RouterLink class="nav-link" active-class="active" to="/polizas">
          Pólizas
        </RouterLink>
      </div>
    </nav>
  </header>

  <main class="container-xxl app-main px-3 px-lg-4">
    <RouterView />
  </main>
</template>
