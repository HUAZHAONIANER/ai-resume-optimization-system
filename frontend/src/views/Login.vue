<template>
  <div class="login-container">
    <div class="login-card">
      <h2>{{ isLogin ? '🔐 用户登录' : '📝 用户注册' }}</h2>
      <p class="subtitle">简历智能优化与岗位匹配系统</p>

      <div class="form-group">
        <label>用户名</label>
        <input v-model="form.username" placeholder="请输入用户名" />
      </div>

      <div class="form-group">
        <label>密码</label>
        <input v-model="form.password" type="password" placeholder="请输入密码" />
      </div>

      <div class="form-group" v-if="!isLogin">
        <label>邮箱</label>
        <input v-model="form.email" placeholder="请输入邮箱（选填）" />
      </div>

      <div class="form-group" v-if="!isLogin">
        <label>手机号</label>
        <input v-model="form.phone" placeholder="请输入手机号（选填）" />
      </div>

      <button class="btn btn-primary" @click="handleSubmit" :disabled="submitting" style="width:100%;padding:12px;margin-top:8px;">
        {{ submitting ? '处理中...' : (isLogin ? '登录' : '注册') }}
      </button>

      <p style="text-align:center;margin-top:16px;color:#888;font-size:14px;">
        {{ isLogin ? '还没有账号？' : '已有账号？' }}
        <a href="#" @click.prevent="toggleMode" style="color:#1a73e8;text-decoration:none;">
          {{ isLogin ? '立即注册' : '去登录' }}
        </a>
      </p>

      <div class="info-banner" style="margin-top:20px;">
        💡 测试账号：admin / 123456（管理员） 或 student1 / 123456（学生）
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios'

export default {
  name: 'LoginView',
  data() {
    return {
      isLogin: true,
      submitting: false,
      form: {
        username: '',
        password: '',
        email: '',
        phone: '',
      },
    }
  },
  methods: {
    toggleMode() {
      this.isLogin = !this.isLogin
      this.form = { username: '', password: '', email: '', phone: '' }
    },
    async handleSubmit() {
      if (!this.form.username || !this.form.password) {
        window.$toast.warning('请输入用户名和密码')
        return
      }
      this.submitting = true
      try {
        const url = this.isLogin ? '/api/user/login' : '/api/user/register'
        const res = await axios.post(url, this.form)
        const data = res.data
        if (data.code === '200') {
          window.$toast.success(this.isLogin ? '登录成功！欢迎回来！' : '注册成功！请登录')
          if (data.data) {
            localStorage.setItem('user', JSON.stringify(data.data))
          }
          this.$router.push('/')
        } else {
          window.$toast.error(data.msg || '操作失败')
        }
      } catch (e) {
        window.$toast.error('请求失败：' + (e.response?.data?.msg || e.message))
      } finally {
        this.submitting = false
      }
    },
  },
}
</script>
