package org.example.resumeai.controller;

import org.example.resumeai.common.Result;
import org.example.resumeai.dao.ResumeDao;
import org.example.resumeai.entity.Resume;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@RestController
@RequestMapping("/resume")
@CrossOrigin
public class ResumeController {

    @Autowired
    private ResumeDao resumeDao;

    /**
     * 获取所有简历
     */
    @GetMapping("/getResume")
    public Result getAllResumes() {
        List<Resume> resumes = resumeDao.findAll();
        return Result.success(resumes);
    }

    /**
     * 根据用户ID获取简历列表
     */
    @GetMapping("/getByUserId")
    public Result getByUserId(@RequestParam Integer userId) {
        List<Resume> resumes = resumeDao.findByUserId(userId);
        return Result.success(resumes);
    }

    /**
     * 根据ID获取简历详情
     */
    @GetMapping("/getById")
    public Result getById(@RequestParam Integer id) {
        Resume resume = resumeDao.findById(id);
        if (resume != null) {
            return Result.success(resume);
        }
        return Result.error("简历不存在");
    }

    /**
     * 条件查询简历
     */
    @GetMapping("/findResume")
    public Result findResume(Resume resume) {
        List<Resume> resumes = resumeDao.findResume(resume);
        return Result.success(resumes);
    }

    /**
     * 新增简历
     */
    @PostMapping("/addResume")
    public Result addResume(@RequestBody Resume resume) {
        int result = resumeDao.insertResume(resume);
        if (result > 0) {
            return Result.success(null, "简历添加成功");
        }
        return Result.error("添加失败");
    }

    /**
     * 更新简历
     */
    @PostMapping("/updateResume")
    public Result updateResume(@RequestBody Resume resume) {
        int result = resumeDao.updateResume(resume);
        if (result > 0) {
            return Result.success(null, "简历更新成功");
        }
        return Result.error("更新失败");
    }

    /**
     * 删除简历
     */
    @GetMapping("/deleteResume")
    public Result deleteResume(@RequestParam Integer id) {
        int result = resumeDao.deleteResume(id);
        if (result > 0) {
            return Result.success(null, "删除成功");
        }
        return Result.error("删除失败");
    }

    /**
     * 上传简历文件
     * 支持拖拽上传，读取文件内容并保存
     */
    @PostMapping("/uploadFile")
    public Result uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return Result.error("文件为空");
            }

            String originalName = file.getOriginalFilename();
            // 使用绝对路径，避免相对路径在不同启动方式下找不到
            String userDir = System.getProperty("user.dir");
            Path uploadDirPath = Paths.get(userDir, "uploads", "resumes");
            File dir = uploadDirPath.toFile();
            if (!dir.exists()) {
                boolean created = dir.mkdirs();
                if (!created) {
                    return Result.error("上传目录创建失败，请检查磁盘权限: " + uploadDirPath);
                }
            }

            // 生成唯一文件名
            String savedName = UUID.randomUUID().toString() + "_" + (originalName != null ? originalName : "resume.txt");
            Path filePath = uploadDirPath.resolve(savedName);
            file.transferTo(filePath.toFile());

            // 读取文本内容
            String content = "";
            String ext = originalName != null ? originalName.toLowerCase() : "";
            if (ext.endsWith(".txt")) {
                content = Files.readString(filePath);
            }

            Map<String, Object> result = new HashMap<>();
            result.put("filePath", filePath.toString());
            result.put("fileName", originalName);
            result.put("content", content);
            result.put("msg", ext.endsWith(".txt") ? "文件上传并读取成功" : "文件已保存（非txt格式无法自动解析内容）");

            return Result.success(result);
        } catch (IOException e) {
            return Result.error("文件上传失败：" + e.getMessage());
        }
    }
}
