package com.cdutetc.nan_po_xiao_chao.service;

import com.cdutetc.nan_po_xiao_chao.domain.dto.LoginDTO;
import com.cdutetc.nan_po_xiao_chao.domain.dto.RefreshDTO;
import com.cdutetc.nan_po_xiao_chao.domain.dto.RegisterDTO;
import com.cdutetc.nan_po_xiao_chao.domain.vo.LoginVO;

public interface UserService {

    /**
     * 注册:校验用户名唯一,密码 BCrypt 哈希后入库
     */
    void register(RegisterDTO registerDTO);

    /**
     * 登录:校验用户名密码,签发双令牌
     */
    LoginVO login(LoginDTO loginDTO);

    /**
     * 刷新:校验 refresh token 后换发新的双令牌
     */
    LoginVO refresh(RefreshDTO refreshDTO);
}
