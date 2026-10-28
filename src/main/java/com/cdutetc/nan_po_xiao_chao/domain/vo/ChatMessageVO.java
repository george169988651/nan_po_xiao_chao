package com.cdutetc.nan_po_xiao_chao.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 历史会话消息 VO（出参：后端 → 前端）。
 * 字段和前端约定死了：role 只出 user / assistant（小写），content 是消息文本。
 * 不直接返回 Spring AI 的 Message 或 ChatMemory 实体——Message 内部还带元数据、工具调用，
 * 实体返回去则会把数据库字段（甚至别人的会话）一起暴露。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
public class ChatMessageVO {

    /** 说话人：user=顾客，assistant=AI */
    private String role;

    /** 消息文本 */
    private String content;
}
