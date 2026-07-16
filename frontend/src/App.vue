<template>
  <div id="app-root" :data-theme="theme">
    <!-- 顶部导航栏（登录页和简历预览页面隐藏） -->
    <nav class="navbar" v-if="$route.name !== 'resumePreview' && $route.name !== 'login'">
      <div class="navbar-inner">
        <div class="logo" @click="$router.push('/')">
          🤖 简历AI优化系统
        </div>
        <div class="nav-links">
          <router-link to="/">🏠 首页</router-link>
          <router-link to="/resume">📄 简历管理</router-link>
          <router-link to="/optimize">✨ AI优化</router-link>
          <router-link to="/jobs">💼 岗位浏览</router-link>
          <router-link to="/match">🎯 智能匹配</router-link>
        </div>
        <div class="nav-user">
          <!-- 主题切换 -->
          <button class="theme-toggle" @click="toggleTheme" :title="theme === 'light' ? '切换深色主题' : '切换浅色主题'">
            {{ theme === 'light' ? '🌙' : '☀️' }}
          </button>
          <span class="user-info" v-if="user">👤 {{ user.username }}</span>
          <span class="user-info" v-else>👤 游客</span>
          <router-link v-if="!user" to="/login" class="btn-login">登录</router-link>
          <a v-else href="#" @click.prevent="logout" class="btn-login">退出</a>
        </div>
      </div>
    </nav>

    <!-- 页面内容（带过渡动画） -->
    <div :class="$route.name === 'resumePreview' ? 'page-container-preview' : 'page-container'">
      <router-view v-slot="{ Component, route }">
        <transition name="page-fade" mode="out-in">
          <component :is="Component" :key="route.path" />
        </transition>
      </router-view>
    </div>

    <!-- Toast 通知容器 -->
    <div class="toast-container" ref="toastContainer"></div>
  </div>
</template>

<script>
export default {
  name: 'App',
  data() {
    return {
      user: null,
      theme: 'light',
    }
  },
  mounted() {
    this.loadUser()
    this.initTheme()
    // 初始化滚动渐入动画监听
    this.initScrollReveal()
  },
  watch: {
    '$route'() {
      this.loadUser()
      // 路由切换后重新初始化滚动监听
      this.$nextTick(() => {
        this.checkVisibility()
      })
    },
  },
  methods: {
    loadUser() {
      const stored = localStorage.getItem('user')
      if (stored) {
        try {
          this.user = JSON.parse(stored)
        } catch (e) {
          this.user = null
        }
      }
    },
    logout() {
      localStorage.removeItem('user')
      this.user = null
      this.$router.push('/login')
    },
    initTheme() {
      const saved = localStorage.getItem('theme')
      if (saved) {
        this.theme = saved
      } else if (window.matchMedia('(prefers-color-scheme: dark)').matches) {
        this.theme = 'dark'
      }
      document.documentElement.setAttribute('data-theme', this.theme)
    },
    toggleTheme() {
      this.theme = this.theme === 'light' ? 'dark' : 'light'
      localStorage.setItem('theme', this.theme)
      document.documentElement.setAttribute('data-theme', this.theme)
    },
    initScrollReveal() {
      const observer = new IntersectionObserver(
        (entries) => {
          entries.forEach((entry) => {
            if (entry.isIntersecting) {
              entry.target.classList.add('visible')
              observer.unobserve(entry.target)
            }
          })
        },
        { threshold: 0.1, rootMargin: '0px 0px -40px 0px' }
      )
      // 监听带有动画类的元素
      this.$nextTick(() => {
        document.querySelectorAll('.fade-in-up, .card, .stat-card, .feature-card').forEach((el) => {
          if (!el.classList.contains('visible')) {
            observer.observe(el)
          }
        })
      })
      // 保存引用以便路由切换后重新观察
      this._scrollObserver = observer
    },
    checkVisibility() {
      if (this._scrollObserver) {
        document.querySelectorAll('.fade-in-up, .card, .stat-card, .feature-card').forEach((el) => {
          this._scrollObserver.observe(el)
        })
      }
    },
  },
}
</script>

<style>
/* 页面过渡动画 */
.page-fade-enter-active {
  animation: pageEnter 0.35s ease;
}

.page-fade-leave-active {
  animation: pageLeave 0.2s ease;
}

@keyframes pageEnter {
  from {
    opacity: 0;
    transform: translateY(12px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes pageLeave {
  from {
    opacity: 1;
    transform: translateY(0);
  }
  to {
    opacity: 0;
    transform: translateY(-8px);
  }
}
</style>
