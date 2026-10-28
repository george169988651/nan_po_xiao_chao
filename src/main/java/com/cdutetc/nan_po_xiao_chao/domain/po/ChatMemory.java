package com.cdutetc.nan_po_xiao_chao.domain.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Spring AI 聊天记忆表实体类
 */
@Data
@TableName("SPRING_AI_CHAT_MEMORY")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatMemory implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 会话标识
     */
    private String conversationId;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 消息类型: USER/ASSISTANT/SYSTEM/TOOL
     */
    private String type;

    /**
     * 消息时间戳
     */
    private LocalDateTime timestamp;
}
