package com.lxq.learn.repository;

import com.lxq.learn.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 用户数据访问接口
 * 继承 JpaRepository 提供基础的 CRUD 操作
 * 针对 nacos.users 表的数据访问层
 */
@Repository
public interface UserRepository extends JpaRepository<User, String> {

    /**
     * 根据用户名查找用户
     * 用于登录认证时查询用户信息
     *
     * @param username 用户名
     * @return 用户信息（可能为空）
     */
    Optional<User> findByUsername(String username);

    /**
     * 根据用户名查找启用状态的用户
     * 用于确保只有启用的账户才能登录
     *
     * @param username 用户名
     * @return 启用状态的用户信息（可能为空）
     */
    @Query("SELECT u FROM User u WHERE u.username = :username AND u.enabled = true")
    Optional<User> findByUsernameAndEnabled(@Param("username") String username);

    /**
     * 检查用户名是否存在
     * 用于注册时验证用户名唯一性
     *
     * @param username 用户名
     * @return 是否存在
     */
    boolean existsByUsername(String username);

    /**
     * 统计启用状态的用户数量
     * 用于系统监控
     *
     * @return 启用用户数量
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.enabled = true")
    long countEnabledUsers();

    /**
     * 根据启用状态查找用户列表
     * 用于管理员查看用户状态
     *
     * @param enabled 启用状态
     * @return 用户列表
     */
    @Query("SELECT u FROM User u WHERE u.enabled = :enabled ORDER BY u.username")
    java.util.List<User> findByEnabledOrderByUsername(@Param("enabled") Boolean enabled);
}