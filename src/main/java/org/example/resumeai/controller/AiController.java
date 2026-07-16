package org.example.resumeai.controller;

import org.example.resumeai.common.Result;
import org.example.resumeai.dao.JobDao;
import org.example.resumeai.dao.MatchResultDao;
import org.example.resumeai.dao.ResumeDao;
import org.example.resumeai.entity.Job;
import org.example.resumeai.entity.MatchResult;
import org.example.resumeai.entity.Resume;
import org.example.resumeai.service.ResumeAssistant;
import org.example.resumeai.service.MatchAssistant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/ai")
@CrossOrigin
public class AiController {

    @Autowired
    private ResumeAssistant resumeAssistant;

    @Autowired
    private MatchAssistant matchAssistant;

    @Autowired
    private ResumeDao resumeDao;

    @Autowired
    private JobDao jobDao;

    @Autowired
    private MatchResultDao matchResultDao;

    /**
     * AI简历优化
     * 接收简历内容，返回AI优化后的简历
     */
    @PostMapping("/optimizeResume")
    public Result optimizeResume(@RequestBody Map<String, Object> request) {
        try {
            Object resumeIdObj = request.get("resumeId");
            Integer resumeId = resumeIdObj != null ? ((Number) resumeIdObj).intValue() : null;
            String resumeContent = (String) request.get("content");

            if (resumeContent == null || resumeContent.isEmpty()) {
                return Result.error("简历内容不能为空");
            }

            // 调用AI服务优化简历
            String rawOutput = resumeAssistant.optimizeResume(resumeContent);
            String optimizedContent = cleanMarkdownSymbols(rawOutput);

            // 如果提供了resumeId，将优化结果保存到数据库
            if (resumeId != null) {
                Resume resume = resumeDao.findById(resumeId);
                if (resume != null) {
                    resume.setOptimizedContent(optimizedContent);
                    resumeDao.updateResume(resume);
                }
            }

            Map<String, Object> result = new HashMap<>();
            result.put("originalContent", resumeContent);
            result.put("optimizedContent", optimizedContent);

            return Result.success(result, "简历优化完成");
        } catch (Exception e) {
            return Result.error("AI优化失败：" + e.getMessage());
        }
    }

    /**
     * AI岗位匹配
     * 接收简历ID和岗位ID，返回匹配分析结果
     */
    @PostMapping("/matchJob")
    public Result matchJob(@RequestBody Map<String, Object> request) {
        try {
            Object resumeIdObj = request.get("resumeId");
            Object jobIdObj = request.get("jobId");
            Integer resumeId = resumeIdObj != null ? ((Number) resumeIdObj).intValue() : null;
            Integer jobId = jobIdObj != null ? ((Number) jobIdObj).intValue() : null;

            if (resumeId == null || jobId == null) {
                return Result.error("简历ID和岗位ID不能为空");
            }

            Resume resume = resumeDao.findById(resumeId);
            Job job = jobDao.findById(jobId);

            if (resume == null) {
                return Result.error("简历不存在");
            }
            if (job == null) {
                return Result.error("岗位不存在");
            }

            String resumeContent = resume.getContent() != null ? resume.getContent() :
                    "教育背景：" + (resume.getEducation() != null ? resume.getEducation() : "") + "\n" +
                    "学校：" + (resume.getSchool() != null ? resume.getSchool() : "") + "\n" +
                    "专业：" + (resume.getMajor() != null ? resume.getMajor() : "") + "\n" +
                    "技能：" + (resume.getSkills() != null ? resume.getSkills() : "") + "\n" +
                    "经历：" + (resume.getExperience() != null ? resume.getExperience() : "");

            String jobDesc = "岗位名称：" + job.getTitle() + "\n" +
                    "公司：" + job.getCompany() + "\n" +
                    "岗位描述：" + (job.getDescription() != null ? job.getDescription() : "") + "\n" +
                    "任职要求：" + (job.getRequirements() != null ? job.getRequirements() : "");

            // 组合简历和岗位信息，调用AI服务进行匹配分析
            String combinedInput = "【候选人简历】\n" + resumeContent + "\n\n【目标岗位】\n" + jobDesc;
            String matchAnalysis = matchAssistant.matchResumeToJob(combinedInput);

            // 保存匹配结果
            MatchResult matchResult = new MatchResult();
            matchResult.setResumeId(resumeId);
            matchResult.setJobId(jobId);
            matchResult.setAnalysis(matchAnalysis);
            matchResult.setSuggestion(""); // AI在analysis中统一返回
            matchResultDao.insertMatchResult(matchResult);

            Map<String, Object> result = new HashMap<>();
            result.put("resumeId", resumeId);
            result.put("jobId", jobId);
            result.put("resumeName", resume.getName());
            result.put("jobTitle", job.getTitle());
            result.put("company", job.getCompany());
            result.put("analysis", matchAnalysis);

            return Result.success(result, "匹配分析完成");
        } catch (Exception e) {
            return Result.error("AI匹配失败：" + e.getMessage());
        }
    }

    /**
     * 获取匹配历史记录
     */
    @GetMapping("/getMatchHistory")
    public Result getMatchHistory(@RequestParam(required = false) Integer resumeId,
                                   @RequestParam(required = false) Integer jobId) {
        if (resumeId != null) {
            return Result.success(matchResultDao.findByResumeId(resumeId));
        }
        if (jobId != null) {
            return Result.success(matchResultDao.findByJobId(jobId));
        }
        return Result.success(matchResultDao.findAll());
    }

    /**
     * 删除匹配记录
     */
    @GetMapping("/deleteMatchResult")
    public Result deleteMatchResult(@RequestParam Integer id) {
        int result = matchResultDao.deleteMatchResult(id);
        if (result > 0) {
            return Result.success(null, "删除成功");
        }
        return Result.error("删除失败");
    }

    /**
     * 清理AI输出中的markdown符号和多余格式
     */
    private String cleanMarkdownSymbols(String text) {
        if (text == null || text.isEmpty()) return text;

        // 去除markdown代码块标记
        text = text.replaceAll("```[a-zA-Z]*\\s*", "");
        text = text.replaceAll("```", "");

        // 去除粗体/斜体标记 **text**  *text*  __text__  _text_
        text = text.replaceAll("\\*\\*(.+?)\\*\\*", "$1");
        text = text.replaceAll("__(.+?)__", "$1");
        text = text.replaceAll("\\*(.+?)\\*", "$1");
        text = text.replaceAll("_(.+?)_", "$1");

        // 去除删除线 ~~text~~
        text = text.replaceAll("~~(.+?)~~", "$1");

        // 去除行内代码标记 `code`
        text = text.replaceAll("`(.+?)`", "$1");

        // 去除markdown标题标记（保留标题文字）
        text = text.replaceAll("(?m)^#{1,6}\\s+", "");

        // 去除水平分隔线
        text = text.replaceAll("(?m)^[-*_]{3,}\\s*$", "");

        // 去除无序列表标记
        text = text.replaceAll("(?m)^\\s*[-*+]\\s+", "• ");

        // 合并多余空行：>2个连续换行 → 保留2个
        text = text.replaceAll("\\n{4,}", "\n\n\n");
        text = text.replaceAll("(\\r?\\n){4,}", "\n\n\n");

        // 清理每行首尾多余空格
        text = text.replaceAll("(?m)^[ \t]+", "");
        text = text.replaceAll("(?m)[ \t]+$", "");

        // 去除首尾空白
        text = text.trim();

        return text;
    }
}
