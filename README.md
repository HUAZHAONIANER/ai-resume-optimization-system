# 基于AI大模型的大学生简历智能优化与岗位匹配系统

## 📋 项目概述

本系统是一个基于AI大模型的大学生简历智能优化与岗位匹配平台，旨在帮助大学生提升求职竞争力。系统整合了**阿里云通义千问大模型**，实现简历的智能优化和岗位的精准匹配分析。

## 🏗️ 系统架构

### 整体架构图

```
┌─────────────────────────────────────────────────────────────────┐
│                        前端展示层 (Vue 3)                        │
│  ┌────────────┬──────────────┬──────────────┬──────────────┐   │
│  │ 登录注册   │ 简历管理     │ AI优化       │ 岗位匹配     │   │
│  └────────────┴──────────────┴──────────────┴──────────────┘   │
│                            │                                    │
│                            ▼                                    │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │                    API 网关层 (CORS)                      │   │
│  └──────────────────────────────────────────────────────────┘   │
│                            │                                    │
└────────────────────────────┼────────────────────────────────────┘
                             ▼
┌─────────────────────────────────────────────────────────────────┐
│                      后端服务层 (Spring Boot)                    │
│  ┌────────────┬──────────────┬──────────────┬──────────────┐   │
│  │ UserCtrl   │ ResumeCtrl   │ AiController │ JobController│   │
│  │ 用户管理   │ 简历管理     │ AI服务       │ 岗位管理     │   │
│  └────────────┴──────────────┴──────────────┴──────────────┘   │
│                            │                                    │
│        ┌───────────────────┼───────────────────┐               │
│        ▼                   ▼                   ▼               │
│  ┌───────────┐      ┌─────────────┐      ┌─────────────┐      │
│  │ Resume    │      │ Match       │      │ LangChain4j │      │
│  │ Assistant │      │ Assistant   │      │  AI服务层   │      │
│  └───────────┘      └─────────────┘      └─────────────┘      │
└────────────────────────────┬────────────────────────────────────┘
                             │
        ┌────────────────────┼────────────────────┐
        ▼                    ▼                    ▼
┌──────────────┐    ┌──────────────┐    ┌──────────────┐
│   MySQL      │    │  MongoDB     │    │  阿里云      │
│ 关系型数据   │    │ 文档型数据   │    │ 通义千问     │
│ 用户/岗位/   │    │ 匹配结果     │    │ 大模型      │
│ 简历         │    │ 存储         │    │             │
└──────────────┘    └──────────────┘    └──────────────┘
```

### 技术栈详解

| 层次           | 技术        | 版本   | 说明                     |
| -------------- | ----------- | ------ | ------------------------ |
| **前端框架**   | Vue         | 3.5.38 | 渐进式JavaScript框架     |
| **路由管理**   | Vue Router  | 5.1.0  | 单页应用路由控制         |
| **HTTP客户端** | Axios       | 1.18.1 | 异步请求处理             |
| **构建工具**   | Vite        | 8.0.16 | 下一代前端构建工具       |
| **后端框架**   | Spring Boot | 3.5.16 | Java企业级开发框架       |
| **ORM框架**    | MyBatis     | 3.0.3  | 持久层框架               |
| **AI框架**     | LangChain4j | 1.15.0 | LLM应用开发框架          |
| **关系数据库** | MySQL       | 8.0.27 | 用户、岗位、简历数据存储 |
| **文档数据库** | MongoDB     | 6.x    | 匹配分析结果存储         |
| **AI大模型**   | 通义千问    |qwen3-max| 阿里云百炼平台           |
| **编程语言**   | Java        | 17     | LTS版本                  |

### 核心模块

#### 1. 用户管理模块

- **功能**: 用户注册、登录、信息管理
- **设计**: 基于角色的访问控制(RBAC)，支持学生和管理员角色
- **安全性**: 密码验证、登录状态持久化

#### 2. 简历管理模块

- **功能**: 简历创建、上传、查看、更新、删除
- **特性**: 支持文本简历和文件上传(.txt)
- **数据模型**:
  ```
  Resume {
    id, userId, name, education, school,
    major, skills, experience, content,
    filePath, optimizedContent, createTime
  }
  ```

#### 3. AI简历优化模块

- **核心服务**: `ResumeAssistant` AI服务接口
- **优化原则**:
  - 使用专业、简洁、有力的语言描述经历和技能
  - 突出与目标岗位相关的核心竞争力
  - 量化成果，使用具体数据和指标
  - 优化简历结构和排版
  - 使用行业关键词提升ATS筛选通过率
- **技术实现**: 基于LangChain4j的`@AiService`注解，自动生成AI服务代理

#### 4. 岗位管理模块

- **功能**: 岗位增删改查、条件筛选
- **数据模型**:
  ```
  Job {
    id, title, company, description,
    requirements, salary, location, type, createTime
  }
  ```

#### 5. AI岗位匹配模块

- **核心服务**: `MatchAssistant` AI服务接口
- **匹配分析输出**:
  - 综合匹配度评分 (1-100分)
  - 优势分析 (至少3条)
  - 差距分析 (至少2条)
  - 技能匹配详情
  - 改进建议 (至少3条)
  - 面试准备建议
- **实现机制**: 将简历内容与岗位描述组合后发送给AI大模型，获取结构化分析报告

## 🚀 快速开始

### 环境要求

- **JDK**: 17+
- **Node.js**: 18+
- **MySQL**: 8.0+
- **MongoDB**: 6.0+
- **Maven**: 3.8+

### 后端部署

#### 1. 数据库配置

创建MySQL数据库：

```sql
CREATE DATABASE resume_ai CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

MongoDB无需手动创建数据库，系统会自动创建。

#### 2. 配置文件修改

编辑 `src/main/resources/application.yaml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/resume_ai?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai
    username: your_username
    password: your_password
  data:
    mongodb:
      uri: mongodb://localhost:27017/resume_ai_db
```

配置AI大模型（需在阿里云百炼平台申请API Key）：

编辑 `src/main/resources/application.properties`：

```properties
langchain4j.dashscope.chat-model.api-key=your_api_key
langchain4j.dashscope.chat-model.model=qwen-max
```

#### 3. 启动后端服务

```bash
cd ResumeAISpringBoot
mvn spring-boot:run
```

服务启动后访问：`http://localhost:18080`

### 前端部署

#### 1. 安装依赖

```bash
cd ResumeAIVue
npm install
```

#### 2. 配置API地址

编辑 `src/main.js` 或相关配置文件，确保API地址正确指向后端服务。

#### 3. 启动开发服务器

```bash
npm run dev
```

访问前端页面：`http://localhost:5173`

#### 4. 构建生产版本

```bash
npm run build
```

构建产物位于 `dist/` 目录。

## 📖 API接口文档

### 用户模块

| 方法 | 路径                | 描述           |
| ---- | ------------------- | -------------- |
| POST | `/user/login`       | 用户登录       |
| POST | `/user/register`    | 用户注册       |
| GET  | `/user/getUser`     | 获取所有用户   |
| GET  | `/user/getUserById` | 根据ID获取用户 |
| POST | `/user/update`      | 更新用户信息   |
| GET  | `/user/delete`      | 删除用户       |

### 简历模块

| 方法 | 路径                   | 描述               |
| ---- | ---------------------- | ------------------ |
| GET  | `/resume/getResume`    | 获取所有简历       |
| GET  | `/resume/getByUserId`  | 根据用户ID获取简历 |
| GET  | `/resume/getById`      | 根据ID获取简历详情 |
| POST | `/resume/addResume`    | 新增简历           |
| POST | `/resume/updateResume` | 更新简历           |
| GET  | `/resume/deleteResume` | 删除简历           |
| POST | `/resume/uploadFile`   | 上传简历文件       |

### AI服务模块

| 方法 | 路径                    | 描述         |
| ---- | ----------------------- | ------------ |
| POST | `/ai/optimizeResume`    | AI简历优化   |
| POST | `/ai/matchJob`          | AI岗位匹配   |
| GET  | `/ai/getMatchHistory`   | 获取匹配历史 |
| GET  | `/ai/deleteMatchResult` | 删除匹配记录 |

### 岗位模块

| 方法 | 路径             | 描述           |
| ---- | ---------------- | -------------- |
| GET  | `/job/getJob`    | 获取所有岗位   |
| GET  | `/job/getById`   | 根据ID获取岗位 |
| POST | `/job/addJob`    | 新增岗位       |
| POST | `/job/updateJob` | 更新岗位       |
| GET  | `/job/deleteJob` | 删除岗位       |

## 📁 项目结构

```
基于AI大模型的大学生简历智能优化与岗位匹配系统/
├── backend/                        # Spring Boot后端
│   ├── src/main/java/org/example/resumeai/
│   │   ├── ResumeAiApplication.java    # 启动类
│   │   ├── common/                     # 通用组件
│   │   │   └── Result.java             # 统一响应封装
│   │   ├── config/                     # 配置类
│   │   │   ├── AiConfig.java           # AI配置
│   │   │   └── WebConfig.java          # Web配置
│   │   ├── controller/                 # 控制器层
│   │   │   ├── AiController.java       # AI服务接口
│   │   │   ├── JobController.java      # 岗位管理接口
│   │   │   ├── ResumeController.java   # 简历管理接口
│   │   │   └── UserController.java     # 用户管理接口
│   │   ├── dao/                        # 数据访问层
│   │   │   ├── JobDao.java
│   │   │   ├── MatchResultDao.java
│   │   │   ├── ResumeDao.java
│   │   │   └── UserDao.java
│   │   ├── entity/                     # 实体类
│   │   │   ├── Job.java
│   │   │   ├── MatchResult.java
│   │   │   ├── Resume.java
│   │   │   └── User.java
│   │   └── service/                    # 服务层
│   │       ├── MatchAssistant.java     # 岗位匹配AI服务
│   │       └── ResumeAssistant.java    # 简历优化AI服务
│   ├── src/main/resources/
│   │   ├── mapper/                     # MyBatis映射文件
│   │   ├── application.yaml            # 应用配置
│   │   ├── application.properties      # AI配置
│   │   └── init.sql                    # 初始化脚本
│   └── pom.xml                         # Maven依赖
├── frontend/                       # Vue 3前端
│   ├── src/
│   │   ├── views/                      # 页面组件
│   │   │   ├── Home.vue                # 首页
│   │   │   ├── Login.vue               # 登录页
│   │   │   ├── ResumeManage.vue        # 简历管理
│   │   │   ├── ResumeOptimize.vue      # 简历优化
│   │   │   ├── ResumePreview.vue       # 简历预览
│   │   │   ├── JobList.vue             # 岗位列表
│   │   │   └── JobMatch.vue            # 岗位匹配
│   │   ├── router/
│   │   │   └── index.js                # 路由配置
│   │   ├── assets/
│   │   │   └── main.css                # 全局样式
│   │   ├── App.vue                     # 根组件
│   │   └── main.js                     # 入口文件
│   ├── index.html
│   ├── package.json
│   └── vite.config.js
└── README.md                        # 项目说明文档
```

## 🔧 核心技术实现

### LangChain4j AI服务集成

系统采用LangChain4j框架实现AI大模型的集成，通过`@AiService`注解声明AI服务接口：

```java
@AiService
public interface ResumeAssistant {
    @SystemMessage("""
            你是一位专业的简历优化专家...
            优化原则：
            1. 使用专业、简洁、有力的语言描述经历和技能
            2. 突出与目标岗位相关的核心竞争力
            ...
            """)
    String optimizeResume(@UserMessage String resumeContent);
}
```

Spring Boot自动扫描并创建代理实现，开发者无需手动编写调用代码。

### 双数据库架构

系统采用MySQL和MongoDB双数据库架构：

- **MySQL**: 存储结构化数据（用户、岗位、简历）
- **MongoDB**: 存储非结构化数据（AI匹配分析结果）

这种设计充分发挥了两种数据库的优势，提高了系统的灵活性和扩展性。

### 响应式前端设计

前端采用Vue 3 Composition API，实现组件化开发和状态管理：

- **路由守卫**: 全局登录状态检查
- **异步请求**: Axios封装统一处理
- **PDF导出**: html2pdf.js实现简历PDF生成

## 🎯 功能特性

### 简历智能优化

- ✅ AI自动优化简历语言表达
- ✅ 量化成果，提升简历竞争力
- ✅ 行业关键词智能添加
- ✅ 一键对比优化前后差异

### 岗位智能匹配

- ✅ 综合匹配度评分
- ✅ 优势与差距分析
- ✅ 技能匹配详情展示
- ✅ 个性化改进建议
- ✅ 面试准备指导

### 用户体验

- ✅ 简洁直观的UI设计
- ✅ 完整的登录注册流程
- ✅ 简历文件上传支持
- ✅ 匹配历史记录管理
- ✅ 简历PDF导出功能

## 📝 使用流程

1. **注册/登录**: 访问首页进行注册或登录
2. **创建简历**: 在简历管理页面创建或上传简历
3. **智能优化**: 选择简历进行AI智能优化
4. **浏览岗位**: 在岗位列表查看可用岗位
5. **岗位匹配**: 选择简历和岗位进行智能匹配分析
6. **查看结果**: 获取匹配分析报告和改进建议

## 🔮 未来规划

- [ ] 支持更多格式的简历文件上传（PDF、Word）
- [ ] 添加简历模板库
- [ ] 实现智能岗位推荐
- [ ] 增加面试模拟功能
- [ ] 移动端适配
- [ ] 多语言支持

## 📄 许可证

MIT License

## 🤝 贡献

欢迎提交Issue和Pull Request！

## 📧 联系方式

如有问题或建议，请联系：[HUAZHAONIANER](https://github.com/HUAZHAONIANER)

---

⭐ 如果这个项目对你有帮助，请给个Star！
