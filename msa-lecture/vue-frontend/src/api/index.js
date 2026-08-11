import axios from 'axios'
import { useAuthStore } from '@/store/auth.js'

const api = axios.create({
  baseURL: '',
  timeout: 10000,
  headers: { 'Content-Type': 'application/json' }
})

api.interceptors.request.use((config) => {
  const auth = useAuthStore()
  if (auth.accessToken) {
    config.headers.Authorization = `Bearer ${auth.accessToken}`
  }
  return config
})

api.interceptors.response.use(
  (res) => res,
  (err) => {
    if (err.response?.status === 401) {
      console.error('[API] 401 Unauthorized')
      console.error('[API] response data =', err.response?.data)
      console.error('[API] request url =', err.config?.url)
      const auth = useAuthStore()

      // 기존 로그인 토큰이 거절된 경우에만 세션 만료로 처리한다.
      // 비로그인 상태의 공개 페이지 API 실패는 현재 화면에서 처리하도록 둔다.
      if (auth.accessToken) {
        auth.logout(false)

        if (window.location.pathname !== '/login') {
          window.location.href = '/login?expired=1'
        }
      }
    }
    return Promise.reject(err)
  }
)

export default api
