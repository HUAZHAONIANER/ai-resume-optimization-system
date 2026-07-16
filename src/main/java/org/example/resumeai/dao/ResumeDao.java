package org.example.resumeai.dao;

import org.example.resumeai.entity.Resume;
import java.util.List;

/**
 * 简历 DAO 接口
 */
public interface ResumeDao {
    List<Resume> findAll();
    List<Resume> findByUserId(Integer userId);
    List<Resume> findResume(Resume resume);
    Resume findById(Integer id);
    int insertResume(Resume resume);
    int updateResume(Resume resume);
    int deleteResume(Integer id);
}
