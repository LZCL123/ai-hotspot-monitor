import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import LoginView from '../views/LoginView.vue'
import DashboardView from '../views/DashboardView.vue'
import SubscriptionsView from '../views/SubscriptionsView.vue'
import HotspotsView from '../views/HotspotsView.vue'
import HotspotDetailView from '../views/HotspotDetailView.vue'
import AdminView from '../views/AdminView.vue'
import AgentView from '../views/AgentView.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/login', component: LoginView },
    { path: '/', component: DashboardView },
    { path: '/subscriptions', component: SubscriptionsView },
    { path: '/hotspots', component: HotspotsView },
    { path: '/hotspots/:id', component: HotspotDetailView },
    { path: '/agent', component: AgentView },
    { path: '/admin', component: AdminView }
  ]
})

router.beforeEach((to) => {
  const auth = useAuthStore()
  if (to.path !== '/login' && !auth.token) {
    return '/login'
  }
})

export default router
