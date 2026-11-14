package com.cdutetc.nan_po_xiao_chao;

import com.cdutetc.nan_po_xiao_chao.domain.po.Order;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.function.Consumer;

@SpringBootTest
// 用 test profile 提供占位密钥,避免依赖本机的 .env / 环境变量
@ActiveProfiles("test")
class NanPoXiaoChaoApplicationTests {

    // Qwen 现在由 spring-ai-starter-model-openai 按 spring.ai.openai.* 自动装配,
    // 不再是手写的 DashScopeChatModel(DashScope 那条路在 Spring AI 2.0.1 上走不通)
    @Autowired
    private OpenAiChatModel chatModel;

    @Autowired
    @Qualifier("qwen")
    private ChatClient qwenChatClient;

    // 真实调用外部 AI 的集成测试:默认跳过,避免 mvn package 依赖网络和真实密钥
    @Test
    @EnabledIfEnvironmentVariable(named = "RUN_AI_TESTS", matches = "true")
    void testQwenChat() {
        String content = qwenChatClient.prompt()
                .user(new Consumer<ChatClient.PromptUserSpec>() {
                    @Override
                    public void accept(ChatClient.PromptUserSpec promptUserSpec) {
                        promptUserSpec.text("你叫什么名字？");
                    }
                })
                .call()
                .content();
        System.out.println("QWEN REPLY >>> " + content);
        assertNotNull(content, "千问返回内容为空");


    }

    /**
     * 携带图片调用:按请求覆盖成视觉模型 qwen3-vl-plus,走同一套 DashScope OpenAI 兼容端点。
     * 用项目里真实的一张菜品/大屏截图验证模型确实能读图,并走流式(与 /ai/chat/stream/image 的契约一致)。spring-ai
     */
    @Test
    @EnabledIfEnvironmentVariable(named = "RUN_AI_TESTS", matches = "true")
    void testQwenImageChat() {
        FileSystemResource image = new FileSystemResource(
                "src/main/resources/image/2026-10-13/69db25ac-2e59-495e-845a-15173364f0ff.png");
        assertTrue(image.exists(), "测试图片不存在");

        List<String> chunks = qwenChatClient.prompt()
                .options(OpenAiChatOptions.builder().model("qwen3-vl-plus"))
                .user(u -> u.text("这张图是什么内容？用一句话概括，并说出图中最显眼的标题文字。")
                        .media(MediaType.IMAGE_PNG, image))
                .stream()
                .content()
                .collectList()
                .block();

        String content = String.join("", chunks == null ? List.of() : chunks);
        System.out.println("QWEN IMAGE STREAM REPLY >>> " + content);
        assertFalse(content.isBlank(), "千问图片流式调用返回内容为空");
    }

    @Test
    void contextLoads() throws NoSuchFieldException {
        Class<Order> orderClass = Order.class;
        Field orderList = orderClass.getDeclaredField("orderList");
        Type genericType = orderList.getGenericType();
        if(genericType instanceof ParameterizedType){
            System.out.println(((ParameterizedType) genericType).getActualTypeArguments()[0]);
        }
    }

    @Test
    void testChatModel() {
        System.out.println(chatModel.getDefaultOptions());
    }
}
