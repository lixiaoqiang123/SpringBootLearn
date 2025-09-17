package com.lxq.learn.config;

import com.lxq.learn.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 数据库初始化配置
 * 在应用启动时自动创建测试用户数据
 */
@Configuration
public class DatabaseInitializer {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseInitializer.class);

    private final UserService userService;

    @Value("${app.database.init-test-data:true}")
    private boolean initTestData;

    @Autowired
    public DatabaseInitializer(UserService userService) {
        this.userService = userService;
    }

    /**
     * 应用启动时初始化数据库数据
     */
    @Bean
    public ApplicationRunner initDatabase() {
        return args -> {
            if (!initTestData) {
                logger.info("跳过测试数据初始化（配置已禁用）");
                return;
            }

            logger.info("开始初始化测试用户数据...");

            try {
                // 创建管理员账户
                createUserIfNotExists("admin", "123456", true);

                // 创建普通用户账户
                createUserIfNotExists("user", "123456", true);

                // 创建测试账户
                createUserIfNotExists("test", "123456", true);

                // 创建禁用账户示例
                createUserIfNotExists("disabled_user", "123456", false);

                // 显示统计信息
                long enabledUsers = userService.getEnabledUserCount();
                logger.info("数据初始化完成，当前启用用户数量: {}", enabledUsers);

                // 显示测试账户信息
                logger.info("=== 测试账户信息 ===");
                logger.info("管理员账户: admin / 123456 (启用)");
                logger.info("普通用户: user / 123456 (启用)");
                logger.info("测试账户: test / 123456 (启用)");
                logger.info("禁用账户: disabled_user / 123456 (禁用)");
                logger.info("==================");

            } catch (Exception e) {
                logger.error("初始化测试数据时发生错误", e);
            }
        };
    }

    /**
     * 创建用户（如果不存在）
     *
     * @param username 用户名
     * @param password 密码
     * @param enabled  是否启用
     */
    private void createUserIfNotExists(String username, String password, boolean enabled) {
        try {
            // 检查用户是否已存在
            if (userService.findByUsername(username).isPresent()) {
                logger.info("用户 [{}] 已存在，跳过创建", username);
                return;
            }

            // 创建新用户
            userService.createUser(username, password, enabled);
            String status = enabled ? "启用" : "禁用";
            logger.info("成功创建用户 [{}]，状态: {}", username, status);

        } catch (Exception e) {
            logger.error("创建用户 [{}] 时发生错误: {}", username, e.getMessage());
        }
    }
}