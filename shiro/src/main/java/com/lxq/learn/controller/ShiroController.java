package com.lxq.learn.controller;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.*;
import org.apache.shiro.subject.Subject;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Shiro 认证控制器
 * 提供登录、登出和测试接口
 */
@RestController
@RequestMapping("/shiro")
public class ShiroController {

    /**
     * 用户登录接口 - GET 方式（用于测试，支持查询参数）
     * GET /shiro/login?username=xxx&password=xxx
     *
     * @param loginRequest 包含用户名和密码的登录请求
     * @return 登录结果
     */
    @GetMapping("/login")
    public Map<String, Object> loginByGet(LoginRequest loginRequest) {
        return performLogin(loginRequest);
    }

    /**
     * 用户登录接口 - POST 方式（推荐，支持 JSON）
     * POST /shiro/login
     *
     * @param loginRequest 包含用户名和密码的登录请求
     * @return 登录结果
     */
    @PostMapping("/login")
    public Map<String, Object> loginByPost(@RequestBody LoginRequest loginRequest) {
        return performLogin(loginRequest);
    }

    /**
     * 执行登录的核心逻辑
     *
     * @param loginRequest 登录请求
     * @return 登录结果
     */
    private Map<String, Object> performLogin(LoginRequest loginRequest) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 获取当前用户主体
            Subject subject = SecurityUtils.getSubject();

            // 如果已经登录，直接返回成功
            if (subject.isAuthenticated()) {
                result.put("success", true);
                result.put("message", "用户已登录");
                result.put("username", subject.getPrincipal());
                return result;
            }

            // 创建用户名密码令牌
            UsernamePasswordToken token = new UsernamePasswordToken(
                    loginRequest.getUsername(),
                    loginRequest.getPassword()
            );

            // 执行登录
            subject.login(token);

            // 登录成功
            result.put("success", true);
            result.put("message", "登录成功");
            result.put("username", subject.getPrincipal());

        } catch (UnknownAccountException e) {
            result.put("success", false);
            result.put("message", "用户名不存在");
        } catch (IncorrectCredentialsException e) {
            result.put("success", false);
            result.put("message", "密码错误");
        } catch (LockedAccountException e) {
            result.put("success", false);
            result.put("message", "账户被锁定");
        } catch (AuthenticationException e) {
            result.put("success", false);
            result.put("message", "认证失败：" + e.getMessage());
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "登录过程中发生错误：" + e.getMessage());
        }

        return result;
    }

    /**
     * 用户登出接口
     * POST /shiro/logout
     *
     * @return 登出结果
     */
    @PostMapping("/logout")
    public Map<String, Object> logout() {
        Map<String, Object> result = new HashMap<>();

        try {
            Subject subject = SecurityUtils.getSubject();
            if (subject.isAuthenticated()) {
                subject.logout();
                result.put("success", true);
                result.put("message", "登出成功");
            } else {
                result.put("success", true);
                result.put("message", "用户未登录");
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "登出过程中发生错误：" + e.getMessage());
        }

        return result;
    }

    /**
     * 公共接口（不需要登录）
     * GET /shiro/public
     *
     * @return 公共信息
     */
    @GetMapping("/public")
    public Map<String, Object> publicEndpoint() {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "这是一个公共接口，无需登录即可访问");
        result.put("timestamp", System.currentTimeMillis());
        return result;
    }

    /**
     * 受保护的接口（需要登录）
     * GET /shiro/protected
     *
     * @return 受保护的信息
     */
    @GetMapping("/protected")
    public Map<String, Object> protectedEndpoint() {
        Map<String, Object> result = new HashMap<>();
        Subject subject = SecurityUtils.getSubject();

        result.put("success", true);
        result.put("message", "这是一个受保护的接口，需要登录才能访问");
        result.put("username", subject.getPrincipal());
        result.put("authenticated", subject.isAuthenticated());
        result.put("timestamp", System.currentTimeMillis());

        return result;
    }

    /**
     * 需要管理员权限的接口
     * GET /shiro/admin
     *
     * @return 管理员信息
     */
    @GetMapping("/admin")
    public Map<String, Object> adminEndpoint() {
        Map<String, Object> result = new HashMap<>();
        Subject subject = SecurityUtils.getSubject();

        // 检查用户是否有管理员角色
        if (subject.hasRole("admin")) {
            result.put("success", true);
            result.put("message", "欢迎，管理员！");
            result.put("username", subject.getPrincipal());
        } else {
            result.put("success", false);
            result.put("message", "权限不足，需要管理员权限");
        }

        return result;
    }

    /**
     * 获取当前用户信息
     * GET /shiro/user-info
     *
     * @return 用户信息
     */
    @GetMapping("/user-info")
    public Map<String, Object> getUserInfo() {
        Map<String, Object> result = new HashMap<>();
        Subject subject = SecurityUtils.getSubject();

        if (subject.isAuthenticated()) {
            result.put("success", true);
            result.put("username", subject.getPrincipal());
            result.put("authenticated", true);
            result.put("hasAdminRole", subject.hasRole("admin"));
            result.put("hasUserRole", subject.hasRole("user"));
        } else {
            result.put("success", false);
            result.put("message", "用户未登录");
            result.put("authenticated", false);
        }

        return result;
    }

    /**
     * 登录请求的数据传输对象
     */
    public static class LoginRequest {
        private String username;
        private String password;

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }
}
