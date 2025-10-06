# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概述

这是一个SpringBoot学习项目，主要用于学习和测试SpringBoot与各种技术的集成。当前项目采用Maven多模块结构，目前包含一个Shiro安全框架的学习模块。

## 技术栈

- **Java版本**: 17
- **SpringBoot版本**: 3.5.5
- **构建工具**: Maven
- **安全框架**: Apache Shiro 2.0.5 (Jakarta EE版本)
- **JSON处理**: Jackson
- **服务器端口**: 8080

## 项目结构

```
SpringBootLearn/
├── pom.xml                          # 父级POM，定义全局依赖管理
├── shiro/                           # Shiro安全框架学习模块
│   ├── pom.xml                      # Shiro模块的POM文件
│   └── src/main/java/com/lxq/learn/
│       ├── LearnApplication.java    # SpringBoot启动类
│       ├── config/                  # 配置类目录
│       │   ├── ShiroBean.java       # Shiro核心配置(SecurityManager、Realm、过滤器链)
│       │   ├── UserRealm.java       # 自定义认证授权Realm
│       │   └── GlobalExceptionHandler.java # 全局异常处理器
│       └── controller/
│           └── ShiroController.java # Shiro相关API控制器
└── test-shiro.bat                   # Shiro模块测试启动脚本
```

## 常用开发命令

### 项目构建与运行

```bash
# 在根目录编译整个项目
mvn clean compile

# 在shiro目录运行Shiro模块
cd shiro
mvn spring-boot:run

# 使用提供的测试脚本
./test-shiro.bat
```

### 测试命令

```bash
# 运行所有测试
mvn test

# 运行特定模块测试
cd shiro && mvn test
```

## Shiro模块架构要点

### 核心配置
- **ShiroBean.java**: 显式配置SecurityManager解决Spring Boot 3.x兼容性问题
- **UserRealm.java**: 实现自定义认证和授权逻辑
- **过滤器链配置**: 定义URL访问权限规则

### API端点设计
ShiroController提供了完整的认证测试接口：
- `GET/POST /shiro/login` - 登录接口(支持查询参数和JSON)
- `POST /shiro/logout` - 登出接口
- `GET /shiro/public` - 公共接口(无需认证)
- `GET /shiro/protected` - 受保护接口(需要认证)
- `GET /shiro/admin` - 管理员接口(需要admin角色)
- `GET /shiro/user-info` - 当前用户信息

### 关键技术点
1. **Jakarta EE兼容**: 使用Shiro 2.0.5的jakarta classifier版本
2. **显式SecurityManager配置**: 通过@Bean和@PostConstruct确保正确初始化
3. **多种登录方式**: 支持GET查询参数和POST JSON两种登录方式
4. **完整的异常处理**: 区分用户名不存在、密码错误、账户锁定等场景

## 开发注意事项

### Shiro配置要点
- SecurityManager必须显式配置并绑定到SecurityUtils
- 使用jakarta classifier的依赖版本以兼容Spring Boot 3.x
- 过滤器链配置需要平衡安全性和易用性

### 测试建议
- 使用test-shiro.bat快速启动测试环境
- 测试URL: `http://localhost:8080/shiro/login?username=admin&password=123456`
- 验证各种认证场景：成功登录、失败登录、权限检查

### 扩展开发
- 新增模块应在根目录pom.xml的modules节点中声明
- 遵循现有的包结构: `com.lxq.learn`
- 配置类统一放在config包下
- 控制器类统一放在controller包下
- 以后不需要验证修复效果，告诉我完成即可