package com.cdutetc.nan_po_xiao_chao.interceptor;

import cn.hutool.core.lang.Pair;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.cdutetc.nan_po_xiao_chao.context.UserContext;
import com.cdutetc.nan_po_xiao_chao.domain.Result;
import com.cdutetc.nan_po_xiao_chao.tool.JWTUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.nio.charset.StandardCharsets;

@Component
@Slf4j
public class LoginInterceptor implements HandlerInterceptor {

    /**
     * 请求属性键,控制器可通过 request.getAttribute(LoginInterceptor.USER_ID_ATTR) 取当前登录用户id
     */
    public static final String USER_ID_ATTR = "userId";

    private static final String SKIP="SKIP";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        log.info("拦截调用: {}",request.getRequestURI());
        String authorization = request.getHeader("Authorization");
        if(skip(authorization)){
            return true;
        }
        log.info("Authorization: {}", authorization);
        if (StrUtil.isBlank(authorization) || !authorization.startsWith("Bearer ")) {
            return writeUnauthorized(response, "未登录,请携带 Authorization: Bearer <token>");
        }

        String token = authorization.substring("Bearer ".length());
        try {
            // 只接受 access 类型令牌,refresh token 不能当作访问凭证
            if (!"access".equals(JWTUtil.parseTokenType(token))) {
                return writeUnauthorized(response, "令牌类型错误,请使用 accessToken");
            }
            String userId = JWTUtil.parseUserId(token);
            String userName = JWTUtil.parseNickname(token);
            request.setAttribute(USER_ID_ATTR, userId);// 设置请求属性,供控制器获取当前登录用户id
            log.info("当前登录用户: {}", userId);
            UserContext.setCurrentUser(new Pair<>(userId,userName ));
            return true;
        } catch (Exception e) {
            log.info("令牌校验失败: {}", e.getMessage());
            return writeUnauthorized(response, e.getMessage());
        }
    }

    private boolean writeUnauthorized(HttpServletResponse response, String message) throws Exception {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write(JSONUtil.toJsonStr(Result.error(401, message)));
        return false;
    }

    private boolean skip(String authorization){
        return SKIP.equals(authorization);
    }
}
