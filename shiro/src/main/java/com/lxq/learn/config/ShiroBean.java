package com.lxq.learn.config;

import jakarta.annotation.PostConstruct;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.credential.HashedCredentialsMatcher;
import org.apache.shiro.mgt.DefaultSecurityManager;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.spring.web.config.DefaultShiroFilterChainDefinition;
import org.apache.shiro.spring.web.config.ShiroFilterChainDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Shiro 配置类
 * 负责配置 Shiro 的核心组件：SecurityManager、Realm、过滤器链等
 * 显式配置 SecurityManager 以解决 Spring Boot 3.x 兼容性问题
 */
@Configuration
public class ShiroBean {

    /**
     * 配置 SecurityManager（核心安全管理器）
     * 在 Spring Boot 3.x 中需要显式配置以确保正确初始化
     * 自动化配置都在spring-boot-starter中，因为javax迁移到jakarta生态，shiro-spring-boot-starter并不能与springboot3相兼容，
     * 只能采用单独引入依赖jakarta模块，然后再手动进行创建securitymanager用于spring容器管理
     * @param userRealm 用户认证授权 Realm
     * @return SecurityManager 实例
     */
    @Bean
    public SecurityManager securityManager(UserRealm userRealm) {
        DefaultSecurityManager securityManager = new DefaultSecurityManager();
        securityManager.setRealm(userRealm);
        SecurityUtils.setSecurityManager(securityManager);
        return securityManager;
    }

    /**
     * 配置密码匹配器
     * 用于验证用户输入的密码与数据库中存储的加密密码是否匹配
     */
    @Bean
    public HashedCredentialsMatcher hashedCredentialsMatcher() {
        HashedCredentialsMatcher matcher = new HashedCredentialsMatcher();
        // 设置加密算法名称
        matcher.setHashAlgorithmName("MD5");
        // 设置加密迭代次数
        matcher.setHashIterations(1024);
        // 设置是否存储为十六进制格式
        matcher.setStoredCredentialsHexEncoded(true);

        System.out.println("配置密码匹配器: MD5算法，1024次迭代，十六进制存储");
        return matcher;
    }

    /**
     * 配置自定义 Realm
     * Realm 负责用户认证和授权
     *
     * @return UserRealm 实例
     */
    @Bean
    public UserRealm userRealm() {
        UserRealm userRealm = new UserRealm();
        // 设置密码匹配器
        userRealm.setCredentialsMatcher(hashedCredentialsMatcher());
        return userRealm;
    }

    /**
     * 初始化 SecurityUtils
     * 确保 SecurityManager 正确绑定到 SecurityUtils
     */
    @PostConstruct
    public void initSecurityUtils() {
        // 注意：在 Spring 环境中，SecurityManager 会自动设置
        // 这里的 PostConstruct 主要是为了确认配置已正确加载
        System.out.println("Shiro SecurityManager 配置完成");
    }

    /**
     * 配置 Shiro 过滤器链
     * 定义哪些 URL 需要认证，哪些可以匿名访问
     *
     * @return ShiroFilterChainDefinition 过滤器链定义
     */
    @Bean
    public ShiroFilterChainDefinition shiroFilterChainDefinition() {
        DefaultShiroFilterChainDefinition chainDefinition = new DefaultShiroFilterChainDefinition();

        // 配置过滤器链规则
        // anon：匿名访问，不需要登录
        // authc：需要认证（登录）才能访问
        // user：记住我或已认证用户可以访问
        // perms：需要指定权限才能访问
        // roles：需要指定角色才能访问

        // 登录接口允许匿名访问
        chainDefinition.addPathDefinition("/shiro/login", "anon");
        chainDefinition.addPathDefinition("/shiro/logout", "anon");

        // 测试接口允许匿名访问（用于测试登录前的访问）
        chainDefinition.addPathDefinition("/shiro/public", "anon");

        // 所有其他 shiro 接口都需要认证
        chainDefinition.addPathDefinition("/shiro/**", "authc");

        // 对于其他路径，我们暂时允许匿名访问，避免过度拦截
        // 在实际项目中，你可以根据需要调整这个规则
        // chainDefinition.addPathDefinition("/**", "authc");

        return chainDefinition;
    }



}
