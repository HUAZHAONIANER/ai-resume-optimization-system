import './assets/main.css'

import { createApp } from 'vue'
import App from './App.vue'
import router from './router'

const app = createApp(App)
app.use(router)
app.mount('#app')

// ========== 全局 Toast 通知 ==========
window.$toast = function (message, type = 'info', duration = 3000) {
  const container = document.querySelector('.toast-container')
  if (!container) return

  const icons = {
    success: '✅',
    error: '❌',
    info: 'ℹ️',
    warning: '⚠️',
  }

  const toast = document.createElement('div')
  toast.className = `toast toast-${type}`
  toast.innerHTML = `<span>${icons[type] || icons.info}</span> ${message}`
  toast.addEventListener('click', () => {
    toast.style.animation = 'toastOut 0.3s ease forwards'
    setTimeout(() => toast.remove(), 300)
  })

  container.appendChild(toast)

  setTimeout(() => {
    if (toast.parentNode) {
      toast.remove()
    }
  }, duration)
}

// 便捷方法
window.$toast.success = (msg, d) => window.$toast(msg, 'success', d)
window.$toast.error = (msg, d) => window.$toast(msg, 'error', d)
window.$toast.info = (msg, d) => window.$toast(msg, 'info', d)
window.$toast.warning = (msg, d) => window.$toast(msg, 'warning', d)

// ========== 全局数字滚动动画 ==========
window.$countUp = function (el, target, duration = 1200) {
  if (!el) return
  const start = 0
  const startTime = performance.now()

  function easeOutExpo(t) {
    return t === 1 ? 1 : 1 - Math.pow(2, -10 * t)
  }

  function update(currentTime) {
    const elapsed = currentTime - startTime
    const progress = Math.min(elapsed / duration, 1)
    const eased = easeOutExpo(progress)
    const current = Math.round(start + (target - start) * eased)
    el.textContent = current
    if (progress < 1) {
      requestAnimationFrame(update)
    }
  }

  requestAnimationFrame(update)
}

// ========== 全局按钮涟漪效果 ==========
document.addEventListener('click', function (e) {
  const btn = e.target.closest('.btn')
  if (!btn || btn.disabled) return

  // 移除旧涟漪
  const oldRipple = btn.querySelector('.btn-ripple')
  if (oldRipple) oldRipple.remove()

  const ripple = document.createElement('span')
  ripple.className = 'btn-ripple'
  const rect = btn.getBoundingClientRect()
  const size = Math.max(rect.width, rect.height) * 2
  ripple.style.cssText = `
    position: absolute;
    border-radius: 50%;
    background: rgba(255,255,255,0.4);
    width: ${size}px;
    height: ${size}px;
    left: ${e.clientX - rect.left - size / 2}px;
    top: ${e.clientY - rect.top - size / 2}px;
    pointer-events: none;
    animation: rippleEffect 0.6s ease-out forwards;
    z-index: 0;
  `
  btn.appendChild(ripple)
})

// 按钮涟漪动画样式注入
const rippleStyle = document.createElement('style')
rippleStyle.textContent = `
  @keyframes rippleEffect {
    from { transform: scale(0); opacity: 0.5; }
    to { transform: scale(1); opacity: 0; }
  }
  .btn { position: relative; overflow: hidden; }
`
document.head.appendChild(rippleStyle)

// 导出供组件内使用
export { app }
