import axios from 'axios'
import type { AxiosRequestConfig } from 'axios'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '../stores/auth'
import { simplifyApiData, toSimplified } from '../utils/zh'

export interface ApiResponse<T> {
  code: number
  message: string
  data: T
}

export const http = axios.create({
  // 本地开发时 Vite 会把 /api 代理到 8080 端口的 Spring Boot 后端。
  // Docker 部署时 nginx 也会把同样的 /api 路径转发到后端容器。
  baseURL: '/api',
  timeout: 15000
})

http.interceptors.request.use((config) => {
  const auth = useAuthStore()
  if (auth.token) {
    // 除登录接口外，后端 API 都需要这个 Bearer Token。
    config.headers.Authorization = `Bearer ${auth.token}`
  }
  return config
})

http.interceptors.response.use(
  (response) => {
    const body = response.data as ApiResponse<unknown>
    body.message = toSimplified(body.message || '')
    body.data = simplifyApiData(body.data)
    if (body.code !== 0) {
      if (body.code === 401) {
        const auth = useAuthStore()
        auth.logout()
        window.location.href = '/login'
      }
      ElMessage.error(body.message || '请求失败')
      return Promise.reject(new Error(body.message))
    }
    return response
  },
  (error) => {
    if (error.response?.status === 401 || error.response?.data?.code === 401) {
      const auth = useAuthStore()
      auth.logout()
      window.location.href = '/login'
    }
    ElMessage.error(toSimplified(error.response?.data?.message || error.message || '网络异常'))
    return Promise.reject(error)
  }
)

export async function apiGet<T>(url: string, params?: Record<string, unknown>) {
  const res = await http.get<ApiResponse<T>>(url, { params })
  return res.data.data
}

export async function apiPost<T>(url: string, data?: unknown, config?: AxiosRequestConfig) {
  const res = await http.post<ApiResponse<T>>(url, data, config)
  return res.data.data
}

export async function apiPut<T>(url: string, data?: unknown) {
  const res = await http.put<ApiResponse<T>>(url, data)
  return res.data.data
}

export async function apiDelete<T>(url: string) {
  const res = await http.delete<ApiResponse<T>>(url)
  return res.data.data
}
