package com.cdutetc.nan_po_xiao_chao.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;

/**
 * 第14课：把 /files/** 映射到本地图片目录，浏览器可以直接按路径访问照片。
 * 目录型 resource location 必须以 / 结尾，否则映射静默失效。
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${nanpo.image.dir:src/main/resources/image}")
    private String imageDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String location = Path.of(imageDir).toAbsolutePath().normalize().toUri().toString();
        if (!location.endsWith("/")) {
            location += "/";
        }
        registry.addResourceHandler("/files/**")
                .addResourceLocations(location);
    }
}
