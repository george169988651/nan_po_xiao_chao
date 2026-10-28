package com.cdutetc.nan_po_xiao_chao.controller;

import com.cdutetc.nan_po_xiao_chao.domain.Result;
import com.cdutetc.nan_po_xiao_chao.service.ImageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/image")
@RequiredArgsConstructor
@Slf4j
public class ImageController {

    private final ImageService imageService;

    @PostMapping("/{id}/upload")
    public Result<?> imageUpload(
            @PathVariable("id") Long id,
            @RequestParam("file")MultipartFile file
    ){
        log.info("file size: {}", file.getSize());
        return imageService.imageUpload(id,file);
    }

    @GetMapping("/{id}")
    public ResponseEntity<byte[]> downImage(@PathVariable("id") Long id){
        return imageService.downImage(id);
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadImage(@PathVariable("id") Long id){
        return imageService.downloadImage(id);
    }
}
