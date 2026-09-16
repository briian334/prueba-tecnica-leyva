import { createRouter, createWebHistory } from 'vue-router'

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
    },
    {
      path: '/polizas',
      name: 'polizas',
      component: () => import('../views/PolizasView.vue'),
    },
    {
      path: '/polizas/:id',
      name: 'poliza-detalle',
      component: () => import('../views/PolizaDetalleView.vue'),
      props: true,
    },
  ],
})

export default router
