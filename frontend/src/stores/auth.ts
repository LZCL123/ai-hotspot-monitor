import { defineStore } from 'pinia'
import { apiGet, apiPost } from '../api/client'

interface LoginResponse {
  token: string
  username: string
  role: string
}

interface UserInfo {
  username: string
  role: string
}

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    username: localStorage.getItem('username') || '',
    role: localStorage.getItem('role') || ''
  }),
  actions: {
    async login(username: string, password: string) {
      const data = await apiPost<LoginResponse>('/auth/login', { username, password })
      this.token = data.token
      this.username = data.username
      this.role = data.role
      this.persistSession(data)
    },
    async register(username: string, password: string) {
      const data = await apiPost<LoginResponse>('/auth/register', { username, password })
      this.token = data.token
      this.username = data.username
      this.role = data.role
      this.persistSession(data)
    },
    async fetchMe() {
      const data = await apiGet<UserInfo>('/auth/me')
      this.username = data.username
      this.role = data.role
      localStorage.setItem('username', data.username)
      localStorage.setItem('role', data.role)
    },
    async changePassword(oldPassword: string, newPassword: string) {
      await apiPost('/auth/change-password', { oldPassword, newPassword })
    },
    logout() {
      this.token = ''
      this.username = ''
      this.role = ''
      localStorage.removeItem('token')
      localStorage.removeItem('username')
      localStorage.removeItem('role')
    },
    persistSession(data: LoginResponse) {
      localStorage.setItem('token', data.token)
      localStorage.setItem('username', data.username)
      localStorage.setItem('role', data.role)
    }
  }
})
