package com.cdutetc.nan_po_xiao_chao.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cdutetc.nan_po_xiao_chao.domain.Result;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

public interface ImageService {

    /**
     * 图片上传
     * @param file
     * @return
     */
    Result<?> imageUpload(Long id,MultipartFile file);


    /**
     * 图片下载
     * @param id
     * @return
     */
    ResponseEntity<byte[]> downImage(Long id);

    /**
     * 第14课：以附件形式下载，浏览器弹"另存为"
     */
    ResponseEntity<Resource> downloadImage(Long id);
}
