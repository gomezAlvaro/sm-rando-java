import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: () => import('../views/HomeView.vue')
    },
    {
      path: '/generate',
      name: 'generate',
      component: () => import('../views/GenerateView.vue')
    },
    {
      path: '/seed/:id',
      name: 'seed-details',
      component: () => import('../views/SeedDetailsView.vue'),
      props: true
    }
  ]
})

export default router