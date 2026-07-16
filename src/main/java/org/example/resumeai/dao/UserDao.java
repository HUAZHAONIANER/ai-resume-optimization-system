package org.example.resumeai.dao;

import org.example.resumeai.entity.User;
import java.util.List;

/**
 * 用户 DAO 接口
 */
public interface UserDao {
    List<User> findAll();
    List<User> findUser(User user);
    User findById(Integer id);
    User findByUsernameAndPassword(User user);
    int insertUser(User user);
    int update(User user);
    int delete(Integer id);
}
