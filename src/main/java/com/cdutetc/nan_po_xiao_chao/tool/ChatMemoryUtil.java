package com.cdutetc.nan_po_xiao_chao.tool;

import com.cdutetc.nan_po_xiao_chao.domain.po.ChatMemory;
import org.springframework.ai.chat.messages.*;
import org.springframework.ai.deepseek.DeepSeekAssistantMessage;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


public class ChatMemoryUtil {

    /** 会话标识前缀：按登录用户隔离 */
    private static final String CONVERSATION_PREFIX = "user-";

    /**
     * 会话标识：聊天、查历史、清历史必须用同一个值，别在各处硬编码 "user-" + userId。
     * 一旦不一致，历史查出来永远是空的。
     * @param userId 登录用户id
     * @return 会话标识，如 user-1
     */
    public static String conversationId(String userId) {
        return CONVERSATION_PREFIX + userId;
    }

    public static Message createMessage(ChatMemory chatMemory){
        //* 消息类型: USER/ASSISTANT/SYSTEM/TOOL
        switch (chatMemory.getType()) {
            case "USER":
                return new UserMessage(chatMemory.getContent());
            case "ASSISTANT":
                return new AssistantMessage(chatMemory.getContent());
            case "SYSTEM":
                return new SystemMessage(chatMemory.getContent());
            case "TOOL":
                // 工具消息无法仅凭文本还原，跳过
                return null;
        }
        return null;
    }


    public static List<ChatMemory> createChatMemory(List<Message> messages, String conversationId){

        List<ChatMemory> chatMemories = new ArrayList<ChatMemory>(messages.size());
        for (Message message : messages) {
            chatMemories.add(ChatMemory.builder().
                    conversationId(conversationId).
                    content(message.getText()).
                    type(message.getMessageType().name()).
                    timestamp(LocalDateTime.now()).
                    build()
            );
        }
        return chatMemories;
    }
}
