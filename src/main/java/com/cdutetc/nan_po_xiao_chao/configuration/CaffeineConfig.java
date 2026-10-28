package com.cdutetc.nan_po_xiao_chao.configuration;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * Caffeine 本地缓存配置：菜单等读多写少的数据走进程内缓存
 */
@Configuration
@EnableCaching
public class CaffeineConfig {

    /**
     * 缓存名常量，供 @Cacheable("...") 引用，避免字符串散落
     */
    public static final String CACHE_MENU = "menu";

    @Bean
    public CaffeineCacheManager caffeineCacheManager() {
        CaffeineCacheManager manager = new CaffeineCacheManager(CACHE_MENU);
        manager.setCaffeine(Caffeine.newBuilder()
                .maximumSize(500)
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .expireAfterAccess(10, TimeUnit.MINUTES)
                .recordStats());
        manager.setAllowNullValues(false);/// 禁止缓存 null
        return manager;
    }
}
