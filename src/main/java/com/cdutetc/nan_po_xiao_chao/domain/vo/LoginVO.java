package com.cdutetc.nan_po_xiao_chao.domain.vo;

import lombok.Data;

/**
 * 登录成功小票（第16课升级：长短两张卡）。
 * accessToken：15 分钟，每次请求都带着（放请求头）；
 * refreshToken：7 天，只在续卡(/refresh)时使用，平时收好别外传。
 */
@Data
public class LoginVO {

    private String accessToken;
    private String refreshToken;
    private Long accessExpiresIn;   // 短卡有效期（分钟）
    private String nickname;
}
