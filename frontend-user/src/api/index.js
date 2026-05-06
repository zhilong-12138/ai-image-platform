import axios from 'axios'

const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 30000,
  headers: { 'Content-Type': 'application/json' },
})

// 请求拦截器：注入 JWT token
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

// 响应拦截器：处理错误
api.interceptors.response.use(
  (response) => {
    const res = response.data
    // 如果后端返回 code !== 200，当作错误处理
    if (res.code !== undefined && res.code !== 200) {
      return Promise.reject(new Error(res.msg || '请求失败'))
    }
    // 返回内层 data，让调用方直接拿到 {records, total, ...}
    return res.data
  },
  (error) => {
    if (error.response) {
      const { status, data } = error.response
      if (status === 401) {
        // token 过期，清除并跳转登录
        localStorage.removeItem('token')
        localStorage.removeItem('user')
        window.dispatchEvent(new CustomEvent('auth:logout'))
      }
      const msg = data?.msg || error.message || '网络错误'
      return Promise.reject(new Error(msg))
    }
    return Promise.reject(error)
  }
)

export default api