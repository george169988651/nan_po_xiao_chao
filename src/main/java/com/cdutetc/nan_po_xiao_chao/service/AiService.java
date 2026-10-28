package com.cdutetc.nan_po_xiao_chao.service;

import com.cdutetc.nan_po_xiao_chao.domain.Result;
import com.cdutetc.nan_po_xiao_chao.domain.dto.ChatDTO;
import com.cdutetc.nan_po_xiao_chao.domain.vo.OrderVO;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.net.URISyntaxException;

public interface AiService {

    SseEmitter chatStream(ChatDTO chatDTO) throws URISyntaxException;

    /**
     * 查询当前用户的历史会话记录
     * @param userId 登录用户id
     * @return Result<List<ChatMessageVO>>，只含 user/assistant 两种角色
     */
    Result getChatMemory(String userId);

    /**
     * 清空当前用户的会话记忆
     * @param userId 登录用户id
     * @return Result
     */
    Result clearChatMemory(String userId);

    SseEmitter chatStreamImage(ChatDTO chatDTO) throws URISyntaxException, IOException;

    SseEmitter chat(ChatDTO chatDTO) throws URISyntaxException, IOException;

    /** 第18课：自然语言 → OrderDTO → 走 MenuService 的老收银台 */
    OrderVO orderByAi(String message);
}
