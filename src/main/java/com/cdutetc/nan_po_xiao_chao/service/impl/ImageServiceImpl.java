package com.cdutetc.nan_po_xiao_chao.service.impl;

import cn.hutool.core.io.IORuntimeException;
import cn.hutool.core.io.file.FileReader;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.cdutetc.nan_po_xiao_chao.domain.Result;
import com.cdutetc.nan_po_xiao_chao.domain.po.Menu;
import com.cdutetc.nan_po_xiao_chao.exception.BusinessException;
import com.cdutetc.nan_po_xiao_chao.mapper.MenuMapper;
import com.cdutetc.nan_po_xiao_chao.service.ImageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImageServiceImpl implements ImageService {

    private final MenuMapper menuMapper;

    @Value("${nanpo.image.dir:src/main/resources/image}")
    private String imageDir;

    private final static DateTimeFormatter FORMATTER=DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("png", "jpg", "jpeg", "gif", "webp");

    /**
     * 图片上传
     * @param file
     * @return
     */
    @Override
    public Result<?> imageUpload(Long id,MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(40403, "上传文件不能为空");
        }
        Menu menu = menuMapper.selectById(id);
        if (menu == null) {
            throw new BusinessException(40401, "该菜品不存在");
        }

        //生成图片名称
        log.info("上传图片：{}", file.getOriginalFilename());
        String fileName = generateFileName(file.getOriginalFilename());
        //创建文件目录
        generateDir();
        //保存图片
        Path path = imageRoot().resolve(fileName);
        try {
            file.transferTo(path);
        } catch (IOException e) {
            log.error("图片上传失败：{}", e.getMessage());
            throw new RuntimeException(e);
        }
        //保存到数据库
        save(id, path);
        return Result.success(path.toAbsolutePath().toString());
    }

    /**
     * 图片下载
     * @param id
     * @return
     */
    @Override
    public ResponseEntity<byte[]> downImage(Long id) {
        //查询数据库
        Menu menu = menuMapper.selectById(id);
        if(menu==null||menu.getImage()==null||menu.getImage().isEmpty()){
            log.info("图片不存在 {}", id);
            return ResponseEntity.notFound().build();
        }
        String image = menu.getImage();
        Path path = Path.of(image);
        if(!Files.exists(path)){
            log.info("图片不存在 {}", path);
            return ResponseEntity.notFound().build();
        }
        FileReader fileReader = FileReader.create(new File(path.toAbsolutePath().toUri()));
        try {
            BufferedInputStream inputStream = fileReader.getInputStream();
            byte[] bytes = inputStream.readAllBytes();
            return ResponseEntity.ok(bytes);
        } catch (Exception e) {
            log.error("图片下载失败：{}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 第14课：下载接口。和 /image/{id} 直接看图相比，
     * Content-Disposition: attachment 让浏览器弹"另存为"。
     * normalize 后必须仍位于图片根目录，防 ../ 路径穿越。
     */
    @Override
    public ResponseEntity<Resource> downloadImage(Long id) {
        Menu menu = menuMapper.selectById(id);
        if (menu == null || StrUtil.isEmpty(menu.getImage())) {
            throw new BusinessException(40405, "文件不存在");
        }
        Path root = imageRoot();
        Path path = Path.of(menu.getImage()).toAbsolutePath().normalize();
        if (!path.startsWith(root) || !Files.exists(path)) {
            throw new BusinessException(40405, "文件不存在");
        }
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + path.getFileName() + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(new FileSystemResource(path));
    }

    /**
     * 保存图片
     * @param id
     * @param path
     */
    private void save(Long id, Path path) {
        //查询图片，删除旧图片(只删图片根目录内的,防止误删)
        Menu menu = menuMapper.selectById(id);
        if(menu!=null && StrUtil.isNotEmpty(menu.getImage())){
            Path oldPath = Path.of(menu.getImage()).toAbsolutePath().normalize();
            Path root = imageRoot();
            if (oldPath.startsWith(root)) {
                try {
                    Files.deleteIfExists(oldPath);
                } catch (IOException e) {
                    log.warn("旧图片删除失败：{}", e.getMessage());
                }
            }
        }
        //保存
        try {
            menuMapper.update(
                    new UpdateWrapper<Menu>()
                            .eq("id", id)
                            .set("image", path.toAbsolutePath().toString())
            );
        } catch (Exception e) {
            log.info("图片保存失败：{}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    /**
     * 创建文件目录
     */
    private void generateDir() {
        Path path = Path.of(makeDir());
        if(!Files.exists(path)){
            try {
                Files.createDirectories(path);
            } catch (IOException e) {
                log.error("创建文件目录失败：{}", e.getMessage());
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * 生成图片名称
     * @param originalFilename
     */
    private String generateFileName(@Nullable String originalFilename) {
        if(StrUtil.isEmpty(originalFilename)){
            throw new BusinessException(40403, "文件名不能为空");
        }
        int last = originalFilename.lastIndexOf(".");
        if(last==-1){
            throw new BusinessException(40403, "文件名格式错误");
        }
        String extension = originalFilename.substring(last + 1).toLowerCase(Locale.ROOT);
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new BusinessException(40403, "只支持 jpg/jpeg/png/gif/webp 图片");
        }
        String time = LocalDateTime.now().format(FORMATTER);
        String fileName = time + "/"+UUID.randomUUID()+"." + extension;
        log.info("生成图片名称：{}", fileName);
        return fileName;
    }

    /**
     * 创建文件目录
     * @return
     */
    private String makeDir(){
        return imageRoot().resolve(LocalDateTime.now().format(FORMATTER)).toString();
    }

    /** 图片根目录的绝对路径，上传、下载、静态映射共用 */
    private Path imageRoot() {
        return Path.of(imageDir).toAbsolutePath().normalize();
    }
}
