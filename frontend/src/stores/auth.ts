import { defineStore } from 'pinia'
import { apiPost } from '../api/client'

interface LoginResponse {
  token: string
  username: string
  role: string
}

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    username: localStorage.getItem('username') || ''
  }),
  actions: {
    async login(username: string, password: string) {
      const data = await apiPost<LoginResponse>('/auth/login', { username, password })
      this.token = data.token
      this.username = data.username
      localStorage.setItem('token', data.token)
      localStorage.setItem('username', data.username)
    },
    logout() {
      this.token = ''
      this.username = ''
      localStorage.removeItem('token')
      localStorage.removeItem('username')
    }
  }
})
