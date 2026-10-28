package com.cdutetc.nan_po_xiao_chao.service.impl;

import com.cdutetc.nan_po_xiao_chao.context.UserContext;
import com.cdutetc.nan_po_xiao_chao.domain.Result;
import com.cdutetc.nan_po_xiao_chao.domain.dto.ChatDTO;
import com.cdutetc.nan_po_xiao_chao.domain.dto.OrderDTO;
import com.cdutetc.nan_po_xiao_chao.domain.po.Menu;
import com.cdutetc.nan_po_xiao_chao.domain.vo.ChatMessageVO;
import com.cdutetc.nan_po_xiao_chao.domain.vo.OrderVO;
import com.cdutetc.nan_po_xiao_chao.enmu.AgentEnum;
import com.cdutetc.nan_po_xiao_chao.exception.BusinessException;
import com.cdutetc.nan_po_xiao_chao.router.RouterAgent;
import com.cdutetc.nan_po_xiao_chao.service.AiService;
import com.cdutetc.nan_po_xiao_chao.service.MenuService;

import com.cdutetc.nan_po_xiao_chao.tool.ChatMemoryUtil;
import com.cdutetc.nan_po_xiao_chao.tool.OssUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.content.Media;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiServiceImpl implements AiService {

    @Autowired
    @Qualifier("user")
    private ChatClient chatClient;

    @Autowired
    private RouterAgent routerAgent;

    @Autowired
    @Qualifier("qwen")
    private ChatClient qwenChatClient;

    @Autowired
    @Qualifier("orderParser")
    private ChatClient orderParser;

    @Autowired
    private MenuService menuService;

    @Autowired
    @Qualifier("jdbcChatHistory")
    private ChatMemoryRepository chatMemoryRepository;

    @Autowired
    private OssUtil ossUtil;

    /** SSE 连接最长保持(毫秒),要大于模型最慢一档的生成耗时 */
    private final static long TIME_OUT = 180000L;

    private final static String END_MARK = "[DONE]";

    /**
     * 聊天流
     * @return sseEmitter
     */
    @Override
    public SseEmitter chatStream(ChatDTO chatDTO) throws URISyntaxException {
        String message = chatDTO.getMessage();
        String userId = chatDTO.getUserId();
        if (message == null || message.isBlank()) {
            throw new BusinessException(400, "消息为空");
        }

        //SseEmitter对象
        SseEmitter sseEmitter = new SseEmitter(TIME_OUT);

        //这三个回调都在响应收尾阶段触发,只做日志;发数据统一在 subscribe 回调里做
        sseEmitter.onCompletion(() -> log.info("SseEmitter 完成"));

        sseEmitter.onTimeout(() -> {
            log.warn("SseEmitter 超时,强制关闭");
            sseEmitter.complete();
        });

        sseEmitter.onError(throwable -> log.error("SseEmitter 异常: {}", throwable.getMessage()));

        //异步调用 调用大模型接口
        chatClient.prompt()
                .user(message)
                .advisors(advisorSpec -> advisorSpec.param(ChatMemory.CONVERSATION_ID,
                        ChatMemoryUtil.conversationId(userId)))
                .toolContext(Map.of("userId", Long.valueOf(UserContext.getCurrentUser().getKey()),
                        "userName", UserContext.getCurrentUser().getValue()))
                .stream()
                .content()
                .filter(s -> s != null && !s.isEmpty())
                .subscribe(
                        s -> send(sseEmitter, s),
                        sseEmitter::completeWithError,
                        () -> {
                            //先发结束标记再关连接,顺序反了客户端就收不到 [DONE]
                            send(sseEmitter, END_MARK);
                            sseEmitter.complete();
                        }
                );

        //返回连接
        return sseEmitter;
    }

    /**
     * 查询当前用户的历史会话记录
     * @param userId 登录用户id
     * @return Result<List<ChatMessageVO>>
     */
    @Override
    public Result getChatMemory(String userId) {
        //会话 id 必须和 chatStream 用的一致,否则查出来永远是空
        List<Message> messages = chatMemoryRepository.findByConversationId(ChatMemoryUtil.conversationId(userId));

        List<ChatMessageVO> history = messages.stream()
                .map(AiServiceImpl::toChatMessageVO)
                .filter(Objects::nonNull)
                .toList();

        return Result.success(history);
    }

    /**
     * 清空当前用户的会话记忆。
     * MessageWindowChatMemory 每次都是现读仓库、没有进程内缓存,所以删完立刻生效,不用重启。
     * @param userId 登录用户id
     * @return Result
     */
    @Override
    public Result clearChatMemory(String userId) {
        String conversationId = ChatMemoryUtil.conversationId(userId);
        chatMemoryRepository.deleteByConversationId(conversationId);
        log.info("已清空会话记忆: {}", conversationId);
        return Result.success(null);
    }

    /**
     * 多模态聊天
     * @return sseEmitter
     */
    @Override
    public SseEmitter chatStreamImage(ChatDTO chatImageDTO) throws URISyntaxException, IOException {
        if(chatImageDTO == null ){
            throw new BusinessException(400, "消息或图片为空");
        }

        UserMessage userMessage;

        if(chatImageDTO.getImage() != null && !chatImageDTO.getImage().isEmpty()){
            // 上传图片到OSS服务器,再把公开 URL 交给模型
            List<Media> medias = chatImageDTO.getImage()
                    .stream()
                    .map(file -> {
                        try {
                            String url = ossUtil.upload(file);
                            return new Media(MediaType.IMAGE_PNG, new URI(url));
                        } catch (IOException | URISyntaxException e) {
                            throw new RuntimeException(e);
                        }
                    }).toList();
            userMessage = UserMessage
                    .builder()
                    .text(chatImageDTO.getMessage())
                    .media(medias)
                    .build();
        }else{
            userMessage = UserMessage.builder().text(chatImageDTO.getMessage()).build();
        }

        SseEmitter sseEmitter=initSseEmitter();

        log.info("用户消息：{}", chatImageDTO.getMessage());

        qwenChatClient.prompt(
                new Prompt(userMessage)
        ).stream()
                .content()
                .filter(s -> s != null && !s.isEmpty())
                .subscribe(
                        s -> send(sseEmitter, s),
                        sseEmitter::completeWithError,
                        () -> {
                            send(sseEmitter, END_MARK);
                            sseEmitter.complete();
                        }
                );
        return sseEmitter;
    }

    /**
     * 第18课：AI 结构化下单。
     * 先把真实菜单塞进提示词，让解析器只能从菜单里挑菜；
     * 解析结果不直接落库，而是交给 MenuService.order 走人工下单同一套校验、事务和记名逻辑。
     */
    @Override
    public OrderVO orderByAi(String message) {
        if (message == null || message.isBlank()) {
            throw new BusinessException(400, "消息为空");
        }

        @SuppressWarnings("unchecked")
        List<Menu> menus = (List<Menu>) menuService.queryMenu().getData();
        String menuText = menus.stream()
                .map(menu -> menu.getName() + "￥" + menu.getPrice())
                .collect(Collectors.joining("、"));

        OrderDTO orderDTO;
        try {
            orderDTO = orderParser.prompt()
                    .user("本店菜单：" + menuText + "\n顾客说：" + message)
                    .call()
                    .entity(OrderDTO.class);
        } catch (Exception e) {
            log.error("AI 点单解析失败", e);
            throw new BusinessException(500, "AI 点单解析失败: " + e.getMessage());
        }

        if (orderDTO == null || orderDTO.getDishList() == null || orderDTO.getDishList().isEmpty()) {
            throw new BusinessException(40401, "没有识别出菜品");
        }

        Result result = menuService.order(orderDTO);
        return (OrderVO) result.getData();
    }

    /**
     * 模型路由
     * @return sseEmitter
     */
    @Override
    public SseEmitter chat(ChatDTO chatDTO) throws URISyntaxException, IOException {
        if(chatDTO == null) {
            throw new BusinessException(400, "消息为空");
        }
        // 带图片直接进厨师分支,不再让文本路由去猜(问"这张图是什么"会被判成 UNKNOWN)
        if (chatDTO.getImage() != null && !chatDTO.getImage().isEmpty()) {
            return chatStreamImage(chatDTO);
        }
        AgentEnum route = routerAgent.route(chatDTO.getMessage());
        if(route == AgentEnum.UNKNOWN){
            return null;
        }
        return switch (route) {
            case USER_AGENT -> chatStream(chatDTO);
            case CHIEF_AGENT -> chatStreamImage(chatDTO);
            default -> null;
        };
    }

    /**
     * 初始化SseEmitter
     */
    private SseEmitter initSseEmitter() {
        SseEmitter sseEmitter = new SseEmitter(TIME_OUT);
        sseEmitter.onError(throwable -> log.error("SseEmitter 异常: {}", throwable.getMessage()));
        sseEmitter.onCompletion(() -> log.info("SseEmitter 完成"));
        sseEmitter.onTimeout(() -> {
            log.warn("SseEmitter 超时,强制关闭");
            sseEmitter.complete();
        });
        return sseEmitter;
    }

    /**
     * Message → VO。前端只认 user/assistant 两种角色,
     * 其余(SYSTEM 等)返回 null,由调用方 filter 掉;空内容的消息也不展示。
     */
    private static ChatMessageVO toChatMessageVO(Message message) {
        MessageType type = message.getMessageType();
        String role;
        if (type == MessageType.USER) {
            role = "user";
        } else if (type == MessageType.ASSISTANT) {
            role = "assistant";
        } else {
            return null;
        }

        String content = message.getText();
        if (content == null || content.isBlank()) {
            return null;
        }
        return ChatMessageVO.of(role, content);
    }

    /** 发送一段 SSE 数据,统一收口三处 try/catch */
    private void send(SseEmitter emitter, String data) {
        try {
            emitter.send(SseEmitter.event().data(data).build());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
