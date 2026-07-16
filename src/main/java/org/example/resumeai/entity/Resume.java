package org.example.resumeai.entity;

/**
 * 简历实体类
 */
public class Resume {
    private Integer id;
    private Integer userId;
    private String name;           // 简历名称
    private String education;      // 教育背景
    private String school;         // 毕业院校
    private String major;          // 专业
    private String skills;         // 技能列表
    private String experience;     // 实习/项目经历
    private String content;        // 简历全文
    private String filePath;       // 上传文件路径
    private String optimizedContent; // AI优化后的简历内容
    private String createTime;

    public Resume() {}

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEducation() { return education; }
    public void setEducation(String education) { this.education = education; }
    public String getSchool() { return school; }
    public void setSchool(String school) { this.school = school; }
    public String getMajor() { return major; }
    public void setMajor(String major) { this.major = major; }
    public String getSkills() { return skills; }
    public void setSkills(String skills) { this.skills = skills; }
    public String getExperience() { return experience; }
    public void setExperience(String experience) { this.experience = experience; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }
    public String getOptimizedContent() { return optimizedContent; }
    public void setOptimizedContent(String optimizedContent) { this.optimizedContent = optimizedContent; }
    public String getCreateTime() { return createTime; }
    public void setCreateTime(String createTime) { this.createTime = createTime; }

    @Override
    public String toString() {
        return "Resume{" +
                "id=" + id +
                ", userId=" + userId +
                ", name='" + name + '\'' +
                ", education='" + education + '\'' +
                ", school='" + school + '\'' +
                ", major='" + major + '\'' +
                ", skills='" + skills + '\'' +
                ", experience='" + experience + '\'' +
                ", createTime='" + createTime + '\'' +
                '}';
    }
}
