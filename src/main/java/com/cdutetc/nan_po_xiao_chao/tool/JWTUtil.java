package com.cdutetc.nan_po_xiao_chao.tool;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.cdutetc.nan_po_xiao_chao.exception.BusinessException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Date;
import java.util.Map;

public class JWTUtil {

    // HMAC 签名密钥原文，HS256 要求至少 32 字节，否则 Keys.hmacShaKeyFor 会抛异常
    private static final String SECRET_KEY="abcdshdusdiiqwhosahdovgsyfduyafd";

    // 由密钥原文派生的签名密钥，两种令牌共用
    private static final SecretKey KEY = Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8));

    // JwtParser 不可变且线程安全，构建一次全局复用，避免每次解析都重复 build
    private static final JwtParser PARSER = Jwts.parser()
            .verifyWith(KEY)
            .build();

    /**
     * 生成访问令牌（短卡），随每次请求放入请求头
     * subject 为用户id，载荷带 nickname（下单记名用）与 type=access，有效期 2 小时
     * @param userId 用户id
     * @param nickname 用户昵称
     * @return 访问令牌字符串
     */
    public static String generateAccessToken(String userId, String nickname){
        return buildToken(userId, Map.of("type", "access", "nickname", nickname), Duration.ofHours(2),KEY);
    }

    /**
     * 生成刷新令牌（长卡），仅在续卡时使用
     * 载荷中携带 username 与 type=refresh，据此换发新的访问令牌，有效期 7 天
     * @param userId 用户id
     * @param username 用户名
     * @return 刷新令牌字符串
     */
    public static String generateRefreshToken(String userId,String username){
        return  buildToken(userId, Map.of("username", username, "type", "refresh"), Duration.ofDays(7),KEY);
    }


    /**
     * 构建并签发 JWT
     * @param subject 主题，此处用于标识令牌类型（access_token/refresh_token）
     * @param claims 自定义载荷，为空则不写入
     * @param expire 有效期，从签发时刻起算
     * @param key 签名密钥
     * @return 紧凑格式的 JWT 字符串
     */
    private static String buildToken(String subject, Map<String,Object> claims, Duration expire, SecretKey key){
        JwtBuilder jwtBuilder = Jwts.builder()
                .subject(subject)
                .issuedAt(new Date())// 签发时间
                .expiration(new Date(System.currentTimeMillis() + expire.toMillis()))// 有效期
                .signWith(key);// 签名密钥4

        // 有自定义载荷时才写入，避免生成空的 claims 节点
        if(!claims.isEmpty()){
            jwtBuilder.claims(claims);// 写入自定义载荷
        }
        return jwtBuilder.compact();// 签发令牌
    }

    /**
     * 解析访问令牌，取出载荷中的 nickname
     * @param token 访问令牌字符串
     * @return nickname，载荷中不存在时返回空字符串
     */
    public static String parseNickname(String token){
        Claims payload = parseClaims(token);
        return payload.getOrDefault("nickname", "").toString();
    }

    /**
     * 解析访问令牌，取出 subject（即签发时写入的 userId）
     * @param token 访问令牌字符串
     * @return userId
     */
    public static String parseUserId(String token){
        return parseClaims(token).getSubject();
    }

    /**
     * 解析令牌类型（access / refresh），用于拦截器和刷新接口区分令牌用途
     * @param token 令牌字符串
     * @return type 声明的值，缺失时返回空字符串
     */
    public static String parseTokenType(String token){
        return parseClaims(token).getOrDefault("type", "").toString();
    }

    /**
     * 校验并解析 JWT，返回其载荷
     * 令牌非法、过期或签名不匹配时统一抛出 BusinessException，交由全局异常处理
     * @param token 令牌字符串
     * @return 解析出的 Claims 载荷
     */
    private static Claims parseClaims(String token){
        if (StrUtil.isBlank(token)) {
            throw new BusinessException(401, "令牌不能为空");
        }
        try {
            return PARSER.parseSignedClaims(token).getPayload();
        } catch (ExpiredJwtException e) {
            throw new BusinessException(401, "令牌已过期");
        } catch (JwtException | IllegalArgumentException e) {
            throw new BusinessException(401, "令牌无效");
        }
    }

}
