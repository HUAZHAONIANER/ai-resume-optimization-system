<template>
  <div>
    <div class="card">
      <div class="card-title">🎯 简历与岗位智能匹配</div>
      <p style="color:#888;margin-bottom:16px;">
        深度分析您的简历与目标岗位的匹配程度，提供专业评估和改进建议
      </p>

      <div class="form-row">
        <div class="form-group">
          <label>选择简历</label>
          <select v-model="selectedResumeId">
            <option value="">-- 请选择简历 --</option>
            <option v-for="r in resumes" :key="r.id" :value="r.id">{{ r.name }}</option>
          </select>
        </div>
        <div class="form-group">
          <label>选择目标岗位</label>
          <select v-model="selectedJobId">
            <option value="">-- 请选择岗位 --</option>
            <option v-for="j in jobs" :key="j.id" :value="j.id">{{ j.title }} - {{ j.company }}</option>
          </select>
        </div>
      </div>

      <button class="btn btn-success" @click="startMatch" :disabled="matching || !selectedResumeId || !selectedJobId" style="margin-bottom:20px;">
        {{ matching ? '🎯 正在分析匹配...' : '🎯 开始智能匹配分析' }}
      </button>

      <!-- 匹配结果 -->
      <div v-if="matchResult && !matching">
        <div class="card-title">📊 匹配分析结果</div>
        <div class="match-result-text">{{ cleanedMatchResult }}</div>

        <div style="margin-top:16px;">
          <button class="btn btn-primary" @click="saveMatchResult">💾 保存匹配结果</button>
          <button class="btn btn-outline" style="margin-left:10px;" @click="$router.push('/optimize')">✨ 去优化简历</button>
        </div>
      </div>

      <div class="ai-loading-container" v-if="matching">
        <div class="ai-brain">🧠</div>
        <div class="ai-progress-text">{{ matchingText }}</div>
        <div class="progress-bar" style="width:200px;"></div>
      </div>

      <div v-if="!matchResult && !matching && selectedResumeId && selectedJobId" class="info-banner" style="margin-top:16px;">
        ℹ️ 已选择简历和岗位，点击上方按钮开始智能匹配分析
      </div>
    </div>

    <!-- 匹配历史 -->
    <div class="card">
      <div class="card-title">📋 匹配历史记录</div>
      <div v-if="matchHistory.length > 0">
        <div class="table-container">
          <table>
            <thead>
              <tr>
                <th>ID</th>
                <th>简历ID</th>
                <th>岗位ID</th>
                <th>分析结果（摘要）</th>
                <th>时间</th>
                <th>操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="m in matchHistory" :key="m.id">
                <td>{{ m.id }}</td>
                <td>{{ m.resumeId }}</td>
                <td>{{ m.jobId }}</td>
                <td>{{ cleanSummary(m.analysis) }}</td>
                <td>{{ m.createTime || '-' }}</td>
                <td class="action-cell">
                  <button class="btn btn-outline btn-sm" @click="viewHistoryDetail(m)">查看</button>
                  <button class="btn btn-danger btn-sm" @click="deleteHistory(m.id)">删除</button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
      <div v-else class="empty-state">
        <div class="empty-icon">📋</div>
        <p>暂无匹配记录</p>
      </div>
    </div>

    <!-- 历史详情模态框 -->
    <div class="modal-overlay" v-if="showHistoryDetail" @click.self="showHistoryDetail = false">
      <div class="modal" style="min-width:700px;">
        <h3>📊 匹配分析详情</h3>
        <div class="match-result-text" v-if="historyDetail">{{ cleanText(historyDetail.analysis) }}</div>
        <div class="modal-actions">
          <button class="btn btn-outline" @click="showHistoryDetail = false">关闭</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios'

export default {
  name: 'JobMatch',
  data() {
    return {
      resumes: [],
      jobs: [],
      selectedResumeId: '',
      selectedJobId: '',
      matchResult: '',
      matching: false,
      matchHistory: [],
      showHistoryDetail: false,
      historyDetail: null,
      matchingText: '正在解析简历与岗位...',
      matchingTimer: null,
    }
  },
  computed: {
    cleanedMatchResult() {
      return this.cleanText(this.matchResult)
    },
  },
  mounted() {
    this.fetchResumes()
    this.fetchJobs()
    this.fetchMatchHistory()
    if (this.$route.query.jobId) {
      this.selectedJobId = parseInt(this.$route.query.jobId)
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
    async fetchJobs() {
      try {
        const res = await axios.get('/api/job/getJob')
        const data = res.data
        if (Array.isArray(data)) { this.jobs = data }
        else if (data && Array.isArray(data.data)) { this.jobs = data.data }
        else { this.jobs = [] }
      } catch (e) {
        console.error('获取岗位列表失败', e)
      }
    },
    async fetchMatchHistory() {
      try {
        const res = await axios.get('/api/ai/getMatchHistory')
        const data = res.data
        if (Array.isArray(data)) { this.matchHistory = data }
        else if (data && Array.isArray(data.data)) { this.matchHistory = data.data }
        else { this.matchHistory = [] }
      } catch (e) {
        console.error('获取匹配历史失败', e)
      }
    },
    async startMatch() {
      if (!this.selectedResumeId || !this.selectedJobId) {
        window.$toast.warning('请选择简历和岗位')
        return
      }
      this.matching = true
      this.matchResult = ''
      // 启动进度文字轮换
      const progressTexts = [
        '正在解析简历与岗位...',
        '正在提取关键技能...',
        '正在分析匹配维度...',
        '正在计算匹配得分...',
        '正在生成改进建议...',
        '即将完成分析...',
      ]
      let idx = 0
      this.matchingText = progressTexts[0]
      this.matchingTimer = setInterval(() => {
        idx = (idx + 1) % progressTexts.length
        this.matchingText = progressTexts[idx]
      }, 1800)
      try {
        const res = await axios.post('/api/ai/matchJob', {
          resumeId: this.selectedResumeId,
          jobId: this.selectedJobId,
        })
        const data = res.data
        if (data.code === '200' && data.data) {
          this.matchResult = data.data.analysis || '分析结果为空'
          window.$toast.success('智能匹配分析完成！')
        } else {
          window.$toast.error('匹配失败：' + (data.msg || '未知错误'))
        }
      } catch (e) {
        window.$toast.error('匹配请求失败：' + (e.response?.data?.msg || e.message))
      } finally {
        this.matching = false
        if (this.matchingTimer) {
          clearInterval(this.matchingTimer)
          this.matchingTimer = null
        }
      }
    },
    saveMatchResult() {
      window.$toast.success('匹配结果已保存，可在下方历史记录中查看。')
      this.fetchMatchHistory()
    },
    viewHistoryDetail(m) {
      this.historyDetail = m
      this.showHistoryDetail = true
    },
    async deleteHistory(id) {
      if (!confirm('确认删除该匹配记录吗？')) return
      try {
        await axios.get('/api/ai/deleteMatchResult', { params: { id } })
        window.$toast.success('删除成功！')
        this.fetchMatchHistory()
      } catch (e) {
        window.$toast.error('删除失败')
      }
    },

    /* 去除 # * 等 markdown 符号及 AI 痕迹 */
    cleanText(text) {
      if (!text) return text
      let t = text
      // 代码块
      t = t.replace(/```[a-zA-Z]*\s*/g, '').replace(/```/g, '')
      // 标题标记
      t = t.replace(/^#{1,6}\s+/gm, '')
      // 粗体/斜体
      t = t.replace(/\*\*(.+?)\*\*/g, '$1').replace(/__(.+?)__/g, '$1')
      t = t.replace(/\*(.+?)\*/g, '$1').replace(/_(.+?)_/g, '$1')
      // 删除线 / 行内代码
      t = t.replace(/~~(.+?)~~/g, '$1').replace(/`(.+?)`/g, '$1')
      // 水平分隔线
      t = t.replace(/^[-*_]{3,}\s*$/gm, '')
      // 方括号内容展开
      t = t.replace(/\[([^\]]*)\]/g, ' $1 ')
      // 括号内容缩减
      t = t.replace(/[（(][^）)]*[）)]/g, '')
      t = t.replace(/\([^)]*\)/g, '')
      // 常见 AI 前缀词
      t = t.replace(/^(AI|ai)[：:\s]*/gm, '')
      // 多余空白
      t = t.replace(/[ \t]{2,}/g, ' ')
      t = t.replace(/^[ \t]+/gm, '').replace(/[ \t]+$/gm, '')
      t = t.replace(/\n{4,}/g, '\n\n')
      return t.trim()
    },

    /* 表格摘要 */
    cleanSummary(text) {
      if (!text) return '-'
      const cleaned = this.cleanText(text)
      return cleaned.length > 60 ? cleaned.substring(0, 60) + '...' : cleaned
    },
  },
}
</script>

<style scoped>
/* 匹配结果文本 */
.match-result-text {
  background: #f8f9fa;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  padding: 20px;
  white-space: pre-wrap;
  line-height: 1.9;
  font-size: 14px;
  max-height: 500px;
  overflow-y: auto;
  color: #333;
}

/* 操作按钮列 — 左右并列 */
.action-cell {
  display: flex;
  flex-direction: row;
  gap: 8px;
  align-items: center;
  white-space: nowrap;
}
</style>
