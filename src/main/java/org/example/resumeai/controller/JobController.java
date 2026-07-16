package org.example.resumeai.controller;

import org.example.resumeai.common.Result;
import org.example.resumeai.dao.JobDao;
import org.example.resumeai.entity.Job;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/job")
@CrossOrigin
public class JobController {

    @Autowired
    private JobDao jobDao;

    /**
     * 获取所有岗位
     */
    @GetMapping("/getJob")
    public Result getAllJobs() {
        List<Job> jobs = jobDao.findAll();
        return Result.success(jobs);
    }

    /**
     * 根据ID获取岗位详情
     */
    @GetMapping("/getById")
    public Result getById(@RequestParam Integer id) {
        Job job = jobDao.findById(id);
        if (job != null) {
            return Result.success(job);
        }
        return Result.error("岗位不存在");
    }

    /**
     * 条件查询岗位
     */
    @GetMapping("/findJob")
    public Result findJob(Job job) {
        List<Job> jobs = jobDao.findJob(job);
        return Result.success(jobs);
    }

    /**
     * 新增岗位
     */
    @PostMapping("/addJob")
    public Result addJob(@RequestBody Job job) {
        int result = jobDao.insertJob(job);
        if (result > 0) {
            return Result.success(null, "岗位添加成功");
        }
        return Result.error("添加失败");
    }

    /**
     * 更新岗位
     */
    @PostMapping("/updateJob")
    public Result updateJob(@RequestBody Job job) {
        int result = jobDao.updateJob(job);
        if (result > 0) {
            return Result.success(null, "岗位更新成功");
        }
        return Result.error("更新失败");
    }

    /**
     * 删除岗位
     */
    @GetMapping("/deleteJob")
    public Result deleteJob(@RequestParam Integer id) {
        int result = jobDao.deleteJob(id);
        if (result > 0) {
            return Result.success(null, "删除成功");
        }
        return Result.error("删除失败");
    }
}
