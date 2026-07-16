package org.example.resumeai.entity;

/**
 * 匹配结果实体类
 */
public class MatchResult {
    private Integer id;
    private Integer resumeId;
    private Integer jobId;
    private String matchScore;     // 匹配分数
    private String analysis;       // AI分析结果
    private String suggestion;     // AI改进建议
    private String createTime;

    public MatchResult() {}

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getResumeId() { return resumeId; }
    public void setResumeId(Integer resumeId) { this.resumeId = resumeId; }
    public Integer getJobId() { return jobId; }
    public void setJobId(Integer jobId) { this.jobId = jobId; }
    public String getMatchScore() { return matchScore; }
    public void setMatchScore(String matchScore) { this.matchScore = matchScore; }
    public String getAnalysis() { return analysis; }
    public void setAnalysis(String analysis) { this.analysis = analysis; }
    public String getSuggestion() { return suggestion; }
    public void setSuggestion(String suggestion) { this.suggestion = suggestion; }
    public String getCreateTime() { return createTime; }
    public void setCreateTime(String createTime) { this.createTime = createTime; }

    @Override
    public String toString() {
        return "MatchResult{" +
                "id=" + id +
                ", resumeId=" + resumeId +
                ", jobId=" + jobId +
                ", matchScore='" + matchScore + '\'' +
                ", createTime='" + createTime + '\'' +
                '}';
    }
}
