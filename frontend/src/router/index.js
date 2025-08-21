import { createRouter, createWebHistory } from 'vue-router'
import Home from '../views/Home.vue'
import ModernView from '../views/ModernView.vue'
import MinimalView from '../views/MinimalView.vue'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: Home
  },
  {
    path: '/modern',
    name: 'Modern',
    component: ModernView
  },
  {
    path: '/minimal',
    name: 'Minimal',
    component: MinimalView
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router