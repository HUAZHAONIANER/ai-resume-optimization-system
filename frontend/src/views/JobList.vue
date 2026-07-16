<template>
  <div>
    <div class="card">
      <div style="display:flex;justify-content:space-between;align-items:center;">
        <div class="card-title" style="margin-bottom:0;border:none;padding-bottom:0;">💼 岗位列表</div>
        <button class="btn btn-primary" @click="openAddModal" v-if="isAdmin">+ 发布岗位</button>
      </div>
    </div>

    <!-- 搜索工具栏 -->
    <div class="toolbar">
      <input v-model="searchTitle" type="text" placeholder="🔍 搜索岗位..." />
      <input v-model="searchCompany" type="text" placeholder="🏢 搜索公司..." />
      <select v-model="searchType">
        <option value="">全部类型</option>
        <option value="全职">全职</option>
        <option value="实习">实习</option>
        <option value="兼职">兼职</option>
      </select>
      <select v-model="searchLocation">
        <option value="">全部地点</option>
        <option v-for="loc in locations" :key="loc" :value="loc">{{ loc }}</option>
      </select>
      <button class="btn btn-outline btn-sm" @click="clearSearch">清空</button>
    </div>

    <!-- 岗位列表 -->
    <div v-if="filteredJobs.length > 0">
      <div class="card" v-for="job in filteredJobs" :key="job.id" style="cursor:pointer;" @click="viewJob(job)">
        <div style="display:flex;justify-content:space-between;align-items:flex-start;">
          <div style="flex:1;">
            <h3 style="font-size:18px;color:#1a1a1a;margin-bottom:8px;">{{ job.title }}</h3>
            <p style="color:#1a73e8;font-weight:500;margin-bottom:6px;">{{ job.company }}</p>
            <p style="color:#888;font-size:14px;margin-bottom:4px;">
              📍 {{ job.location || '地点不限' }} &nbsp;|&nbsp; 💰 {{ job.salary || '薪资面议' }} &nbsp;|&nbsp; 📋 {{ job.type || '全职' }}
            </p>
            <p style="color:#aaa;font-size:13px;">📅 {{ job.createTime || '-' }}</p>
          </div>
          <div style="display:flex;gap:8px;flex-shrink:0;margin-left:20px;">
            <button class="btn btn-success btn-sm" @click.stop="goMatch(job)">🎯 匹配</button>
            <button class="btn btn-outline btn-sm" @click.stop="openEditModal(job)">编辑</button>
            <button class="btn btn-danger btn-sm" @click.stop="handleDelete(job.id)">删除</button>
          </div>
        </div>
      </div>
    </div>

    <div v-else class="card empty-state">
      <div class="empty-icon">💼</div>
      <p>暂无岗位信息</p>
    </div>

    <!-- 岗位详情模态框 -->
    <div class="modal-overlay" v-if="showDetail" @click.self="showDetail = false">
      <div class="modal" style="min-width:700px;">
        <h3>💼 {{ detailJob.title }}</h3>
        <div style="line-height:2;margin-top:12px;" v-if="detailJob">
          <p><strong>公司：</strong>{{ detailJob.company }}</p>
          <p><strong>地点：</strong>{{ detailJob.location || '-' }}</p>
          <p><strong>薪资：</strong>{{ detailJob.salary || '-' }}</p>
          <p><strong>类型：</strong><span class="tag tag-green">{{ detailJob.type || '-' }}</span></p>
          <p><strong>岗位描述：</strong></p>
          <div class="ai-response">{{ detailJob.description || '暂无' }}</div>
          <p style="margin-top:12px;"><strong>任职要求：</strong></p>
          <div class="ai-response">{{ detailJob.requirements || '暂无' }}</div>
        </div>
        <div class="modal-actions">
          <button class="btn btn-outline" @click="showDetail = false">关闭</button>
          <button class="btn btn-success" @click="showDetail = false; goMatch(detailJob)">🎯 智能匹配</button>
        </div>
      </div>
    </div>

    <!-- 新增/编辑岗位模态框 -->
    <div class="modal-overlay" v-if="showModal" @click.self="showModal = false">
      <div class="modal" style="min-width:650px;">
        <h3>{{ isEditing ? '编辑岗位' : '发布新岗位' }}</h3>
        <div class="form-row">
          <div class="form-group">
            <label>岗位名称 *</label>
            <input v-model="form.title" placeholder="如：Java后端开发工程师" />
          </div>
          <div class="form-group">
            <label>公司名称 *</label>
            <input v-model="form.company" placeholder="如：阿里巴巴" />
          </div>
        </div>
        <div class="form-row">
          <div class="form-group">
            <label>工作地点</label>
            <input v-model="form.location" placeholder="如：北京" />
          </div>
          <div class="form-group">
            <label>薪资范围</label>
            <input v-model="form.salary" placeholder="如：15K-25K" />
          </div>
          <div class="form-group">
            <label>工作类型</label>
            <select v-model="form.type">
              <option value="">请选择</option>
              <option value="全职">全职</option>
              <option value="实习">实习</option>
              <option value="兼职">兼职</option>
            </select>
          </div>
        </div>
        <div class="form-group">
          <label>岗位描述</label>
          <textarea v-model="form.description" rows="4" placeholder="请描述岗位职责和工作内容..."></textarea>
        </div>
        <div class="form-group">
          <label>任职要求</label>
          <textarea v-model="form.requirements" rows="4" placeholder="请列出岗位要求..."></textarea>
        </div>
        <div class="modal-actions">
          <button class="btn btn-outline" @click="showModal = false">取消</button>
          <button class="btn btn-primary" @click="handleSubmit" :disabled="submitting">
            {{ submitting ? '提交中...' : (isEditing ? '保存修改' : '发布岗位') }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios'

export default {
  name: 'JobList',
  data() {
    return {
      jobs: [],
      searchTitle: '',
      searchCompany: '',
      searchType: '',
      searchLocation: '',
      showModal: false,
      showDetail: false,
      isEditing: false,
      submitting: false,
      isAdmin: true,
      detailJob: null,
      form: {
        id: null,
        title: '',
        company: '',
        description: '',
        requirements: '',
        salary: '',
        location: '',
        type: '',
      },
    }
  },
  computed: {
    filteredJobs() {
      return this.jobs.filter(j => {
        const matchTitle = !this.searchTitle || (j.title && j.title.includes(this.searchTitle))
        const matchCompany = !this.searchCompany || (j.company && j.company.includes(this.searchCompany))
        const matchType = !this.searchType || j.type === this.searchType
        const matchLocation = !this.searchLocation || j.location === this.searchLocation
        return matchTitle && matchCompany && matchType && matchLocation
      })
    },
    locations() {
      return [...new Set(this.jobs.map(j => j.location).filter(Boolean))]
    },
  },
  mounted() {
    this.fetchJobs()
  },
  methods: {
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
    openAddModal() {
      this.isEditing = false
      this.form = { id: null, title: '', company: '', description: '', requirements: '', salary: '', location: '', type: '' }
      this.showModal = true
    },
    openEditModal(job) {
      this.isEditing = true
      this.form = { ...job }
      this.showModal = true
    },
    viewJob(job) {
      this.detailJob = job
      this.showDetail = true
    },
    goMatch(job) {
      this.$router.push({ name: 'match', query: { jobId: job.id } })
    },
    async handleSubmit() {
      if (!this.form.title || !this.form.company) {
        window.$toast.warning('请输入岗位名称和公司名称')
        return
      }
      this.submitting = true
      try {
        const url = this.isEditing ? '/api/job/updateJob' : '/api/job/addJob'
        await axios.post(url, this.form)
        window.$toast.success(this.isEditing ? '更新成功！' : '发布成功！')
        this.showModal = false
        this.fetchJobs()
      } catch (e) {
        window.$toast.error('操作失败')
      } finally {
        this.submitting = false
      }
    },
    async handleDelete(id) {
      if (!confirm('确认删除该岗位吗？')) return
      try {
        await axios.get('/api/job/deleteJob', { params: { id } })
        window.$toast.success('删除成功！')
        this.fetchJobs()
      } catch (e) {
        window.$toast.error('删除失败')
      }
    },
    clearSearch() {
      this.searchTitle = ''
      this.searchCompany = ''
      this.searchType = ''
      this.searchLocation = ''
    },
  },
}
</script>
