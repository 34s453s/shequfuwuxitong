package com.example.communityserver.config;

import com.example.communityserver.service.RedisService;
import com.example.communityserver.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;

@Component
public class JwtInterceptor implements HandlerInterceptor {

    private static final String TOKEN_BLACKLIST_PREFIX = "token:blacklist:";

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired(required = false)
    private RedisService redisService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 放行 OPTIONS 预检请求
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            try {
                Claims claims = jwtUtil.parseToken(token);

                // 检查Token是否在黑名单中
                if (redisService != null && Boolean.TRUE.equals(redisService.hasKey(TOKEN_BLACKLIST_PREFIX + token))) {
                    sendErrorResponse(response, 401, "Token已失效，请重新登录");
                    return false;
                }

                request.setAttribute("userId", claims.get("userId", Long.class));
                request.setAttribute("username", claims.get("username", String.class));
                request.setAttribute("role", claims.get("role", String.class));
                return true;
            } catch (ExpiredJwtException e) {
                // Token 已过期
                sendErrorResponse(response, 401, "Token已过期，请重新登录");
                return false;
            } catch (JwtException e) {
                // Token 无效
                sendErrorResponse(response, 401, "Token无效，请重新登录");
                return false;
            }
        }
        // 没有Token
        sendErrorResponse(response, 401, "未登录，请先登录");
        return false;
    }

    /**
     * 发送错误响应
     */
    private void sendErrorResponse(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"code\":" + status + ",\"message\":\"" + message + "\"}");
    }
}
