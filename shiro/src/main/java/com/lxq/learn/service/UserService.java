package com.lxq.learn.service;

import com.lxq.learn.entity.User;
import com.lxq.learn.repository.UserRepository;
import org.apache.shiro.crypto.hash.SimpleHash;
import org.apache.shiro.lang.util.ByteSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 用户服务层
 * 提供用户相关的业务逻辑处理
 * 包括用户认证、权限管理、用户信息维护等功能
 */
@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;

    /**
     * 密码加密算法名称
     */
    private static final String HASH_ALGORITHM = "MD5";

    /**
     * 密码加密迭代次数
     */
    private static final int HASH_ITERATIONS = 1024;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * 根据用户名查找用户
     * 用于 Shiro 认证过程
     *
     * @param username 用户名
     * @return 用户信息（可能为空）
     */
    @Transactional(readOnly = true)
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    /**
     * 根据用户名查找启用状态的用户
     * 确保只有启用的账户才能登录
     *
     * @param username 用户名
     * @return 启用状态的用户信息（可能为空）
     */
    @Transactional(readOnly = true)
    public Optional<User> findEnabledUserByUsername(String username) {
        return userRepository.findByUsernameAndEnabled(username);
    }

    /**
     * 验证用户密码
     * 用于 Shiro Realm 中的密码验证
     *
     * @param user          用户信息
     * @param inputPassword 输入的密码
     * @return 密码是否正确
     */
    public boolean verifyPassword(User user, String inputPassword) {
        if (user == null || inputPassword == null) {
            return false;
        }

        // 使用用户名作为盐值进行密码加密比较
        String salt = user.getUsername();
        String hashedInput = hashPassword(inputPassword, salt);

        // 比较加密后的密码
        return hashedInput.equals(user.getPassword());
    }

    /**
     * 创建新用户
     * 自动对密码进行加密处理
     *
     * @param username    用户名
     * @param rawPassword 原始密码
     * @param enabled     是否启用
     * @return 创建的用户
     */
    public User createUser(String username, String rawPassword, Boolean enabled) {
        // 检查用户名是否已存在
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("用户名 '" + username + "' 已存在");
        }

        // 加密密码
        String hashedPassword = hashPassword(rawPassword, username);

        // 创建用户
        User user = new User(username, hashedPassword, enabled);
        return userRepository.save(user);
    }

    /**
     * 更新用户密码
     *
     * @param username    用户名
     * @param newPassword 新密码
     * @return 是否更新成功
     */
    public boolean updatePassword(String username, String newPassword) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            String hashedPassword = hashPassword(newPassword, username);
            user.setPassword(hashedPassword);
            userRepository.save(user);
            return true;
        }
        return false;
    }

    /**
     * 启用或禁用用户账户
     *
     * @param username 用户名
     * @param enabled  启用状态
     * @return 是否操作成功
     */
    public boolean updateUserStatus(String username, Boolean enabled) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setEnabled(enabled);
            userRepository.save(user);
            return true;
        }
        return false;
    }

    /**
     * 获取所有启用的用户
     *
     * @return 启用用户列表
     */
    @Transactional(readOnly = true)
    public List<User> findAllEnabledUsers() {
        return userRepository.findByEnabledOrderByUsername(true);
    }

    /**
     * 获取用户总数统计
     *
     * @return 启用用户数量
     */
    @Transactional(readOnly = true)
    public long getEnabledUserCount() {
        return userRepository.countEnabledUsers();
    }

    /**
     * 密码加密工具方法
     * 使用 Shiro 提供的加密算法
     *
     * @param password 原始密码
     * @param salt     盐值
     * @return 加密后的密码
     */
    private String hashPassword(String password, String salt) {
        return new SimpleHash(
                HASH_ALGORITHM,
                password,
                ByteSource.Util.bytes(salt),
                HASH_ITERATIONS
        ).toHex();
    }

    /**
     * 获取用户权限信息
     * 根据用户名返回该用户的角色和权限
     * 这里采用简单的硬编码方式，实际项目中应该从数据库获取
     *
     * @param username 用户名
     * @return 用户角色数组
     */
    public String[] getUserRoles(String username) {
        // 在实际项目中，这里应该从数据库的角色表中查询用户角色
        // 为了演示，我们使用简单的硬编码逻辑
        if ("admin".equals(username)) {
            return new String[]{"admin", "user"};
        } else {
            return new String[]{"user"};
        }
    }

    /**
     * 获取用户权限信息
     *
     * @param username 用户名
     * @return 用户权限数组
     */
    public String[] getUserPermissions(String username) {
        // 在实际项目中，这里应该从数据库的权限表中查询用户权限
        // 为了演示，我们使用简单的硬编码逻辑
        if ("admin".equals(username)) {
            return new String[]{"user:read", "user:write", "user:delete"};
        } else {
            return new String[]{"user:read"};
        }
    }

    /**
     * 用户注册方法
     * 专门用于用户注册，包含完整的验证逻辑
     *
     * @param username 用户名
     * @param password 原始密码
     * @return 注册结果信息
     */
    public RegisterResult registerUser(String username, String password) {
        try {
            // 1. 验证用户名格式
            if (username == null || username.trim().isEmpty()) {
                return new RegisterResult(false, "用户名不能为空");
            }

            if (username.length() < 2 || username.length() > 50) {
                return new RegisterResult(false, "用户名长度必须在2-50个字符之间");
            }

            // 2. 验证密码格式
            if (password == null || password.trim().isEmpty()) {
                return new RegisterResult(false, "密码不能为空");
            }

            if (password.length() < 6) {
                return new RegisterResult(false, "密码长度至少6个字符");
            }

            // 3. 检查用户名是否已存在
            if (userRepository.existsByUsername(username.trim())) {
                return new RegisterResult(false, "用户名已存在，请选择其他用户名");
            }

            // 4. 创建用户（自动加密密码）
            User user = createUser(username.trim(), password, true);

            return new RegisterResult(true, "注册成功", user.getUsername());

        } catch (Exception e) {
            System.err.println("用户注册失败：" + e.getMessage());
            return new RegisterResult(false, "注册过程中发生错误，请稍后重试");
        }
    }

    /**
     * 注册结果封装类
     */
    public static class RegisterResult {
        private final boolean success;
        private final String message;
        private final String username;

        public RegisterResult(boolean success, String message) {
            this.success = success;
            this.message = message;
            this.username = null;
        }

        public RegisterResult(boolean success, String message, String username) {
            this.success = success;
            this.message = message;
            this.username = username;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }

        public String getUsername() {
            return username;
        }
    }
}