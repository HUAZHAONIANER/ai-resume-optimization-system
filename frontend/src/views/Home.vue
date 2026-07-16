<template>
  <div>
    <!-- 英雄横幅 -->
    <div class="hero-banner">
      <h1>🤖 简历智能优化与岗位匹配系统</h1>
      <p>
        为大学生简历提供智能化优化建议，
        精准匹配最适合的岗位机会，助力职场起航！
      </p>
      <div class="hero-actions">
        <button class="btn btn-success btn-lg" @click="$router.push('/optimize')" style="padding:12px 32px;font-size:16px;">
          ✨ 立即优化简历
        </button>
        <button class="btn btn-outline btn-lg" @click="$router.push('/match')" style="padding:12px 32px;font-size:16px;background:rgba(255,255,255,0.15);color:#fff;border-color:rgba(255,255,255,0.5);">
          🎯 岗位智能匹配
        </button>
      </div>
    </div>

    <!-- 统计概览 -->
    <div class="stat-grid">
      <div class="stat-card" @click="$router.push('/resume')" style="cursor:pointer;">
        <div class="stat-icon blue">📄</div>
        <div class="stat-info">
          <h3 ref="countResume">0</h3>
          <p>简历总数</p>
        </div>
      </div>
      <div class="stat-card" @click="$router.push('/jobs')" style="cursor:pointer;">
        <div class="stat-icon green">💼</div>
        <div class="stat-info">
          <h3 ref="countJob">0</h3>
          <p>岗位数量</p>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon orange">🎯</div>
        <div class="stat-info">
          <h3 ref="countMatch">0</h3>
          <p>匹配记录</p>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon purple">👤</div>
        <div class="stat-info">
          <h3 ref="countUser">0</h3>
          <p>注册用户</p>
        </div>
      </div>
    </div>

    <!-- 核心功能 -->
    <div class="card">
      <div class="card-title">🚀 核心功能</div>
      <div class="feature-grid">
        <div class="feature-card" @click="$router.push('/resume')">
          <div class="feature-icon">📄</div>
          <h3>简历管理</h3>
          <p>在线创建和管理多份简历，支持教育背景、技能、经历等完整信息录入</p>
        </div>
        <div class="feature-card" @click="$router.push('/optimize')">
          <div class="feature-icon">✨</div>
          <h3>智能优化</h3>
          <p>深度优化简历内容，提升专业性和可读性</p>
        </div>
        <div class="feature-card" @click="$router.push('/jobs')">
          <div class="feature-icon">💼</div>
          <h3>岗位浏览</h3>
          <p>浏览热门岗位信息，了解企业招聘需求和任职要求</p>
        </div>
        <div class="feature-card" @click="$router.push('/match')">
          <div class="feature-icon">🎯</div>
          <h3>智能匹配</h3>
          <p>分析简历与岗位匹配度，获取专业评估和针对性改进建议</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios'

export default {
  name: 'HomeView',
  data() {
    return {
      stats: {
        resumeCount: 0,
        jobCount: 0,
        matchCount: 0,
        userCount: 0,
      },
    }
  },
  mounted() {
    this.fetchStats()
  },
  methods: {
    async fetchStats() {
      try {
        const [resumeRes, jobRes, matchRes, userRes] = await Promise.all([
          axios.get('/api/resume/getResume'),
          axios.get('/api/job/getJob'),
          axios.get('/api/ai/getMatchHistory'),
          axios.get('/api/user/getUser'),
        ])
        this.stats.resumeCount = this.extractArray(resumeRes.data).length
        this.stats.jobCount = this.extractArray(jobRes.data).length
        this.stats.matchCount = this.extractArray(matchRes.data).length
        this.stats.userCount = this.extractArray(userRes.data).length
        // 触发数字滚动动画
        this.$nextTick(() => {
          this.animateCounts()
        })
      } catch (e) {
        console.log('统计数据加载中...')
      }
    },
    extractArray(data) {
      if (Array.isArray(data)) return data
      if (data && Array.isArray(data.data)) return data.data
      if (data && Array.isArray(data.records)) return data.records
      return []
    },
    animateCounts() {
      const targets = [
        { el: this.$refs.countResume, val: this.stats.resumeCount },
        { el: this.$refs.countJob, val: this.stats.jobCount },
        { el: this.$refs.countMatch, val: this.stats.matchCount },
        { el: this.$refs.countUser, val: this.stats.userCount },
      ]
      targets.forEach(t => {
        if (t.el && t.val > 0) {
          window.$countUp(t.el, t.val, 1000)
        }
      })
    },
  },
}
</script>
