package com.cdutetc.nan_po_xiao_chao.configuration;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Data
public class OssConfig  {

    @Value("${aliyun.oss.access-key-id}")
    private String accessKeyId;

    @Value("${aliyun.oss.access-key-secret}")
    private String accessKeySecret;

    @Value("${aliyun.oss.endpoint}")
    private String endpoint;

    @Value("${aliyun.oss.bucket-name}")
    private String bucketName;

    public static String OSS_BUCKET_NAME;

    public static String OSS_ENDPOINT;

    @PostConstruct
    void init(){
        OSS_BUCKET_NAME = bucketName;
        OSS_ENDPOINT = endpoint;
    }


    /**
     *  1. 返回接口 OSS 而非实现类 OSSClient（面向接口编程）
     *  2. 使用 OSSClientBuilder 替代已废弃的 new OSSClient()
     *  3. Bean 销毁时自动关闭客户端，防止连接/线程泄漏
     */
    @Bean(destroyMethod = "shutdown")
    public OSS ossClient() {
        return new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
    }

}
