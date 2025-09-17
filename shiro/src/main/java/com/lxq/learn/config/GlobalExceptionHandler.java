package com.lxq.learn.config;

import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.authz.UnauthenticatedException;
import org.apache.shiro.authz.UnauthorizedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * 全局异常处理器
 * 专门处理 Shiro 相关的认证和授权异常
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理未认证异常（用户未登录）
     * 当用户访问需要认证的资源但未登录时触发
     *
     * @param e 未认证异常
     * @return 错误响应
     */
    @ExceptionHandler(UnauthenticatedException.class)
    public ResponseEntity<Map<String, Object>> handleUnauthenticatedException(UnauthenticatedException e) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("code", 401);
        result.put("message", "用户未登录，请先登录");
        result.put("error", "Unauthenticated");

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(result);
    }

    /**
     * 处理未授权异常（用户无权限）
     * 当用户已登录但没有访问特定资源的权限时触发
     *
     * @param e 未授权异常
     * @return 错误响应
     */
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<Map<String, Object>> handleUnauthorizedException(UnauthorizedException e) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("code", 403);
        result.put("message", "权限不足，无法访问该资源");
        result.put("error", "Unauthorized");

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(result);
    }

    /**
     * 处理授权异常（包含认证和授权相关的所有异常）
     * 这是一个更通用的异常处理器，捕获所有授权相关异常
     *
     * @param e 授权异常
     * @return 错误响应
     */
    @ExceptionHandler(AuthorizationException.class)
    public ResponseEntity<Map<String, Object>> handleAuthorizationException(AuthorizationException e) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("code", 403);
        result.put("message", "授权失败：" + e.getMessage());
        result.put("error", "Authorization Failed");

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(result);
    }

    /**
     * 处理通用异常
     * 捕获其他未被特殊处理的异常
     *
     * @param e 通用异常
     * @return 错误响应
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception e) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("code", 500);
        result.put("message", "服务器内部错误：" + e.getMessage());
        result.put("error", "Internal Server Error");

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
    }
}