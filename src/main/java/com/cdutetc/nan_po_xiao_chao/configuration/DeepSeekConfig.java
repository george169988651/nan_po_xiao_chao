package com.cdutetc.nan_po_xiao_chao.configuration;

import com.cdutetc.nan_po_xiao_chao.constant.AiConstant;
import com.cdutetc.nan_po_xiao_chao.history.JdbcChatHistory;
import com.cdutetc.nan_po_xiao_chao.tool.AiTool;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Result;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.deepseek.DeepSeekChatModel;
import org.springframework.ai.deepseek.DeepSeekChatOptions;
import org.springframework.ai.deepseek.api.DeepSeekApi;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.function.FunctionToolCallback;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.http.client.ClientHttpRequestFactoryBuilder;
import org.springframework.boot.http.client.HttpClientSettings;
import org.springframework.boot.restclient.autoconfigure.RestClientBuilderConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class DeepSeekConfig implements InitializingBean {

    private final AiProperties aiProperties;

    private final JdbcChatHistory jdbcChatHistory;

    private final AiTool aiTool;

    private final ToolCallbackProvider toolCallbackProvider;

    @Resource(name="orderFunctionToolCallback")
    private FunctionToolCallback orderFunctionToolCallback;

    @Resource(name="menuFunctionToolCallback")
    private FunctionToolCallback menuFunctionToolCallback;

    @Bean
    public DeepSeekApi deepSeekApi() {
        // 手动创建带超时的 RestClient.Builder
        RestClient.Builder restClientBuilder = RestClient.builder()
                .baseUrl(aiProperties.getBase_url())
                .requestFactory(
                        ClientHttpRequestFactoryBuilder
                                .simple()
                                .build(
                                        HttpClientSettings
                                                .defaults()
                                                .withTimeouts(Duration.ofMinutes(5), Duration.ofMinutes(5))// 设置连接超时和读取超时
                                )
                );

        return DeepSeekApi.builder()
                .apiKey(aiProperties.getApi_key())
                .baseUrl(aiProperties.getBase_url())
                .restClientBuilder(restClientBuilder)
                .build();
    }


    @Bean
    public DeepSeekChatModel deepSeekChatModel(DeepSeekApi deepSeekApi){

        DeepSeekChatOptions temperature = new DeepSeekChatOptions.
                Builder().
                model(aiProperties.getModel()).
                maxTokens(10000).
                temperature(0.7).build();

        return DeepSeekChatModel.builder().
                deepSeekApi(deepSeekApi).
                options(temperature).
                build();
    }

    @Bean
    public MessageWindowChatMemory messageWindowChatMemory(){
        return MessageWindowChatMemory.
                builder().
                chatMemoryRepository(jdbcChatHistory).
                maxMessages(10).
                build();
    }

    @Bean
    public MessageChatMemoryAdvisor messageChatMemoryAdvisor(MessageWindowChatMemory messageWindowChatMemory){
        return MessageChatMemoryAdvisor.builder(messageWindowChatMemory).build();
    }

    @Bean(value = "user")
    public ChatClient chatClient(DeepSeekChatModel deepSeekChatModel,MessageChatMemoryAdvisor messageChatMemoryAdvisor){

        return ChatClient.
                builder(deepSeekChatModel).
                defaultAdvisors(messageChatMemoryAdvisor)
                .defaultTools(
                        aiTool
                )
                .defaultSystem(AiConstant.DEFAULT_SYSTEM)
                .build();
    }

    @Bean(value = "router")
    public ChatClient chatModel(DeepSeekChatModel deepSeekChatModel){
        return ChatClient.builder(deepSeekChatModel)
                .defaultSystem(AiConstant.ROUTER)
                .build();
    }

    /**
     * 第18课结构化输出:专职把自然语言点单解析成 OrderDTO,
     * 不挂工具、不带记忆,避免它顺手查菜单而不是老实出 JSON。
     */
    @Bean(value = "orderParser")
    public ChatClient orderParser(DeepSeekChatModel deepSeekChatModel){
        return ChatClient.builder(deepSeekChatModel)
                .defaultSystem(AiConstant.ORDER_PARSER)
                .build();
    }


    @Override
    public void afterPropertiesSet() throws Exception {
        log.info("=================================\n");

        log.info("DeepSeek 初始化完成");

        log.info("=================================\n");
    }
}
