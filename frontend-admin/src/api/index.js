import axios from 'axios'

const api = axios.create({
  baseURL: '/api',
  timeout: 10000
})

api.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  error => Promise.reject(error)
)

api.interceptors.response.use(
  response => response.data,
  error => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token')
      window.location.href = '/login'
    }
    return Promise.reject(error)
  }
)

// 用户管理 API
export const userApi = {
  sendCode: (email) => api.post('/user/sendCode', null, { params: { email } }),
  login: (email, code) => api.post('/user/login', null, { params: { email, code } }),
  list: (page = 1, pageSize = 10, email) => api.get('/admin/users', { params: { page, pageSize, email } }),
  detail: (id) => api.get(`/admin/users/${id}`),
  updatePoints: (userId, amount) => api.post(`/admin/users/${userId}/points`, null, { params: { amount } }),
  disable: (userId, status) => api.post(`/admin/users/${userId}/disable`, null, { params: { status } })
}

// 提示词管理 API
export const promptApi = {
  list: (page = 1, pageSize = 10) => api.get('/admin/prompts', { params: { page, pageSize } }),
  create: (data) => api.post('/admin/prompts', data),
  update: (id, data) => api.put(`/admin/prompts/${id}`, data),
  delete: (id) => api.delete(`/admin/prompts/${id}`),
  publish: (id, status) => api.post(`/admin/prompts/${id}/publish`, null, { params: { status } })
}

export const categoryApi = {
  list: (params = {}) => api.get('/admin/categories', { params }),
  create: (data) => api.post('/admin/categories', data),
  update: (id, data) => api.put(`/admin/categories/${id}`, data),
  delete: (id) => api.delete(`/admin/categories/${id}`)
}

// 图片审核 API
export const imageApi = {
  list: (page = 1, pageSize = 10) => api.get('/admin/images', { params: { page, pageSize } }),
  approve: (id) => api.post(`/admin/images/${id}/approve`),
  reject: (id, reason) => api.post(`/admin/images/${id}/reject`, null, { params: { reason } })
}

// 系统配置 API
export const configApi = {
  getAll: () => api.get('/admin/config'),
  update: (key, value) => api.put(`/admin/config/${key}`, { value })
}

// 积分日志 API
export const pointLogApi = {
  list: (page = 1, pageSize = 10, userId) => api.get('/admin/point-logs', { params: { page, pageSize, userId } })
}

// 管理后台汇总 API
export const adminApi = {
  stats: () => api.get('/admin/stats'),
  login: (email) => api.post('/admin/login', null, { params: { email } })
}

export default api
