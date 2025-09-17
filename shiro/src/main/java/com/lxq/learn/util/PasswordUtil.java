package com.lxq.learn.util;

import org.apache.shiro.crypto.hash.SimpleHash;
import org.apache.shiro.lang.util.ByteSource;

/**
 * 密码工具类
 * 用于生成和验证Shiro MD5加密密码
 */
public class PasswordUtil {

    private static final String HASH_ALGORITHM = "MD5";
    private static final int HASH_ITERATIONS = 1024;

    /**
     * 生成加密密码
     *
     * @param password 原始密码
     * @param salt     盐值（通常使用用户名）
     * @return 加密后的十六进制密码字符串
     */
    public static String hashPassword(String password, String salt) {
        return new SimpleHash(
                HASH_ALGORITHM,
                password,
                ByteSource.Util.bytes(salt),
                HASH_ITERATIONS
        ).toHex();
    }

    /**
     * 测试方法：生成测试用户的密码
     */
    public static void main(String[] args) {
        System.out.println("=== Shiro密码生成工具 ===");
        System.out.println("算法: " + HASH_ALGORITHM);
        System.out.println("迭代次数: " + HASH_ITERATIONS);
        System.out.println();

        // 生成测试用户密码
        String[] usernames = {"admin", "user", "test", "disabled_user"};
        String password = "123456";

        for (String username : usernames) {
            String hashedPassword = hashPassword(password, username);
            System.out.println("用户名: " + username);
            System.out.println("原始密码: " + password);
            System.out.println("盐值: " + username);
            System.out.println("加密密码: " + hashedPassword);
            System.out.println("SQL: INSERT INTO users (username, password, enabled) VALUES ('" +
                               username + "', '" + hashedPassword + "', 1);");
            System.out.println();
        }
    }

    /**
     * 验证密码是否正确
     *
     * @param inputPassword    用户输入的密码
     * @param salt            盐值
     * @param storedPassword  数据库中存储的加密密码
     * @return 密码是否正确
     */
    public static boolean verifyPassword(String inputPassword, String salt, String storedPassword) {
        String hashedInput = hashPassword(inputPassword, salt);
        return hashedInput.equals(storedPassword);
    }
}