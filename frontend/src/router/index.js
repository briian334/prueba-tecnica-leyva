import { createRouter, createWebHistory } from 'vue-router'
import { nextTick } from 'vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      redirect: '/polizas',
    },
    {
      path: '/importar',
      name: 'importacion',
      component: () => import('../views/ImportacionView.vue'),
      meta: { titulo: 'Importar archivo' },
    },
    {
      path: '/polizas',
      name: 'polizas',
      component: () => import('../views/PolizasView.vue'),
      meta: { titulo: 'Pólizas' },
    },
    {
      path: '/polizas/:id',
      name: 'poliza-detalle',
      component: () => import('../views/PolizaDetalleView.vue'),
      props: true,
      meta: { titulo: 'Detalle de póliza' },
    },
    {
      path: '/:pathMatch(.*)*',
      name: 'no-encontrado',
      component: () => import('../views/NoEncontradoView.vue'),
      meta: { titulo: 'Página no encontrada' },
    },
  ],
})

router.afterEach(async (to) => {
  document.title = `${to.meta.titulo || 'Gestión contable'} — Gestión de pólizas`
  await nextTick()
  document.querySelector('main h1')?.focus()
})

export default router
