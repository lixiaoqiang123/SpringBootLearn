package com.lxq.learn.config;

import com.lxq.learn.entity.User;
import com.lxq.learn.service.UserService;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.lang.util.ByteSource;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * 自定义用户认证和授权的 Realm
 * 从 MySQL 数据库 nacos.users 表获取用户信息进行认证和授权
 * Realm 是 Shiro 中用于获取用户认证信息和权限信息的核心组件
 */
public class UserRealm extends AuthorizingRealm {

    @Autowired
    private UserService userService;

    /**
     * 授权方法：获取用户的权限信息
     * 当用户访问需要权限的资源时，Shiro 会调用此方法获取用户权限
     * 现在从数据库获取用户权限信息
     *
     * @param principals 用户身份信息
     * @return 用户的权限信息
     */
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        // 获取用户名（在认证成功时设置的 principal）
        String username = (String) principals.getPrimaryPrincipal();

        System.out.println("正在为用户 [" + username + "] 获取权限信息...");

        // 从数据库验证用户是否存在且启用
        Optional<User> userOpt = userService.findEnabledUserByUsername(username);
        if (userOpt.isEmpty()) {
            System.out.println("用户 [" + username + "] 不存在或已被禁用，无法获取权限");
            return null;
        }

        SimpleAuthorizationInfo authorizationInfo = new SimpleAuthorizationInfo();

        // 从 UserService 获取用户角色
        String[] userRoles = userService.getUserRoles(username);
        Set<String> roles = new HashSet<>(Arrays.asList(userRoles));
        authorizationInfo.setRoles(roles);
        System.out.println("用户 [" + username + "] 的角色: " + roles);

        // 从 UserService 获取用户权限
        String[] userPermissions = userService.getUserPermissions(username);
        Set<String> permissions = new HashSet<>(Arrays.asList(userPermissions));
        authorizationInfo.setStringPermissions(permissions);
        System.out.println("用户 [" + username + "] 的权限: " + permissions);

        return authorizationInfo;
    }

    /**
     * 认证方法：验证用户身份
     * 当用户登录时，Shiro 会调用此方法验证用户名和密码
     * 现在从 MySQL 数据库获取用户信息进行认证
     *
     * @param token 用户登录时提交的认证信息
     * @return 认证信息，如果认证失败则抛出异常
     * @throws AuthenticationException 认证异常
     */
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
        // 获取用户输入的用户名和密码
        UsernamePasswordToken usernamePasswordToken = (UsernamePasswordToken) token;
        String username = usernamePasswordToken.getUsername();
        char[] password = usernamePasswordToken.getPassword();

        System.out.println("开始认证用户: " + username);

        if (username == null || username.trim().isEmpty()) {
            System.out.println("用户名为空，认证失败");
            throw new UnknownAccountException("用户名不能为空");
        }

        // 从数据库查询启用状态的用户信息
        Optional<User> userOpt = userService.findEnabledUserByUsername(username);

        if (userOpt.isEmpty()) {
            System.out.println("用户 [" + username + "] 不存在或已被禁用");
            throw new UnknownAccountException("用户名或密码错误");
        }

        User user = userOpt.get();
        System.out.println("从数据库查询到用户: " + user.getUsername() + ", 启用状态: " + user.getEnabled());

        // 检查账户是否被锁定
        if (!user.isAccountNonLocked()) {
            System.out.println("用户 [" + username + "] 账户已被锁定");
            throw new LockedAccountException("账户已被锁定");
        }

        // 注意：这里不进行密码验证，让Shiro的CredentialsMatcher来处理密码验证
        // Shiro会自动使用配置的CredentialsMatcher来比较用户输入的密码和数据库中的密码

        System.out.println("用户 [" + username + "] 认证成功");

        // 创建认证信息
        // 参数说明：
        // 1. principal：用户身份信息，通常是用户名或用户ID
        // 2. credentials：用户凭证，这里使用数据库中的加密密码
        // 3. credentialsSalt：密码盐值，使用用户名作为盐值
        // 4. realmName：Realm名称
        SimpleAuthenticationInfo authenticationInfo = new SimpleAuthenticationInfo(
                username,                               // 用户身份
                user.getPassword(),                     // 数据库中的加密密码
                ByteSource.Util.bytes(username),       // 使用用户名作为盐值
                getName()                               // Realm名称
        );

        return authenticationInfo;
    }

    /**
     * 清除指定用户的授权缓存
     * 当用户权限发生变化时调用，强制重新获取权限信息
     *
     * @param username 用户名
     */
    public void clearCachedAuthorizationInfo(String username) {
        // 如果启用了缓存，清除指定用户的授权缓存
        if (getAuthorizationCache() != null) {
            getAuthorizationCache().remove(username);
            System.out.println("已清除用户 [" + username + "] 的权限缓存");
        }
    }

    /**
     * 清除所有缓存
     * 系统维护时可以调用此方法
     */
    public void clearAllCache() {
//        clearCache();
        System.out.println("已清除所有认证和授权缓存");
    }
}