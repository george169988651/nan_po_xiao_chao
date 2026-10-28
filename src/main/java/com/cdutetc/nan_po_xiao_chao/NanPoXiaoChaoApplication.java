package com.cdutetc.nan_po_xiao_chao;

import com.cdutetc.nan_po_xiao_chao.mapper.MenuMapper;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.ai.model.deepseek.autoconfigure.DeepSeekChatAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication(exclude = DeepSeekChatAutoConfiguration.class)
@Slf4j
@EnableConfigurationProperties
@MapperScan(basePackageClasses = {MenuMapper.class})
public class NanPoXiaoChaoApplication {

    public static void main(String[] args) {

        ConfigurableApplicationContext applicationContext = SpringApplication.run(NanPoXiaoChaoApplication.class, args);

        log.info("=======================================");

        String name = applicationContext.getEnvironment().getProperty("spring.application.name");

        String port = applicationContext.getEnvironment().getProperty("server.port");

        log.info("{} http://localhost:{} \n h2数据库：http://localhost:{}/h2-console",name,port,port);

        log.info("=======================================");

    }

}
