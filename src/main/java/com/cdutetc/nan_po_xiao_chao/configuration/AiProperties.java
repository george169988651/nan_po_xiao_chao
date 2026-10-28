package com.cdutetc.nan_po_xiao_chao.configuration;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Slf4j
@Component
@Data
public class AiProperties implements InitializingBean {

    @Value("${spring.ai.deepseek.api-key}")
    private String api_key;

    @Value("${spring.ai.deepseek.base-url}")
    private String base_url;

    @Value("${spring.ai.deepseek.chat.model}")
    private String model;

    @Override
    public void afterPropertiesSet() throws Exception {
        log.info("{},{},{}", api_key, base_url, model);
    }
}
