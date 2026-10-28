package com.cdutetc.nan_po_xiao_chao.configuration;

import com.cdutetc.nan_po_xiao_chao.tool.AiTool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaChatOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
@Slf4j
@RequiredArgsConstructor
public class OllamaConfig {

    private final AiTool aiTool;

    @Bean
    public OllamaApi ollamaApi( @Value("${spring.ai.ollama.base-url}") String base_url){
        return OllamaApi.builder()
                .baseUrl(base_url)
                .build();
    }

    @Bean
    public OllamaChatOptions ollamaChatOptions(@Value("${spring.ai.ollama.chat.model}") String model){
        return OllamaChatOptions.builder()
                .model(model)
                .maxTokens(1000)
                .enableThinking()
                .build();
    }

    @Bean
    public OllamaChatModel ollamaChatModel(OllamaApi ollamaApi,OllamaChatOptions ollamaChatOptions){
        return OllamaChatModel.builder()
                .ollamaApi(ollamaApi)
                .options(ollamaChatOptions)
                .build();
    }

    @Bean(value = "Ollama")
    public ChatClient chatClient(OllamaChatModel ollamaChatModel){
        return ChatClient.builder(ollamaChatModel)
                .defaultSystem("You are a helpful assistant powered by Ollama.")
                .defaultTools(aiTool)
                .build();
    }
}
