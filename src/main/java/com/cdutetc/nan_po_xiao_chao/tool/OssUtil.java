package com.cdutetc.nan_po_xiao_chao.tool;

import com.aliyun.oss.OSS;
import com.aliyun.oss.model.PutObjectResult;
import com.cdutetc.nan_po_xiao_chao.configuration.OssConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class OssUtil {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private final OSS ossClient;

    /**
     * 上传文件到 OSS
     *
     * @param file 待上传的文件
     * @return 文件访问 URI
     * @throws IOException 文件读取或上传失败时抛出
     */
    public String upload(MultipartFile file) throws IOException {
        // 1. 文件基础校验
        if (file == null || file.isEmpty()) {
            log.warn("OSS 上传失败: 传入的文件为空或大小为0");
            throw new IllegalArgumentException("上传文件不能为空");
        }

        String originalFilename = file.getOriginalFilename();
        log.info("开始上传文件到 OSS, 原始文件名: {}, 大小: {} bytes", originalFilename, file.getSize());

        // 2. 生成对象 Key
        String objectKey = createObjectKey(originalFilename);

        try {
            // 3. 执行上传（使用 objectKey 而非 originalFilename）
            PutObjectResult result = ossClient.putObject(
                    OssConfig.OSS_BUCKET_NAME,
                    objectKey,
                    file.getInputStream()
            );

            // SDK 未必回填 response,拿不到时按标准虚拟主机域名自己拼
            String uri = result.getResponse() != null
                    ? result.getResponse().getUri()
                    : "https://" + OssConfig.OSS_BUCKET_NAME + "." + OssConfig.OSS_ENDPOINT + "/" + objectKey;
            log.info("OSS 上传成功, objectKey: {}, URI: {}", objectKey, uri);
            return uri;

        } catch (Exception e) {
            log.error("OSS 上传异常, objectKey: {}, 原始文件名: {}", objectKey, originalFilename, e);
            throw new IOException("文件上传至 OSS 失败: " + e.getMessage(), e);
        }
    }

    /**
     * 生成 OSS 对象 Key（存储路径）
     * 格式: 2026-10-16/uuid.ext
     */
    private String createObjectKey(String originalFilename) {
        String datePath = LocalDate.now().format(DATE_FORMATTER);
        String uuid = UUID.randomUUID().toString().replace("-", "");

        // 安全提取后缀
        String suffix = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            int lastDot = originalFilename.lastIndexOf('.');
            suffix = originalFilename.substring(lastDot).toLowerCase();
        } else {
            log.warn("文件无扩展名, 将以无后缀形式存储, 原始文件名: {}", originalFilename);
        }

        return datePath + "/" + uuid + suffix;
    }
}