import api from '../api/index.js'

// 提示词
export const promptApi = {
  list: (params) => api.get('/prompt/list', { params }),
  detail: (id) => api.get(`/prompt/detail/${id}`),
}

// 收藏
export const favApi = {
  // 列表（需要登录）
  list: () => api.get('/fav/favorites'),
  // 添加收藏
  add: (promptId) => api.post('/fav/favorite', null, { params: { promptId } }),
  // 取消收藏
  remove: (promptId) => api.delete(`/fav/favorite/${promptId}`),
  // 检查是否收藏
  check: (promptId) => api.get('/fav/favorite/check', { params: { promptId } }),
}

// 分类
export const categoryApi = {
  list: () => api.get('/category/list'),
}

// 生成记录
export const generationApi = {
  list: (params) => api.get('/generation/list', { params }),
  count: () => api.get('/generation/count'),
  submit: (data) => api.post('/generation/submit', data),
  status: (id) => api.get(`/generation/status/${id}`),
  retry: (id) => api.post(`/generation/retry/${id}`),
}

// 用户
export const userApi = {
  info: () => api.get('/user/info'),
}

// 认证
export const authApi = {
  // 发送登录验证码
  sendCode: (email) => api.post('/user/sendCode', null, { params: { email } }),
  // 登录 (邮箱 + 密码，密码 Base64 编码)
  login: (email, password) => api.post('/user/login', null, { params: { email, password: btoa(password) } }),
  // 注册 (邮箱 + 验证码 + 密码 Base64 编码 + 邀请码)
  register: (email, code, password, inviteCode) =>
    api.post('/user/register', null, { params: { email, code, password: btoa(password), inviteCode } }),
  // 重置密码 - 发送验证码
  resetPasswordSendCode: (email) => api.post('/user/resetPassword/sendCode', null, { params: { email } }),
  // 重置密码 (邮箱 + 验证码 + 新密码 Base64 编码)
  resetPassword: (email, code, password) =>
    api.post('/user/resetPassword', null, { params: { email, code, password: btoa(password) } }),
}

// 文件上传
export const uploadApi = {
  image: (file) => {
    const form = new FormData()
    form.append('file', file)
    return api.post('/upload/image', form, { headers: { 'Content-Type': 'multipart/form-data' } })
  },
}