package org.example.resumeai.dao;

import org.example.resumeai.entity.MatchResult;
import java.util.List;

/**
 * 匹配结果 DAO 接口
 */
public interface MatchResultDao {
    List<MatchResult> findAll();
    List<MatchResult> findByResumeId(Integer resumeId);
    List<MatchResult> findByJobId(Integer jobId);
    MatchResult findById(Integer id);
    int insertMatchResult(MatchResult matchResult);
    int deleteMatchResult(Integer id);
}
