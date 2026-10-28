// Author: Phan Tuan Thanh - CE190595
import axios from 'axios'
import { readSession } from '../auth/session'

const client = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api',
  timeout: 15000,
})

client.interceptors.request.use((config) => {
  const session = readSession()
  if (session && config.url !== '/auth/login') {
    config.headers.Authorization = `Bearer ${session.accessToken}`
  }
  return config
})

client.interceptors.response.use(
  (response) => response,
  (error) => {
    const session = readSession()
    // Ignore a late 401 from an older session after the user has signed in again.
    if (error.response?.status === 401 && session
        && error.config?.headers?.Authorization === `Bearer ${session.accessToken}`) {
      window.dispatchEvent(new Event('auth:expired'))
    }
    return Promise.reject(error)
  },
)

export default client
