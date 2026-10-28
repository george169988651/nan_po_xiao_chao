package com.cdutetc.nan_po_xiao_chao.configuration;

import com.cdutetc.nan_po_xiao_chao.interceptor.LoginInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.InterceptorRegistration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
public class InterceptorConfig implements WebMvcConfigurer {

    @Autowired
    private LoginInterceptor loginInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {

        // 放行:预检请求、认证三接口、接口文档、H2 控制台、错误页
        InterceptorRegistration registration = registry.
                addInterceptor(loginInterceptor).
                excludePathPatterns(List.of(
                        "OPTIONS",
                        "/user/login",
                        "/user/register",
                        "/user/refresh",
                        "/error",
                        "/favicon.ico",
                        "/h2-console/**",
                        "/v3/api-docs/**",
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/doc.html",
                        "/webjars/**",
                        "/files/**",
                        // MCP Server:SSE 建会话 + JSON-RPC 消息端点,由 MCP 协议自己鉴权
                        "/sse",
                        "/mcp/**"
                ));
    }
}
