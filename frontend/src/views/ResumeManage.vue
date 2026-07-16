<template>
  <div>
    <div class="card">
      <div style="display:flex;justify-content:space-between;align-items:center;">
        <div class="card-title" style="margin-bottom:0;border:none;padding-bottom:0;">📄 我的简历</div>
        <button class="btn btn-primary" @click="openAddModal">+ 新建简历</button>
      </div>
    </div>

    <!-- 搜索工具栏 -->
    <div class="toolbar">
      <input v-model="searchName" type="text" placeholder="🔍 搜索简历名称..." />
      <input v-model="searchSchool" type="text" placeholder="🏫 搜索学校..." />
      <select v-model="searchEducation">
        <option value="">全部学历</option>
        <option value="本科">本科</option>
        <option value="硕士">硕士</option>
        <option value="博士">博士</option>
        <option value="大专">大专</option>
      </select>
      <button class="btn btn-outline btn-sm" @click="clearSearch">清空</button>
    </div>

    <!-- 简历列表 -->
    <div class="card" v-if="filteredResumes.length > 0">
      <div class="table-container">
        <table>
          <thead>
            <tr>
              <th>ID</th>
              <th>简历名称</th>
              <th>学校</th>
              <th>专业</th>
              <th>学历</th>
              <th>创建时间</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="resume in filteredResumes" :key="resume.id">
              <td>{{ resume.id }}</td>
              <td><strong>{{ resume.name }}</strong></td>
              <td>{{ resume.school || '-' }}</td>
              <td>{{ resume.major || '-' }}</td>
              <td><span class="tag tag-blue">{{ resume.education || '-' }}</span></td>
              <td>{{ resume.createTime || '-' }}</td>
              <td>
                <button class="btn btn-outline btn-sm" @click="viewResume(resume)">预览</button>
                <button class="btn btn-primary btn-sm" style="margin-left:6px;" @click="openEditModal(resume)">编辑</button>
                <button class="btn btn-success btn-sm" style="margin-left:6px;" @click="goOptimize(resume)">AI优化</button>
                <button class="btn btn-danger btn-sm" style="margin-left:6px;" @click="handleDelete(resume.id)">删除</button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <div v-else class="card empty-state">
      <div class="empty-icon">📄</div>
      <p>暂无简历，点击上方按钮创建您的第一份简历吧！</p>
    </div>

    <!-- 简历详情模态框 -->
    <div class="modal-overlay" v-if="showDetail" @click.self="showDetail = false">
      <div class="modal" style="min-width:700px;">
        <h3>📄 简历详情</h3>
        <div v-if="detailResume" style="line-height:2;">
          <p><strong>简历名称：</strong>{{ detailResume.name }}</p>
          <p><strong>学校：</strong>{{ detailResume.school || '-' }}</p>
          <p><strong>专业：</strong>{{ detailResume.major || '-' }}</p>
          <p><strong>学历：</strong>{{ detailResume.education || '-' }}</p>
          <p><strong>技能：</strong>{{ detailResume.skills || '-' }}</p>
          <p><strong>经历：</strong></p>
          <div class="ai-response">{{ detailResume.experience || '暂无' }}</div>
          <p style="margin-top:12px;"><strong>简历全文：</strong></p>
          <div class="ai-response">{{ detailResume.content || '暂无' }}</div>
          <p v-if="detailResume.optimizedContent" style="margin-top:12px;color:#0f9d58;"><strong>✨ AI优化后：</strong></p>
          <div v-if="detailResume.optimizedContent" class="ai-response" style="background:#f0fff4;border-color:#c6f6d5;">
            {{ detailResume.optimizedContent }}
          </div>
        </div>
        <div class="modal-actions">
          <button class="btn btn-outline" @click="showDetail = false">关闭</button>
          <button class="btn btn-primary" v-if="detailResume" @click="showDetail = false; openEditModal(detailResume)">编辑</button>
          <button class="btn btn-success" v-if="detailResume && detailResume.optimizedContent" @click="downloadOptimized(detailResume)">📥 下载优化简历</button>
        </div>
      </div>
    </div>

    <!-- 新增/编辑模态框 -->
    <div class="modal-overlay" v-if="showModal" @click.self="showModal = false">
      <div class="modal" style="min-width:650px;">
        <h3>{{ isEditing ? '编辑简历' : '新建简历' }}</h3>
        <div class="form-row">
          <div class="form-group">
            <label>简历名称 *</label>
            <input v-model="form.name" placeholder="如：张三的Java开发简历" />
          </div>
          <div class="form-group">
            <label>学历</label>
            <select v-model="form.education">
              <option value="">请选择</option>
              <option value="大专">大专</option>
              <option value="本科">本科</option>
              <option value="硕士">硕士</option>
              <option value="博士">博士</option>
            </select>
          </div>
        </div>
        <div class="form-row">
          <div class="form-group">
            <label>学校</label>
            <input v-model="form.school" placeholder="如：清华大学" />
          </div>
          <div class="form-group">
            <label>专业</label>
            <input v-model="form.major" placeholder="如：计算机科学与技术" />
          </div>
        </div>
        <div class="form-group">
          <label>技能列表</label>
          <textarea v-model="form.skills" placeholder="如：Java、Spring Boot、MySQL、Vue.js..." rows="2"></textarea>
        </div>
        <div class="form-group">
          <label>实习/项目经历</label>
          <textarea v-model="form.experience" placeholder="请描述您的实习经历或项目经验..." rows="4"></textarea>
        </div>
        <div class="form-group">
          <label>简历全文</label>
          <div
            class="drop-zone drop-zone-sm"
            :class="{ 'drop-zone-active': isDragging }"
            @dragover.prevent="isDragging = true"
            @dragleave.prevent="isDragging = false"
            @drop.prevent="handleDrop"
            @click="$refs.fileInput.click()"
          >
            <span v-if="isDragging">📥 松开鼠标上传</span>
            <span v-else>📂 拖拽简历文件到此处自动填充内容（支持 .txt）</span>
          </div>
          <input type="file" ref="fileInput" accept=".txt,.pdf,.doc,.docx" style="display:none;" @change="handleFileSelect" />
          <textarea v-model="form.content" placeholder="请粘贴或输入完整的简历内容，AI优化将基于此内容进行..." rows="6" style="margin-top:10px;"></textarea>
        </div>
        <div class="modal-actions">
          <button class="btn btn-outline" @click="showModal = false">取消</button>
          <button class="btn btn-primary" @click="handleSubmit" :disabled="submitting">
            {{ submitting ? '提交中...' : (isEditing ? '保存修改' : '创建简历') }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios'

export default {
  name: 'ResumeManage',
  data() {
    return {
      resumes: [],
      searchName: '',
      searchSchool: '',
      searchEducation: '',
      showModal: false,
      showDetail: false,
      isEditing: false,
      submitting: false,
      detailResume: null,
      isDragging: false,
      uploading: false,
      form: {
        id: null,
        userId: 1,
        name: '',
        education: '',
        school: '',
        major: '',
        skills: '',
        experience: '',
        content: '',
      },
    }
  },
  computed: {
    filteredResumes() {
      return this.resumes.filter(r => {
        const matchName = !this.searchName || (r.name && r.name.includes(this.searchName))
        const matchSchool = !this.searchSchool || (r.school && r.school.includes(this.searchSchool))
        const matchEducation = !this.searchEducation || r.education === this.searchEducation
        return matchName && matchSchool && matchEducation
      })
    },
  },
  mounted() {
    this.fetchResumes()
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
    openAddModal() {
      this.isEditing = false
      this.form = { id: null, userId: 1, name: '', education: '', school: '', major: '', skills: '', experience: '', content: '' }
      this.showModal = true
    },
    openEditModal(resume) {
      this.isEditing = true
      this.form = { ...resume }
      this.showModal = true
    },
    viewResume(resume) {
      this.$router.push({ name: 'resumePreview', params: { id: resume.id } })
    },
    goOptimize(resume) {
      this.$router.push({ name: 'optimize', query: { resumeId: resume.id } })
    },
    async handleSubmit() {
      if (!this.form.name) {
        window.$toast.warning('请输入简历名称')
        return
      }
      this.submitting = true
      try {
        const url = this.isEditing ? '/api/resume/updateResume' : '/api/resume/addResume'
        await axios.post(url, this.form)
        window.$toast.success(this.isEditing ? '更新成功！' : '创建成功！')
        this.showModal = false
        this.fetchResumes()
      } catch (e) {
        window.$toast.error('操作失败：' + (e.response?.data?.msg || e.message))
      } finally {
        this.submitting = false
      }
    },
    async handleDelete(id) {
      if (!confirm('确认删除该简历吗？此操作不可恢复。')) return
      try {
        await axios.get('/api/resume/deleteResume', { params: { id } })
        window.$toast.success('删除成功！')
        this.fetchResumes()
      } catch (e) {
        window.$toast.error('删除失败')
      }
    },
    async handleDrop(e) {
      this.isDragging = false
      const file = e.dataTransfer.files[0]
      if (file) await this.uploadFileContent(file)
    },
    async handleFileSelect(e) {
      const file = e.target.files[0]
      if (file) await this.uploadFileContent(file)
    },
    async uploadFileContent(file) {
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
            this.form.content = data.data.content
            if (!this.form.name) {
              this.form.name = file.name.replace(/\.[^.]+$/, '')
            }
            window.$toast.success('文件解析成功！')
          } else {
            window.$toast.warning('文件已上传，但无法解析内容。请手动粘贴简历全文。')
          }
        } else {
          window.$toast.error('上传失败：' + (data.msg || '未知错误'))
        }
      } catch (e) {
        window.$toast.error('文件上传失败：' + (e.response?.data?.msg || e.message))
      } finally {
        this.uploading = false
        // 重置file input，允许重复上传同一文件
        this.$refs.fileInput.value = ''
      }
    },
    clearSearch() {
      this.searchName = ''
      this.searchSchool = ''
      this.searchEducation = ''
    },
    downloadOptimized(resume) {
      if (!resume.optimizedContent) return
      const date = new Date().toISOString().slice(0, 10)
      const fileName = `优化简历_${resume.name || '简历'}_${date}.txt`
      const blob = new Blob([resume.optimizedContent], { type: 'text/plain;charset=utf-8' })
      const url = URL.createObjectURL(blob)
      const a = document.createElement('a')
      a.href = url
      a.download = fileName
      document.body.appendChild(a)
      a.click()
      document.body.removeChild(a)
      URL.revokeObjectURL(url)
    },
  },
}
</script>
