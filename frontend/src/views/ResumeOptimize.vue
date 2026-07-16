<template>
  <div>
    <div class="card">
      <div class="card-title">✨ 简历智能优化</div>
      <p style="color:#888;margin-bottom:16px;">
        对您的简历内容进行专业优化，提升竞争力与可读性
      </p>

      <!-- 选择简历 -->
      <div class="form-row">
        <div class="form-group">
          <label>选择要优化的简历</label>
          <select v-model="selectedResumeId" @change="onResumeSelect">
            <option value="">-- 请选择简历 --</option>
            <option v-for="r in resumes" :key="r.id" :value="r.id">{{ r.name }}</option>
          </select>
        </div>
      </div>

      <!-- 拖拽上传区域 -->
      <div
        class="drop-zone"
        :class="{ 'drop-zone-active': isDragging }"
        @dragover.prevent="isDragging = true"
        @dragleave.prevent="isDragging = false"
        @drop.prevent="handleDrop"
        @click="$refs.fileInput.click()"
      >
        <div class="drop-zone-content">
          <span class="drop-icon">📂</span>
          <span>{{ isDragging ? '📥 松开鼠标上传文件' : '拖拽简历文件到此处，或点击选择文件（支持 .txt）' }}</span>
        </div>
        <input type="file" ref="fileInput" accept=".txt,.pdf,.doc,.docx" style="display:none;" @change="handleFileSelect" />
      </div>

      <!-- 简历内容 -->
      <div class="two-column" v-if="selectedResume || resumeContent">
        <div>
          <div class="form-group">
            <label>📝 原始简历内容（可编辑）</label>
            <textarea v-model="resumeContent" rows="15" placeholder="在此输入或修改您的简历内容..."></textarea>
          </div>
          <button class="btn btn-success" @click="optimizeResume" :disabled="optimizing">
            {{ optimizing ? '✨ 正在优化中...' : '✨ 开始优化' }}
          </button>
        </div>
        <div>
          <div class="form-group">
            <label>🤖 优化结果</label>
            <div class="ai-response" v-if="optimizedContent && !optimizing">{{ optimizedContent }}</div>
            <div class="ai-loading-container" v-else-if="optimizing">
              <div class="ai-brain">🧠</div>
              <div class="ai-progress-text">{{ optimizingText }}</div>
              <div class="progress-bar" style="width:200px;"></div>
            </div>
            <div class="ai-response" v-else style="color:#aaa;text-align:center;">
              点击"开始优化"按钮，系统将为您优化简历
            </div>
          </div>
          <div v-if="optimizedContent && !optimizing" style="margin-top:12px;display:flex;gap:10px;">
            <button class="btn btn-primary" @click="saveOptimized">
              💾 保存优化结果
            </button>
            <button class="btn btn-outline" @click="downloadResume">
              📥 下载简历文件
            </button>
          </div>
        </div>
      </div>

      <div v-else class="empty-state">
        <div class="empty-icon">📝</div>
        <p>请先选择一份简历，或在简历管理页面创建新简历</p>
        <button class="btn btn-primary" style="margin-top:12px;" @click="$router.push('/resume')">前往简历管理</button>
      </div>
    </div>

    <!-- 优化提示 -->
    <div class="card">
      <div class="card-title">💡 优化说明</div>
      <div class="info-banner">
        ℹ️ 系统将对您的简历进行以下优化：
      </div>
      <ul style="padding-left:20px;line-height:2;color:#666;">
        <li>使用专业、简洁、有力的语言描述经历和技能</li>
        <li>突出与目标岗位相关的核心竞争力</li>
        <li>量化成果，使用具体数据和指标增强说服力</li>
        <li>优化简历结构和排版建议</li>
        <li>修正语法错误和不恰当的表达</li>
        <li>使用行业关键词提升ATS筛选通过率</li>
      </ul>
    </div>
  </div>
</template>

<script>
import axios from 'axios'

export default {
  name: 'ResumeOptimize',
  data() {
    return {
      resumes: [],
      selectedResumeId: '',
      selectedResume: null,
      resumeContent: '',
      optimizedContent: '',
      optimizing: false,
      isDragging: false,
      uploading: false,
      optimizingText: '正在解析简历内容...',
      optimizingTimer: null,
    }
  },
  mounted() {
    this.fetchResumes()
    if (this.$route.query.resumeId) {
      this.selectedResumeId = parseInt(this.$route.query.resumeId)
      this.onResumeSelect()
    }
  },
  methods: {
    async fetchResumes() {
      try {
        const res = await axios.get('/api/resume/getResume')
        const data = res.data
        if (Array.isArray(data)) { this.resumes = data }
        else if (data && Array.isArray(data.data)) { this.resumes = data.data }
        else { this.resumes = [] }
      } catch (e) {
        console.error('获取简历列表失败', e)
      }
    },
    onResumeSelect() {
      const resumeId = Number(this.selectedResumeId)
      const resume = this.resumes.find(r => r.id === resumeId)
      if (resume) {
        this.selectedResume = resume
        this.resumeContent = resume.content || (
          '教育背景：' + (resume.education || '') + '\n' +
          '学校：' + (resume.school || '') + '\n' +
          '专业：' + (resume.major || '') + '\n' +
          '技能：' + (resume.skills || '') + '\n' +
          '经历：' + (resume.experience || '')
        )
        this.optimizedContent = resume.optimizedContent || ''
      } else {
        this.selectedResume = null
        this.resumeContent = ''
        this.optimizedContent = ''
      }
    },
    async optimizeResume() {
      if (!this.resumeContent.trim()) {
        window.$toast.warning('请输入简历内容')
        return
      }
      this.optimizing = true
      this.optimizedContent = ''
      // 启动进度文字轮换
      const progressTexts = [
        '正在解析简历内容...',
        '正在分析关键信息...',
        '正在匹配行业标准...',
        '正在生成优化建议...',
        '正在润色语言表达...',
        '即将完成优化...',
      ]
      let idx = 0
      this.optimizingText = progressTexts[0]
      this.optimizingTimer = setInterval(() => {
        idx = (idx + 1) % progressTexts.length
        this.optimizingText = progressTexts[idx]
      }, 1800)
      try {
        const res = await axios.post('/api/ai/optimizeResume', {
          resumeId: this.selectedResumeId ? Number(this.selectedResumeId) : null,
          content: this.resumeContent,
        })
        const data = res.data
        if (data.code === '200' && data.data) {
          // 后端已做清理，前端再做一次保险清理
          this.optimizedContent = this.cleanSymbols(data.data.optimizedContent || '优化结果为空')
          window.$toast.success('简历优化完成！')
        } else {
          window.$toast.error('优化失败：' + (data.msg || '未知错误'))
        }
      } catch (e) {
        window.$toast.error('AI优化请求失败：' + (e.response?.data?.msg || e.message))
      } finally {
        this.optimizing = false
        if (this.optimizingTimer) {
          clearInterval(this.optimizingTimer)
          this.optimizingTimer = null
        }
      }
    },
    async saveOptimized() {
      if (!this.selectedResume || !this.optimizedContent) return
      try {
        await axios.post('/api/resume/updateResume', {
          id: this.selectedResume.id,
          optimizedContent: this.optimizedContent,
        })
        window.$toast.success('优化结果已保存到简历！')
      } catch (e) {
        window.$toast.error('保存失败')
      }
    },
    async handleDrop(e) {
      this.isDragging = false
      const file = e.dataTransfer.files[0]
      if (file) await this.uploadFile(file)
    },
    async handleFileSelect(e) {
      const file = e.target.files[0]
      if (file) await this.uploadFile(file)
    },
    async uploadFile(file) {
      this.uploading = true
      try {
        const formData = new FormData()
        formData.append('file', file)
        const res = await axios.post('/api/resume/uploadFile', formData, {
          headers: { 'Content-Type': 'multipart/form-data' },
        })
        const data = res.data
        if (data.code === '200' && data.data) {
          if (data.data.content) {
            this.resumeContent = data.data.content
            this.selectedResume = this.selectedResume || { id: null, name: file.name }
            window.$toast.success('文件上传成功！')
          } else {
            window.$toast.warning('文件已上传，但无法解析内容。请手动粘贴简历内容。')
          }
        } else {
          window.$toast.error('上传失败：' + (data.msg || '未知错误'))
        }
      } catch (e) {
        window.$toast.error('文件上传失败：' + (e.response?.data?.msg || e.message))
      } finally {
        this.uploading = false
      }
    },
    downloadResume() {
      if (!this.optimizedContent) return
      const resumeName = this.selectedResume?.name || '简历'
      const date = new Date().toISOString().slice(0, 10)
      const fileName = `优化简历_${resumeName}_${date}.txt`
      const blob = new Blob([this.optimizedContent], { type: 'text/plain;charset=utf-8' })
      const url = URL.createObjectURL(blob)
      const a = document.createElement('a')
      a.href = url
      a.download = fileName
      document.body.appendChild(a)
      a.click()
      document.body.removeChild(a)
      URL.revokeObjectURL(url)
    },
    cleanSymbols(text) {
      if (!text) return text
      // 去除代码块标记
      text = text.replace(/```[a-zA-Z]*\s*/g, '')
      text = text.replace(/```/g, '')
      // 去除粗体/斜体 **text** *text*
      text = text.replace(/\*\*(.+?)\*\*/g, '$1')
      text = text.replace(/__(.+?)__/g, '$1')
      text = text.replace(/\*(.+?)\*/g, '$1')
      text = text.replace(/_(.+?)_/g, '$1')
      // 去除删除线
      text = text.replace(/~~(.+?)~~/g, '$1')
      // 去除行内代码
      text = text.replace(/`(.+?)`/g, '$1')
      // 去除markdown标题标记
      text = text.replace(/^#{1,6}\s+/gm, '')
      // 去除水平分隔线
      text = text.replace(/^[-*_]{3,}\s*$/gm, '')
      // 酌情减少括号内容：去掉全角/半角括号内的补充说明
      text = text.replace(/[（(][^）)]*[）)]/g, '')
      // 去掉英文圆括号及其内容
      text = text.replace(/\([^)]*\)/g, '')
      // 合并多余空行
      text = text.replace(/\n{4,}/g, '\n\n')
      // 合并多余空格
      text = text.replace(/[ \t]{2,}/g, ' ')
      // 清理行首尾空格
      text = text.replace(/^[ \t]+/gm, '')
      text = text.replace(/[ \t]+$/gm, '')
      return text.trim()
    },
  },
}
</script>
