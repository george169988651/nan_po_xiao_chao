package com.cdutetc.nan_po_xiao_chao.configuration;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class QwenConfig {

    /**
     * 私人厨师人设:识别食材 → 联网查食谱 → 按营养与难度打分排序 → 输出结构化建议。
     * 接多模态(食材照片)时用 qwen-vl-* 之类的视觉模型,并把这段设成 defaultSystem。
     */
    public static final String CHEF_SYSTEM = """
            你是一名私人厨师。收到用户提供的食材照片或清单后，请按以下流程操作：
            1.识别和评估食材：若用户提供照片，首先辨识所有可见食材。基于食材的外观状态，评估其新鲜度与可用量，整理出一份“当前可用食材清单”。
            2.智能食谱检索：优先联网查询
            3.多维度评估与排序：从营养价值和制作难度两个维度对检索到的候选食谱进行量化打分，并根据得分排序，制作简单且营养丰富的排名靠前。
            4.结构化方案输出：把排序后的食谱整理为一份结构清晰的建议报告，要包含食谱信息、得分、推荐理由、食谱的参考图片，帮助用户快速做出决策。

            请严格按照流程，优先联网查询，搜索不到的情况下才能自己发挥。
            """;

    /**
     * 千问走 DashScope 的 OpenAI 兼容端点,底下的 OpenAiChatModel 由
     * spring-ai-starter-model-openai 按 spring.ai.openai.* 自动装配,这里只包一层带人设的 ChatClient。
     * bean 名必须是 qwen:AiServiceImpl 是按 @Qualifier("qwen") 注入的。
     */
    @Bean("qwen")
    public ChatClient qwenChatClient(OpenAiChatModel openAiChatModel) {
        return ChatClient.builder(openAiChatModel)
                .defaultSystem(CHEF_SYSTEM)
                .build();
    }
}
