package org.example.resumeai.controller;

import org.example.resumeai.common.Result;
import org.example.resumeai.dao.UserDao;
import org.example.resumeai.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user")
@CrossOrigin
public class UserController {

    @Autowired
    private UserDao userDao;

    /**
     * 获取所有用户
     */
    @GetMapping("/getUser")
    public Result getAllUsers() {
        List<User> users = userDao.findAll();
        return Result.success(users);
    }

    /**
     * 根据ID获取用户
     */
    @GetMapping("/getUserById")
    public Result getUserById(@RequestParam Integer id) {
        User user = userDao.findById(id);
        if (user != null) {
            return Result.success(user);
        }
        return Result.error("用户不存在");
    }

    /**
     * 根据条件查询用户
     */
    @GetMapping("/findUser")
    public Result findUser(User user) {
        List<User> users = userDao.findUser(user);
        return Result.success(users);
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public Result login(@RequestBody User user) {
        User loginUser = userDao.findByUsernameAndPassword(user);
        if (loginUser != null) {
            return Result.success(loginUser, "登录成功");
        }
        return Result.error("用户名或密码错误");
    }

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public Result register(@RequestBody User user) {
        // 检查用户名是否已存在
        User existUser = new User();
        existUser.setUsername(user.getUsername());
        List<User> exist = userDao.findUser(existUser);
        if (exist != null && !exist.isEmpty()) {
            return Result.error("用户名已存在");
        }
        if (user.getRole() == null || user.getRole().isEmpty()) {
            user.setRole("student");
        }
        int result = userDao.insertUser(user);
        if (result > 0) {
            return Result.success(null, "注册成功");
        }
        return Result.error("注册失败");
    }

    /**
     * 更新用户信息
     */
    @PostMapping("/update")
    public Result updateUser(@RequestBody User user) {
        int result = userDao.update(user);
        if (result > 0) {
            return Result.success(null, "更新成功");
        }
        return Result.error("更新失败");
    }

    /**
     * 删除用户
     */
    @GetMapping("/delete")
    public Result deleteUser(@RequestParam Integer id) {
        int result = userDao.delete(id);
        if (result > 0) {
            return Result.success(null, "删除成功");
        }
        return Result.error("删除失败");
    }
}
