package com.lxq.learn.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;

/**
 * CORS跨域配置
 * 解决前端访问后端API时的跨域问题
 *
 * 支持的前端域名：
 * - http://localhost:5173 (Vite默认端口)
 * - http://localhost:3000 (React默认端口)
 * - http://localhost:8081 (Vue CLI默认端口)
 */
@Configuration
public class CorsConfig {

    /**
     * 配置CORS过滤器
     * 允许前端跨域访问后端API
     *
     * @return CorsFilter CORS过滤器
     */
    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration configuration = new CorsConfiguration();

        // 1. 允许的前端域名（精确配置，提高安全性）
        configuration.setAllowedOrigins(Arrays.asList(
                "http://localhost:5173",    // Vite开发服务器
                "http://localhost:3000",    // React/Next.js开发服务器
                "http://localhost:8081",    // Vue CLI开发服务器
                "http://127.0.0.1:5173",    // Vite开发服务器（127.0.0.1）
                "http://127.0.0.1:3000",    // React开发服务器（127.0.0.1）
                "http://127.0.0.1:8081",
                "http://localhost:63342"// Vue CLI开发服务器（127.0.0.1）
        ));

        // 2. 允许的HTTP方法
        configuration.setAllowedMethods(Arrays.asList(
                "GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD"
        ));

        // 3. 允许的请求头
        configuration.setAllowedHeaders(Arrays.asList(
                "Origin",
                "Content-Type",
                "Accept",
                "Authorization",
                "X-Requested-With",
                "Cache-Control",
                "Access-Control-Request-Method",
                "Access-Control-Request-Headers"
        ));

        // 4. 暴露的响应头（前端可以访问的响应头）
        configuration.setExposedHeaders(Arrays.asList(
                "Access-Control-Allow-Origin",
                "Access-Control-Allow-Credentials",
                "Content-Disposition"
        ));

        // 5. 允许携带认证信息（Cookie、Session等）
        // 这对于基于Session的Shiro认证非常重要
        configuration.setAllowCredentials(true);

        // 6. 预检请求的缓存时间（秒）
        configuration.setMaxAge(3600L);

        // 7. 配置CORS规则应用的路径
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration); // 对所有路径生效

        return new CorsFilter(source);
    }

    /**
     * 提供CORS配置源（备用方法）
     * 如果使用WebMvcConfigurer方式需要此方法
     *
     * @return CorsConfigurationSource
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(Arrays.asList(
                "http://localhost:5173",
                "http://localhost:3000",
                "http://localhost:8081",
                "http://127.0.0.1:5173",
                "http://127.0.0.1:3000",
                "http://127.0.0.1:8081"
        ));

        configuration.setAllowedMethods(Arrays.asList("*"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}