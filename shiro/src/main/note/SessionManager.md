🔍 默认Session管理的含义

在不同环境下的"默认Session管理"

1. 纯Spring Boot环境（无Shiro）
# 没有配置spring.session时的默认行为
spring:
# 没有session配置 = 使用默认的Servlet Session管理

默认行为：
- 使用Tomcat内置的Session管理
- Session存储在内存中
- 使用标准的JSESSIONID Cookie
- Session超时默认30分钟

2. Spring Boot + Shiro环境

情况A：使用DefaultSecurityManager（之前的配置）
@Bean
public SecurityManager securityManager(UserRealm userRealm) {
DefaultSecurityManager securityManager = new DefaultSecurityManager();
// 没有设置SessionManager = 使用Shiro默认的Session管理
}

Shiro的默认Session管理：
- 使用DefaultSessionManager（非Web环境）
- Session存储在内存中的MemorySessionDAO
- 不支持Cookie传递（这是关键问题！）
- Session ID通过其他方式传递（如请求参数等）

情况B：使用DefaultWebSecurityManager（现在的配置）
@Bean
public SecurityManager securityManager(UserRealm userRealm, DefaultWebSessionManager sessionManager) {
DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager();
securityManager.setSessionManager(sessionManager);
}

Shiro Web环境的Session管理：
- 使用DefaultWebSessionManager
- 支持Cookie传递（JSESSIONID）
- 可以与Servlet Session集成
- 支持URL重写等Web特性

📊 不同Session管理器对比

| 管理器类型                    | 环境  | Cookie支持 | Web集成 | 适用场景         |
  |--------------------------|-----|----------|-------|--------------|
| Servlet默认Session         | Web | ✅        | ✅     | 纯Spring Boot |
| DefaultSessionManager    | 通用  | ❌        | ❌     | 非Web应用       |
| DefaultWebSessionManager | Web | ✅        | ✅     | Web + Shiro  |

🔧 各种"默认"的技术细节

1. Servlet容器默认Session管理

  <!-- Tomcat的默认配置 -->
  <Context sessionTimeout="30">  <!-- 30分钟超时 -->
      <Manager className="org.apache.catalina.session.StandardManager" 
               maxActiveSessions="10000" />
  </Context>

2. Shiro默认Session管理（非Web）

public class DefaultSecurityManager {
// 默认使用这个Session管理器
private SessionManager sessionManager = new DefaultSessionManager();
}

public class DefaultSessionManager {
// 默认使用内存存储
private SessionDAO sessionDAO = new MemorySessionDAO();
// 默认30分钟超时
private long globalSessionTimeout = 30 * 60 * 1000L;
}

3. Shiro Web默认Session管理

public class DefaultWebSessionManager extends DefaultSessionManager {
// 启用Cookie支持
private boolean sessionIdCookieEnabled = true;
// 默认Cookie名称
private String sessionIdCookieName = "JSESSIONID";
// 禁用URL重写
private boolean sessionIdUrlRewritingEnabled = true;
}

🎯 你之前遇到问题的根源

之前的配置问题：
// 问题配置：使用了非Web的SecurityManager
DefaultSecurityManager securityManager = new DefaultSecurityManager();
// 这会导致使用DefaultSessionManager，不支持Cookie！

修复后的配置：
// 正确配置：使用Web专用的SecurityManager
DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager();
securityManager.setSessionManager(sessionManager);  // 明确指定Web Session管理器

📋 总结："默认"的层次

1. Spring Boot默认 → Servlet Container Session管理
2. Shiro默认 → DefaultSessionManager（非Web）
3. Shiro Web默认 → DefaultWebSessionManager（Web环境）
4. 你的配置 → 显式配置的DefaultWebSessionManager

关键理解：
- "默认"不是一个绝对概念，而是在特定上下文中的默认行为
- 不同框架有不同的默认Session管理策略
- 在Web环境中使用Shiro时，必须明确配置Web专用的Session管理器

这就是为什么你之前的配置会导致Session认证问题的根本原因！