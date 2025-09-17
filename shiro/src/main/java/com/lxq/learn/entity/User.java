package com.lxq.learn.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 用户实体类
 * 对应数据库 nacos.users 表
 * 表结构：username, password, enabled
 */
@Entity
@Table(name = "users")
public class User {

    /**
     * 用户名 - 主键
     * 不允许为空，长度限制在2-50字符
     */
    @Id
    @Column(name = "username", nullable = false, length = 50)
    @NotBlank(message = "用户名不能为空")
    @Size(min = 2, max = 50, message = "用户名长度必须在2-50个字符之间")
    private String username;

    /**
     * 用户密码
     * 不允许为空，存储加密后的密码
     */
    @Column(name = "password", nullable = false, length = 255)
    @NotBlank(message = "密码不能为空")
    private String password;

    /**
     * 账户是否启用
     * true: 启用, false: 禁用
     * 默认为true
     * 数据库存储为TINYINT(1): 1=启用, 0=禁用
     */
    @Column(name = "enabled", nullable = false, columnDefinition = "TINYINT(1)")
    private Boolean enabled = true;

    /**
     * 无参构造函数 (JPA要求)
     */
    public User() {}

    /**
     * 带参构造函数
     *
     * @param username 用户名
     * @param password 密码
     * @param enabled  是否启用
     */
    public User(String username, String password, Boolean enabled) {
        this.username = username;
        this.password = password;
        this.enabled = enabled;
    }

    // Getter 和 Setter 方法

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

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * 账户是否可用（非锁定状态）
     * 用于Shiro认证判断
     */
    public boolean isAccountNonLocked() {
        return this.enabled != null && this.enabled;
    }

    @Override
    public String toString() {
        return "User{" +
                "username='" + username + '\'' +
                ", password='[PROTECTED]'" +
                ", enabled=" + enabled +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        User user = (User) o;
        return username != null ? username.equals(user.username) : user.username == null;
    }

    @Override
    public int hashCode() {
        return username != null ? username.hashCode() : 0;
    }
}