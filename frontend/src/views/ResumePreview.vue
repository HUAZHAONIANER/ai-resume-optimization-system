<template>
  <div class="preview-root">
    <!-- 固定顶部工具栏 -->
    <header class="preview-toolbar no-print">
      <div class="tb-left">
        <button class="tb-btn tb-btn-back" @click="$router.back()">← 返回</button>
        <span class="tb-title">{{ resume.name || '简历预览' }}</span>
      </div>
      <div class="tb-actions">
        <button class="tb-btn tb-btn-print" @click="printResume">🖨 打印</button>
        <div class="download-dropdown" ref="dropdownRef">
          <button class="tb-btn tb-btn-download" @click="toggleDropdown">
            📥 下载 <span class="dropdown-arrow">▾</span>
          </button>
          <div class="dropdown-menu" v-if="showDropdown">
            <button class="dropdown-item" @click="downloadAsPdf">
              <span class="item-icon">📄</span> PDF 文件 (.pdf)
            </button>
            <button class="dropdown-item" @click="downloadAsTxt">
              <span class="item-icon">📝</span> 纯文本 (.txt)
            </button>
          </div>
        </div>
      </div>
    </header>

    <!-- 加载 / 空状态 -->
    <div class="preview-state" v-if="loading">
      <div class="state-spinner"></div>
      <p>正在加载简历...</p>
    </div>
    <div class="preview-state" v-else-if="!resume.id">
      <div class="state-icon">📄</div>
      <p>未找到该简历</p>
      <button class="btn-back" @click="$router.push('/resume')">返回简历管理</button>
    </div>

    <!-- 简历正文 -->
    <article class="resume-paper" ref="resumePaper" v-else>
      <!-- 姓名头部 -->
      <header class="rp-header">
        <h1 class="rp-name">{{ resume.name || '未命名简历' }}</h1>
      </header>

      <!-- 所有模块平级排列 -->
      <div class="rp-modules">
        <!-- 教育背景 -->
        <section class="rp-card" v-if="resume.school || resume.major || resume.education">
          <h2 class="rp-card-title">教育背景</h2>
          <div class="rp-card-body">
            <div class="edu-row" v-if="resume.school">
              <span class="edu-label">院校</span>
              <span class="edu-value">{{ resume.school }}</span>
            </div>
            <div class="edu-row" v-if="resume.major">
              <span class="edu-label">专业</span>
              <span class="edu-value">{{ resume.major }}</span>
            </div>
            <div class="edu-row" v-if="resume.education">
              <span class="edu-label">学历</span>
              <span class="edu-value">{{ resume.education }}</span>
            </div>
          </div>
        </section>

        <!-- 专业技能 -->
        <section class="rp-card" v-if="skillList.length > 0">
          <h2 class="rp-card-title">专业技能</h2>
          <div class="rp-card-body">
            <div class="rp-skills">
              <span v-for="(s, i) in skillList" :key="i" class="rp-skill-tag">{{ s }}</span>
            </div>
          </div>
        </section>

        <!-- 项目与实习经历 -->
        <section class="rp-card" v-if="resume.experience">
          <h2 class="rp-card-title">项目与实习经历</h2>
          <div class="rp-card-body">
            <div class="rp-text" v-html="formatContent(resume.experience)"></div>
          </div>
        </section>

        <!-- 从正文中解析出的各个模块 -->
        <section
          class="rp-card"
          v-for="(sec, idx) in parsedSections"
          :key="'sec-' + idx"
        >
          <h2 class="rp-card-title">{{ sec.title }}</h2>
          <div class="rp-card-body">
            <div class="rp-text" v-html="sec.html"></div>
          </div>
        </section>

        <!-- 空内容 -->
        <div class="rp-empty" v-if="!hasAnyContent">
          <p>暂无可显示的简历内容</p>
          <p class="rp-empty-hint">请先完善简历信息或使用优化功能生成内容</p>
        </div>
      </div>
    </article>
  </div>
</template>

<script>
import axios from 'axios'
// html2pdf.js 在 downloadAsPdf 中动态导入，避免增大首屏加载体积

/* 用于从正文中拆分段落的标题关键词 */
const SECTION_KEYS = [
  '教育背景', '工作经历', '实习经历', '项目经历', '项目经验',
  '在校经历', '校园经历', '社会实践', '获奖情况', '所获荣誉',
  '证书资质', '语言能力', '自我评价', '求职意向', '联系方式',
  '个人信息', '技能特长', '科研经历', '发表论文', '竞赛经历',
]

export default {
  name: 'ResumePreview',
  data() {
    return {
      resume: {
        id: null, name: '', education: '', school: '', major: '',
        skills: '', experience: '', content: '', optimizedContent: '', createTime: '',
      },
      loading: true,
      showDropdown: false,
    }
  },
  computed: {
    skillList() {
      if (!this.resume.skills) return []
      return this.resume.skills.split(/[,，;；、\s]+/).filter(s => s.trim())
    },
    displayBody() {
      return this.resume.optimizedContent || this.resume.content || ''
    },
    hasAnyContent() {
      return !!(this.resume.school || this.resume.major || this.resume.education ||
        this.skillList.length || this.resume.experience || this.displayBody)
    },
    /* 将正文按标题拆分为独立模块 */
    parsedSections() {
      const text = this.displayBody
      if (!text) return []
      const cleaned = this.cleanText(text)
      // 构建匹配模式
      const pattern = new RegExp(
        '\\n(?=' + SECTION_KEYS.map(k => this.escapeRegex(k)).join('|') + ')',
        'g'
      )
      const chunks = cleaned.split(pattern).filter(c => c.trim())
      return chunks.map(chunk => {
        const lines = chunk.trim().split(/\n/)
        const first = lines[0].trim()
        // 判断首行是否为已知标题
        let title = '个人信息'
        let bodyStart = 0
        for (const key of SECTION_KEYS) {
          if (first.startsWith(key) || first.replace(/^[#（(]\s*/, '').startsWith(key)) {
            title = key
            bodyStart = 1
            break
          }
        }
        const body = bodyStart ? lines.slice(1).join('\n').trim() : chunk.trim()
        return { title, html: this.buildHtml(body) }
      })
    },
  },
  mounted() {
    this.fetchResume()
    document.addEventListener('click', this.handleClickOutside)
  },
  beforeUnmount() {
    document.removeEventListener('click', this.handleClickOutside)
  },
  methods: {
    async fetchResume() {
      const id = this.$route.params.id
      if (!id) { this.loading = false; return }
      try {
        const res = await axios.get('/api/resume/getResume')
        const data = res.data
        let list = []
        if (Array.isArray(data)) list = data
        else if (data && Array.isArray(data.data)) list = data.data
        else if (data && Array.isArray(data.records)) list = data.records
        const found = list.find(r => String(r.id) === String(id))
        if (found) this.resume = found
      } catch (e) {
        console.error('获取简历详情失败', e)
      } finally {
        this.loading = false
      }
    },

    escapeRegex(s) { return s.replace(/[.*+?^${}()|[\]\\]/g, '\\$&') },

    /* ---------- 内容格式化 ---------- */
    formatContent(text) {
      if (!text) return ''
      return this.buildHtml(this.cleanText(text))
    },

    cleanText(text) {
      if (!text) return text
      let t = text
      t = t.replace(/```[a-zA-Z]*\s*/g, '').replace(/```/g, '')
      t = t.replace(/\*\*(.+?)\*\*/g, '$1').replace(/__(.+?)__/g, '$1')
      t = t.replace(/\*(.+?)\*/g, '$1').replace(/_(.+?)_/g, '$1')
      t = t.replace(/~~(.+?)~~/g, '$1').replace(/`(.+?)`/g, '$1')
      t = t.replace(/^#{1,6}\s+/gm, '').replace(/^[-*_]{3,}\s*$/gm, '')
      t = t.replace(/[（(][^）)]*[）)]/g, '')
      t = t.replace(/\[([^\]]*)\]/g, ' $1 ')
      t = t.replace(/\([^)]*\)/g, '')
      t = t.replace(/[ \t]{2,}/g, ' ')
      t = t.replace(/^[ \t]+/gm, '').replace(/[ \t]+$/gm, '')
      t = t.replace(/\n{4,}/g, '\n\n')
      return t.trim()
    },

    buildHtml(text) {
      if (!text) return ''
      const lines = text.split(/\n+/)
      const out = []
      for (const raw of lines) {
        const line = raw.trim()
        if (!line) continue
        const escaped = this.escapeHtml(line)
        if (/^[-•·►□▸▪◆●○✓✔✅⭐🔹🔸]/.test(line)) {
          out.push(`<li>${escaped.replace(/^[-•·►□▸▪◆●○✓✔✅⭐🔹🔸]\s*/, '')}</li>`)
        } else if (/^\d+[.)]\s/.test(line)) {
          out.push(`<li>${escaped.replace(/^\d+[.)]\s*/, '')}</li>`)
        } else {
          out.push(`<p>${escaped}</p>`)
        }
      }
      let html = out.join('\n')
      html = html.replace(/(<li>[\s\S]*?<\/li>)\n?(?=<li>)/g, '$1')
      html = html.replace(/((?:<li>[\s\S]*?<\/li>\n?)+)/g, '<ul>$1</ul>')
      return html
    },

    escapeHtml(text) {
      const m = { '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }
      return text.replace(/[&<>"']/g, c => m[c])
    },

    /* ---------- 打印 ---------- */
    printResume() { window.print() },

    /* ---------- 下载下拉 ---------- */
    toggleDropdown() { this.showDropdown = !this.showDropdown },
    handleClickOutside(e) {
      if (this.$refs.dropdownRef && !this.$refs.dropdownRef.contains(e.target)) {
        this.showDropdown = false
      }
    },

    /* ---------- 下载 PDF ---------- */
    async downloadAsPdf() {
      this.showDropdown = false
      const html2pdf = (await import('html2pdf.js')).default
      const r = this.resume
      const body = this.displayBody

      const html = `<div style="font-family:'PingFang SC','Microsoft YaHei','Noto Sans SC',sans-serif;color:#2c2c2c;line-height:1.8;background:#fff;padding:40px 48px;max-width:720px;margin:0 auto;">

  <div style="text-align:center;padding-bottom:24px;margin-bottom:32px;border-bottom:2.5px solid #3a3a3a;">
    <h1 style="font-size:24pt;font-weight:700;color:#1a1a1a;letter-spacing:3px;margin:0;">${this.escapeHtml(r.name || '未命名简历')}</h1>
  </div>

  ${(r.school || r.major || r.education) ? `
  <div style="padding:24px 0;border-bottom:1px solid #eaeaea;">
    <h2 style="font-size:13pt;font-weight:700;color:#1a1a1a;margin:0 0 14px;padding-bottom:10px;border-bottom:1px solid #eaeaea;">教育背景</h2>
    ${r.school ? `<div style="display:flex;padding:7px 0;"><span style="width:56px;font-size:11pt;font-weight:600;color:#666;">院校</span><span style="font-size:11.5pt;font-weight:600;color:#1a1a1a;">${this.escapeHtml(r.school)}</span></div>` : ''}
    ${r.major ? `<div style="display:flex;padding:7px 0;"><span style="width:56px;font-size:11pt;font-weight:600;color:#666;">专业</span><span style="font-size:11.5pt;font-weight:600;color:#1a1a1a;">${this.escapeHtml(r.major)}</span></div>` : ''}
    ${r.education ? `<div style="display:flex;padding:7px 0;"><span style="width:56px;font-size:11pt;font-weight:600;color:#666;">学历</span><span style="font-size:11.5pt;font-weight:600;color:#1a1a1a;">${this.escapeHtml(r.education)}</span></div>` : ''}
  </div>` : ''}

  ${this.skillList.length ? `
  <div style="padding:24px 0;border-bottom:1px solid #eaeaea;">
    <h2 style="font-size:13pt;font-weight:700;color:#1a1a1a;margin:0 0 14px;padding-bottom:10px;border-bottom:1px solid #eaeaea;">专业技能</h2>
    <div>${this.skillList.map(s => `<span style="display:inline-block;padding:5px 18px;margin:0 10px 10px 0;font-size:11pt;font-weight:600;color:#3a3a3a;background:#f4f4f4;">${this.escapeHtml(s)}</span>`).join('')}</div>
  </div>` : ''}

  ${r.experience ? `
  <div style="padding:24px 0;border-bottom:1px solid #eaeaea;">
    <h2 style="font-size:13pt;font-weight:700;color:#1a1a1a;margin:0 0 14px;padding-bottom:10px;border-bottom:1px solid #eaeaea;">项目与实习经历</h2>
    <div style="font-size:11.5pt;color:#333;line-height:1.95;">${this.buildDocBody(r.experience)}</div>
  </div>` : ''}

  ${body ? this.buildDocSectionsInline(body) : ''}

</div>`

      // 创建可见容器（html2canvas 要求元素在可视区域内）
      const wrapper = document.createElement('div')
      wrapper.style.cssText = 'position:fixed;top:0;left:0;right:0;bottom:0;z-index:99999;background:#fff;overflow:auto;display:flex;justify-content:center;'
      const el = document.createElement('div')
      el.innerHTML = html
      el.style.cssText = 'width:720px;padding:40px 48px;background:#fff;'
      wrapper.appendChild(el)
      document.body.appendChild(wrapper)

      // 等待渲染完成
      await new Promise(r => setTimeout(r, 300))

      html2pdf().set({
        margin: [10, 10, 10, 10],
        filename: `${r.name || '简历'}.pdf`,
        image: { type: 'jpeg', quality: 0.98 },
        html2canvas: { scale: 2, useCORS: true, logging: false },
        jsPDF: { unit: 'mm', format: 'a4', orientation: 'portrait' },
        pagebreak: { mode: ['avoid-all'] },
      }).from(el).save().then(() => {
        document.body.removeChild(wrapper)
      })
    },

    /* 用内联样式生成正文模块（供 PDF 使用） */
    buildDocSectionsInline(text) {
      const cleaned = this.cleanText(text)
      const pattern = new RegExp(
        '\\n(?=' + SECTION_KEYS.map(k => this.escapeRegex(k)).join('|') + ')', 'g'
      )
      const chunks = cleaned.split(pattern).filter(c => c.trim())
      return chunks.map(chunk => {
        const lines = chunk.trim().split(/\n/)
        const first = lines[0].trim()
        let title = '个人信息'
        let bodyStart = 0
        for (const key of SECTION_KEYS) {
          if (first.startsWith(key) || first.replace(/^[#（(]\s*/, '').startsWith(key)) {
            title = key; bodyStart = 1; break
          }
        }
        const b = bodyStart ? lines.slice(1).join('\n').trim() : chunk.trim()
        return `<div style="padding:24px 0;border-bottom:1px solid #eaeaea;">
          <h2 style="font-size:13pt;font-weight:700;color:#1a1a1a;margin:0 0 14px;padding-bottom:10px;border-bottom:1px solid #eaeaea;">${this.escapeHtml(title)}</h2>
          <div style="font-size:11.5pt;color:#333;line-height:1.95;">${this.buildDocBody(b)}</div>
        </div>`
      }).join('\n')
    },

    /* 将正文拆为模块 */
    buildDocSections(text) {
      const cleaned = this.cleanText(text)
      const pattern = new RegExp(
        '\\n(?=' + SECTION_KEYS.map(k => this.escapeRegex(k)).join('|') + ')', 'g'
      )
      const chunks = cleaned.split(pattern).filter(c => c.trim())
      return chunks.map(chunk => {
        const lines = chunk.trim().split(/\n/)
        const first = lines[0].trim()
        let title = '个人信息'
        let bodyStart = 0
        for (const key of SECTION_KEYS) {
          if (first.startsWith(key) || first.replace(/^[#（(]\s*/, '').startsWith(key)) {
            title = key; bodyStart = 1; break
          }
        }
        const body = bodyStart ? lines.slice(1).join('\n').trim() : chunk.trim()
        return `<div class="section">
          <h2 class="sec-title">${this.escapeHtml(title)}</h2>
          <div class="body">${this.buildDocBody(body)}</div>
        </div>`
      }).join('\n')
    },

    buildDocBody(text) {
      if (!text) return ''
      const cleaned = this.cleanText(text)
      const lines = cleaned.split(/\n+/)
      const out = []
      for (const raw of lines) {
        const line = raw.trim()
        if (!line) continue
        const esc = this.escapeHtml(line)
        if (/^[-•·►□▸▪◆●○✓✔✅⭐🔹🔸]/.test(line)) {
          out.push(`<li>${esc.replace(/^[-•·►□▸▪◆●○✓✔✅⭐🔹🔸]\s*/, '')}</li>`)
        } else if (/^\d+[.)]\s/.test(line)) {
          out.push(`<li>${esc.replace(/^\d+[.)]\s*/, '')}</li>`)
        } else if (/^[#（(]?(教育背景|工作经历|实习经历|项目经验|项目经历|技能|自我评价|联系方式|个人信息|获奖|证书|语言|求职意向|在校经历|校园经历|社会实践)/.test(line)) {
          out.push(`<p class="sub-h">${esc}</p>`)
        } else {
          out.push(`<p>${esc}</p>`)
        }
      }
      let html = out.join('\n')
      html = html.replace(/(<li>[\s\S]*?<\/li>)\n?(?=<li>)/g, '$1')
      html = html.replace(/((?:<li>[\s\S]*?<\/li>\n?)+)/g, '<ul>$1</ul>')
      return html
    },

    /* ---------- 下载 TXT ---------- */
    downloadAsTxt() {
      this.showDropdown = false
      const r = this.resume
      const lines = [r.name || '未命名简历', '']
      if (r.school || r.major || r.education) {
        lines.push('教育背景')
        if (r.school) lines.push(`  院校：${r.school}`)
        if (r.major) lines.push(`  专业：${r.major}`)
        if (r.education) lines.push(`  学历：${r.education}`)
        lines.push('')
      }
      if (r.skills) { lines.push('专业技能'); lines.push(r.skills); lines.push('') }
      if (r.experience) { lines.push('项目与实习经历'); lines.push(this.cleanText(r.experience)); lines.push('') }
      if (this.displayBody) { lines.push(this.cleanText(this.displayBody)); lines.push('') }
      this.triggerDownload(`${r.name || '简历'}.txt`, lines.join('\n'), 'text/plain;charset=utf-8')
    },

    triggerDownload(filename, content, mime) {
      const blob = new Blob(['﻿' + content], { type: mime })
      const url = URL.createObjectURL(blob)
      const a = document.createElement('a')
      a.href = url; a.download = filename
      document.body.appendChild(a); a.click(); document.body.removeChild(a)
      URL.revokeObjectURL(url)
    },
  },
}
</script>

<style scoped>
/* ========== 固定顶部工具栏 ========== */
.preview-toolbar {
  position: fixed; top: 0; left: 0; right: 0; z-index: 100;
  display: flex; align-items: center; justify-content: space-between;
  height: 52px; padding: 0 24px;
  background: rgba(255,255,255,.9); backdrop-filter: blur(12px);
  border-bottom: 1px solid rgba(0,0,0,.06);
  box-shadow: 0 1px 6px rgba(0,0,0,.04);
}
.tb-left { display: flex; align-items: center; gap: 14px; min-width: 0; }
.tb-title {
  font-size: 14px; font-weight: 600; color: #333;
  overflow: hidden; text-overflow: ellipsis; white-space: nowrap; max-width: 360px;
}
.tb-actions { display: flex; gap: 8px; align-items: center; flex-shrink: 0; }
.tb-btn {
  display: inline-flex; align-items: center; gap: 5px; padding: 7px 16px;
  border: none; border-radius: 7px; font-size: 13px; font-weight: 500;
  font-family: inherit; cursor: pointer; transition: all .15s; white-space: nowrap;
}
.tb-btn-back { background: #f0f2f5; color: #555; }
.tb-btn-back:hover { background: #e2e5ea; }
.tb-btn-print { background: #e8f0fe; color: #1a73e8; }
.tb-btn-print:hover { background: #d2e3fc; }
.tb-btn-download { background: #1a73e8; color: #fff; }
.tb-btn-download:hover { background: #1557b0; }

.download-dropdown { position: relative; }
.dropdown-arrow { font-size: 10px; margin-left: 2px; }
.dropdown-menu {
  position: absolute; right: 0; top: calc(100% + 6px);
  background: #fff; border-radius: 8px; box-shadow: 0 8px 30px rgba(0,0,0,.12);
  min-width: 180px; overflow: hidden; z-index: 60; animation: dropIn .15s ease;
}
@keyframes dropIn {
  from { opacity: 0; transform: translateY(-6px); }
  to   { opacity: 1; transform: translateY(0); }
}
.dropdown-item {
  display: flex; align-items: center; gap: 8px; width: 100%;
  padding: 12px 16px; border: none; background: none;
  font-size: 13px; font-family: inherit; color: #333;
  cursor: pointer; transition: background .12s; text-align: left;
}
.dropdown-item:hover { background: #f0f5ff; }
.item-icon { font-size: 16px; }

/* ========== 根容器 ========== */
.preview-root { padding-top: 72px; padding-bottom: 48px; }

/* ========== 简历纸 ========== */
.resume-paper {
  max-width: 760px; margin: 0 auto; background: #fff;
  padding: 48px 56px 44px;
  box-shadow: 0 4px 32px rgba(0,0,0,.07), 0 1px 4px rgba(0,0,0,.03);
  border-radius: 4px;
}

/* ========== 姓名头部 ========== */
.rp-header {
  text-align: center; padding-bottom: 24px; margin-bottom: 32px;
  border-bottom: 2px solid #333;
}
.rp-name {
  font-size: 26px; font-weight: 700; color: #1a1a1a;
  letter-spacing: 2px; line-height: 1.3;
}

/* ========== 模块卡片 ========== */
.rp-modules {
  display: flex; flex-direction: column;
}

.rp-card {
  padding: 26px 0;
  border-bottom: 1px solid rgba(0,0,0,.07);
}
.rp-card:last-child { border-bottom: none; }

.rp-card-title {
  font-size: 16px; font-weight: 700; color: #1a1a1a;
  padding-left: 12px; margin-bottom: 16px;
  border-left: 3px solid #555;
  line-height: 1.4;
}

.rp-card-body { font-size: 14px; color: #444; line-height: 1.9; }

/* ========== 教育背景 ========== */
.edu-row {
  display: flex; align-items: baseline; margin-bottom: 8px;
  padding: 6px 0 6px 12px;
  border-left: 2px solid transparent;
  transition: border-color .15s;
}
.edu-label {
  color: #666; font-size: 13px; min-width: 52px; flex-shrink: 0;
  font-weight: 600; letter-spacing: .5px;
}
.edu-value { color: #1a1a1a; font-size: 14px; font-weight: 600; }

/* ========== 技能标签 ========== */
.rp-skills { display: flex; flex-wrap: wrap; gap: 10px; }
.rp-skill-tag {
  padding: 5px 16px;
  background: #f7f7f7; color: #333;
  border: 1px solid rgba(0,0,0,.08);
  border-radius: 3px;
  font-size: 13px; font-weight: 600;
  transition: all .15s;
}
.rp-skill-tag:hover {
  background: #eee; color: #1a1a1a;
}

/* ========== 正文文本 ========== */
.rp-text { font-size: 14px; color: #444; line-height: 1.9; }
.rp-text :deep(p) {
  margin-bottom: 10px;
  padding-left: 2px;
}
.rp-text :deep(ul) {
  margin: 6px 0 16px;
  padding-left: 0;
  list-style: none;
}
.rp-text :deep(li) {
  position: relative;
  padding: 4px 0 4px 18px;
  margin-bottom: 2px;
  line-height: 1.8;
  border-left: 2px solid transparent;
}
.rp-text :deep(li::before) {
  content: '';
  position: absolute; left: 0; top: 14px;
  width: 5px; height: 5px;
  background: #bbb;
  border-radius: 50%;
}

/* 正文内小节标题 — 加黑突出 */
.rp-text :deep(.rp-subtitle) {
  font-size: 14px; font-weight: 700; color: #1a1a1a;
  margin: 18px 0 10px;
  padding: 4px 0 4px 10px;
  border-left: 3px solid #555;
  line-height: 1.5;
}

/* ========== 空内容 ========== */
.rp-empty { text-align: center; padding: 60px 20px; color: #aaa; font-size: 14px; }
.rp-empty-hint { font-size: 13px; margin-top: 6px; color: #ccc; }

/* ========== 加载/空状态 ========== */
.preview-state {
  text-align: center; padding: 120px 20px 80px; color: #999;
  font-size: 15px; display: flex; flex-direction: column; align-items: center; gap: 16px;
}
.state-icon { font-size: 64px; }
.state-spinner {
  width: 36px; height: 36px; border: 3px solid #e8e8e8;
  border-top-color: #1a73e8; border-radius: 50%; animation: spin .7s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }
.btn-back {
  margin-top: 8px; padding: 10px 24px; background: #1a73e8;
  color: #fff; border: none; border-radius: 6px;
  font-size: 14px; cursor: pointer; font-family: inherit;
}
.btn-back:hover { background: #1557b0; }

/* ========== 响应式 ========== */
@media (max-width: 700px) {
  .preview-toolbar { padding: 0 14px; }
  .tb-title { max-width: 160px; font-size: 13px; }
  .tb-btn { padding: 6px 12px; font-size: 12px; }
  .resume-paper { padding: 28px 16px 32px; border-radius: 0; box-shadow: none; }
  .rp-name { font-size: 22px; }
  .rp-card { padding: 18px 0; }
}

/* ========== 打印 ========== */
@media print {
  .no-print { display: none !important; }
  body { background: #fff; margin: 0; padding: 0; }
  .page-container, .page-container-preview { max-width: 100%; margin: 0; padding: 0; }
  .preview-root { padding-top: 0; padding-bottom: 0; max-width: 100%; }
  .resume-paper { box-shadow: none; border-radius: 0; max-width: 100%; padding: 20px 32px; }
  .rp-card { border-bottom: 1px solid rgba(0,0,0,.08); break-inside: avoid; }
}
</style>
