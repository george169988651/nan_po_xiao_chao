package com.cdutetc.nan_po_xiao_chao.controller;


import com.cdutetc.nan_po_xiao_chao.domain.Result;
import com.cdutetc.nan_po_xiao_chao.domain.dto.ChatDTO;
import com.cdutetc.nan_po_xiao_chao.interceptor.LoginInterceptor;
import com.cdutetc.nan_po_xiao_chao.service.AiService;
import com.cdutetc.nan_po_xiao_chao.tool.ChatMemoryUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@RestController
@RequestMapping("/ai")
@Slf4j
@RequiredArgsConstructor
@Tag(name = "ai服务接口")
public class AiController {

    @Autowired
    @Qualifier(value = "Ollama")
    private ChatClient chatClient;

    private final AiService aiService;


    @PostMapping("/chat")
    @Operation(summary = "带记忆的AI对话接口,会话按登录用户隔离")
    public Result chat(@RequestBody Map<String,Object>map,
                       @RequestAttribute(value = LoginInterceptor.USER_ID_ATTR,required = false) String userId) {
        String message = (String) map.get("message");
        String content = chatClient.prompt()
                .user(message)

                .advisors(new Consumer<ChatClient.AdvisorSpec>() {
                    @Override
                    public void accept(ChatClient.AdvisorSpec advisorSpec) {
                        advisorSpec.param(ChatMemory.CONVERSATION_ID, ChatMemoryUtil.conversationId(userId));
                    }
                })
                .call()
                .content();
        return Result.success(content);
    }

    @PostMapping("/chat/stream")
    @Operation(summary = "实现会话的流式对话接口，会话按登录用户隔离")
    public SseEmitter chatStream(@RequestBody Map<String,String> request,
                                 @RequestAttribute(LoginInterceptor.USER_ID_ATTR) String userId) throws URISyntaxException, IOException {
        String message = request.get("message");
        log.info("chatStream: {}", message);
        return aiService.chat(ChatDTO.builder()
                .message(message)
                .userId(userId)
                .build());
    }

    @PostMapping(value = "/chat/stream/image", consumes = "multipart/form-data")
    @Operation(summary = "图片流式对话 ")
    public SseEmitter chatStreamImage(
            @RequestPart("file") MultipartFile file,
            @RequestParam(value = "message", required = false) String message,
            @RequestAttribute(LoginInterceptor.USER_ID_ATTR) String userId
    ) throws URISyntaxException, IOException {
        // 前端允许只发图片:message 缺省时给一句默认提示词,否则路由拿不到文本
        String prompt = (message == null || message.isBlank())
                ? "请识别这张图片里的食材，并按你的流程给出建议。"
                : message;
        return aiService.chat(ChatDTO.builder()
                .message(prompt)
                .userId(userId)
                .image(List.of(file))
                .build());
    }

    @GetMapping("/memory")
    @Operation(summary = "查询当前用户的历史会话记录,会话按登录用户隔离")
    public Result getChatMemory(@RequestAttribute(LoginInterceptor.USER_ID_ATTR) String userId){
        return aiService.getChatMemory(userId);
    }

    @DeleteMapping("/memory")
    @Operation(summary = "清空当前用户的会话记忆,会话按登录用户隔离")
    public Result clearChatMemory(@RequestAttribute(LoginInterceptor.USER_ID_ATTR) String userId){
        return aiService.clearChatMemory(userId);
    }

}
