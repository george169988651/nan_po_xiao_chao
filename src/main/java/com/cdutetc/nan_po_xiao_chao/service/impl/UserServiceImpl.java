package com.cdutetc.nan_po_xiao_chao.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cdutetc.nan_po_xiao_chao.domain.dto.LoginDTO;
import com.cdutetc.nan_po_xiao_chao.domain.dto.RefreshDTO;
import com.cdutetc.nan_po_xiao_chao.domain.dto.RegisterDTO;
import com.cdutetc.nan_po_xiao_chao.domain.po.User;
import com.cdutetc.nan_po_xiao_chao.domain.vo.LoginVO;
import com.cdutetc.nan_po_xiao_chao.exception.BusinessException;
import com.cdutetc.nan_po_xiao_chao.mapper.UserMapper;
import com.cdutetc.nan_po_xiao_chao.service.UserService;
import com.cdutetc.nan_po_xiao_chao.tool.JWTUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    /**
     * access token 有效期(分钟),需与 JWTUtil.generateAccessToken 保持一致
     */
    private static final long ACCESS_EXPIRES_MINUTES = 2 * 60;

    private final UserMapper userMapper;

    @Override
    public void register(RegisterDTO registerDTO) {
        if (StrUtil.isBlank(registerDTO.getUsername()) || StrUtil.isBlank(registerDTO.getPassword())) {
            throw new BusinessException(400, "用户名和密码不能为空");
        }

        Long count = userMapper.selectCount(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, registerDTO.getUsername()));
        if (count > 0) {
            throw new BusinessException(400, "用户名已存在");
        }

        User user = new User();
        user.setUsername(registerDTO.getUsername());
        // 只存哈希,明文密码不落库
        user.setPassword(BCrypt.hashpw(registerDTO.getPassword()));
        user.setNickname(StrUtil.blankToDefault(registerDTO.getNickname(), registerDTO.getUsername()));
        userMapper.insert(user);
        log.info("用户注册成功: {}", registerDTO.getUsername());
    }

    @Override
    public LoginVO login(LoginDTO loginDTO) {
        if (StrUtil.isBlank(loginDTO.getUsername()) || StrUtil.isBlank(loginDTO.getPassword())) {
            throw new BusinessException(400, "用户名和密码不能为空");
        }

        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, loginDTO.getUsername()));
        // 用户不存在和密码错误统一提示,避免暴露"该用户名已注册"
        if (user == null || !BCrypt.checkpw(loginDTO.getPassword(), user.getPassword())) {
            throw new BusinessException(401, "用户名或密码错误");
        }

        return buildLoginVO(user);
    }

    /**
     * 续卡:用长效的 refreshToken 换发一套新的登录凭证
     * 校验令牌类型与用户有效性后,重新签发 accessToken/refreshToken
     * @param refreshDTO 携带 refreshToken 的请求参数
     * @return 全新的登录凭证 LoginVO
     */
    @Override
    public LoginVO refresh(RefreshDTO refreshDTO) {
        // refreshToken 缺失直接拒绝,后续解析无意义
        if (StrUtil.isBlank(refreshDTO.getRefreshToken())) {
            throw new BusinessException(400, "refreshToken 不能为空");
        }

        // 必须是 refresh 类型令牌,防止拿 access token 冒充续卡
        if (!"refresh".equals(JWTUtil.parseTokenType(refreshDTO.getRefreshToken()))) {
            throw new BusinessException(401, "令牌类型错误,请提供 refreshToken");
        }

        // 从令牌 subject 取出 userId,回查用户是否仍然存在(可能已被注销)
        String userId = JWTUtil.parseUserId(refreshDTO.getRefreshToken());
        User user = userMapper.selectById(Long.valueOf(userId));
        if (user == null) {
            throw new BusinessException(401, "用户不存在");
        }

        // 校验通过,签发一套全新的凭证返回
        return buildLoginVO(user);
    }

    private LoginVO buildLoginVO(User user) {
        LoginVO loginVO = new LoginVO();
        loginVO.setAccessToken(JWTUtil.generateAccessToken(user.getId().toString(),
                StrUtil.blankToDefault(user.getNickname(), user.getUsername())));
        loginVO.setRefreshToken(JWTUtil.generateRefreshToken(user.getId().toString(), user.getUsername()));
        loginVO.setAccessExpiresIn(ACCESS_EXPIRES_MINUTES);
        loginVO.setNickname(user.getNickname());
        return loginVO;
    }
}
