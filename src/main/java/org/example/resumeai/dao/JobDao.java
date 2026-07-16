package org.example.resumeai.dao;

import org.example.resumeai.entity.Job;
import java.util.List;

/**
 * 岗位 DAO 接口
 */
public interface JobDao {
    List<Job> findAll();
    List<Job> findJob(Job job);
    Job findById(Integer id);
    int insertJob(Job job);
    int updateJob(Job job);
    int deleteJob(Integer id);
}
