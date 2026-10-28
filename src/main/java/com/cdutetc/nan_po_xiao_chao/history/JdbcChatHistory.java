package com.cdutetc.nan_po_xiao_chao.history;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cdutetc.nan_po_xiao_chao.domain.po.ChatMemory;
import com.cdutetc.nan_po_xiao_chao.mapper.JdbcChatMemoryMapper;
import com.cdutetc.nan_po_xiao_chao.tool.ChatMemoryUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.memory.repository.jdbc.JdbcChatMemoryRepository;
import org.springframework.ai.chat.messages.Message;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class JdbcChatHistory implements ChatMemoryRepository {

    private final JdbcChatMemoryMapper jdbcChatMemoryMapper;

    public final static String CONVERSATION_ID=org.springframework.ai.chat.memory.ChatMemory.CONVERSATION_ID;

    @Override
    public List<String> findConversationIds() {
        List<ChatMemory> chatMemories = jdbcChatMemoryMapper.selectList(new LambdaQueryWrapper<ChatMemory>());
        return chatMemories.stream().map(ChatMemory::getConversationId).collect(Collectors.toList());
    }

    @Override
    public List<Message> findByConversationId(String conversationId) {
        LambdaQueryWrapper<ChatMemory> wrapper = new LambdaQueryWrapper<ChatMemory>()
                .eq(ChatMemory::getConversationId, conversationId)
                .orderByAsc(ChatMemory::getId);
        return jdbcChatMemoryMapper.
                selectList(wrapper).
                stream().
                map(ChatMemoryUtil::createMessage).
                filter(java.util.Objects::nonNull).
                collect(Collectors.toList());
    }

    @Override
    public void saveAll(String conversationId, List<Message> messages) {
        //删除消息
        deleteByConversationId(conversationId);

        for (ChatMemory chatMemory : ChatMemoryUtil.createChatMemory(messages, conversationId)) {
            jdbcChatMemoryMapper.insert(chatMemory);
        }
    }

    @Override
    public void deleteByConversationId(String conversationId) {
        jdbcChatMemoryMapper.delete(new LambdaQueryWrapper<ChatMemory>()
                .eq(ChatMemory::getConversationId, conversationId));
    }
}
