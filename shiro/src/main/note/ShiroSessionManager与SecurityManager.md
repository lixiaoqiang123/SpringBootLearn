
# 🛠️ Shiro 启动报错与配置详解（Authorizer & SessionManager）

## 1. 报错原因：`authorizer` Bean 未找到

**报错信息：**

```text
Parameter 0 of method authorizationAttributeSourceAdvisor 
in org.apache.shiro.spring.boot.autoconfigure.ShiroAnnotationProcessorAutoConfiguration 
required a bean named 'authorizer' that could not be found.
```

### 📌 原因解析

这个错误表示 `ShiroAnnotationProcessorAutoConfiguration` 在创建 `AuthorizationAttributeSourceAdvisor`（负责解析如 `@RequiresPermissions` 等注解）时，无法找到名为 **`authorizer`** 的 Bean。其原因包括：

1. **Shiro 无法自动推断你的权限模型**：需要知道哪些 `Realm`、用什么 `PermissionResolver` 等。
2. **安全性考虑**：自动创建可能导致权限过宽或配置错误。
3. **内部存在但未注册为 Bean**：`SecurityManager` 内部有 `Authorizer`，但未暴露给 Spring 容器。
4. **版本和场景差异大**：Starter 无法适配所有项目情况。

---

## 2. 解决办法：手动创建 `Authorizer` Bean

### ✅ 推荐方案：显式注册 `Authorizer`

```java
@Configuration
public class ShiroAuthorizerConfig {

  @Bean(name = "authorizer")
  public Authorizer authorizer(List<Realm> realms) {
    ModularRealmAuthorizer a = new ModularRealmAuthorizer();
    a.setRealms(realms); // 确保已有 Realm Bean
    a.setPermissionResolver(new WildcardPermissionResolver());
    return a;
  }
}
```

### ✅ 替代方案 1：从 `SecurityManager` 中获取

```java
@Bean(name = "authorizer")
public Authorizer authorizer(SecurityManager sm) {
  return ((RealmSecurityManager) sm).getAuthorizer();
}
```

### ✅ 替代方案 2：已有 Authorizer 时改名

如果你已有 `Authorizer`，只需改为：

```java
@Bean(name = "authorizer")
public Authorizer myAuthorizer(...) {
  ...
}
```

---

## 3. 为什么不会自动创建 `Authorizer`

这是 **Shiro Starter 有意不自动创建** 的设计：

- ❌ 权限模型差异大：无法猜测你的 `Realm` 和解析逻辑。
- ❌ 安全风险：错误的默认配置可能导致安全漏洞。
- ❌ 内部未注册 Bean：内部有 `Authorizer` 但未暴露给 Spring。
- ❌ 生态多样：为兼容不同版本和场景，避免强制装配。

✅ **一句话总结**：不是“不会自动创建”，而是“刻意不自动创建”，要求你**显式配置**以确保安全和正确性。

---

## 4. 为什么 `SessionManager` 也不会自动创建

### 📌 根本原因：Shiro 会话模型差异巨大

1. **场景分歧大**
    - 前后端分离、JWT → 无状态（不需要 Session）
    - 传统 Web → 有状态（必须启用 Session）

2. **实现方式多样**
    - `DefaultWebSessionManager`（Shiro 自管会话）
    - `ServletContainerSessionManager`（使用容器 Session）

3. **安全配置复杂**  
   Cookie 名称、`HttpOnly`、`SameSite`、URL 会话 ID 等，都可能影响安全。

4. **内部默认 ≠ Spring Bean**  
   虽然 `SecurityManager` 内部有默认 `SessionManager`，但不会自动注册为 Bean。

5. **生态组合复杂**  
   Redis、Spring Session、不同容器环境等，都需要手动适配。

---

## 5. 如何正确配置 `SessionManager`

### ✅ 无状态（JWT 等）

```java
@Bean
public DefaultWebSecurityManager securityManager(Realm realm) {
  DefaultWebSecurityManager sm = new DefaultWebSecurityManager(realm);

  DefaultSubjectDAO subjectDAO = (DefaultSubjectDAO) sm.getSubjectDAO();
  DefaultSessionStorageEvaluator eval = (DefaultSessionStorageEvaluator) subjectDAO.getSessionStorageEvaluator();
  eval.setSessionStorageEnabled(false);

  sm.setSessionManager(new DefaultSessionManager()); // 可选
  return sm;
}
```

---

### ✅ 有状态 Web 项目

#### 方案 1：Shiro 自管会话

```java
@Bean
public SessionManager sessionManager(SessionDAO sessionDAO) {
  DefaultWebSessionManager mgr = new DefaultWebSessionManager();
  mgr.setSessionDAO(sessionDAO);
  mgr.setGlobalSessionTimeout(30 * 60 * 1000);
  mgr.setSessionValidationSchedulerEnabled(true);
  mgr.setDeleteInvalidSessions(true);
  mgr.setSessionIdUrlRewritingEnabled(false);

  SimpleCookie cookie = new SimpleCookie("sid");
  cookie.setHttpOnly(true);
  cookie.setSecure(true);
  cookie.setSameSite("Lax");
  mgr.setSessionIdCookie(cookie);
  mgr.setSessionIdCookieEnabled(true);
  return mgr;
}

@Bean
public DefaultWebSecurityManager securityManager(SessionManager sessionManager, Realm realm) {
  DefaultWebSecurityManager sm = new DefaultWebSecurityManager(realm);
  sm.setSessionManager(sessionManager);
  return sm;
}
```

#### 方案 2：使用容器会话

```java
@Bean
public SessionManager sessionManager() {
  return new ServletContainerSessionManager();
}

@Bean
public DefaultWebSecurityManager securityManager(SessionManager sessionManager, Realm realm) {
  DefaultWebSecurityManager sm = new DefaultWebSecurityManager(realm);
  sm.setSessionManager(sessionManager);
  return sm;
}
```

---

## ✅ 总结

- `authorizer` 和 `sessionManager` **不会自动创建** 是 Shiro 的有意设计，目的是 **安全性、灵活性和兼容性**。
- 推荐始终 **显式声明 Bean** 并注入到 `SecurityManager`。
- 配置前先决定项目是 **无状态（JWT）** 还是 **有状态（传统 Session）**，再选择对应方案。

---

✅ **一句话总结：**  
Shiro Starter 不自动创建 `Authorizer` 和 `SessionManager`，是为了避免安全隐患和配置冲突。只要你显式声明它们，就能完全掌控权限和会话行为。
