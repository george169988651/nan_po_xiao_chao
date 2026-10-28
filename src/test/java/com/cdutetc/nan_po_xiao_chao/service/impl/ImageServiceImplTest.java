package com.cdutetc.nan_po_xiao_chao.service.impl;

import com.cdutetc.nan_po_xiao_chao.domain.po.Menu;
import com.cdutetc.nan_po_xiao_chao.exception.BusinessException;
import com.cdutetc.nan_po_xiao_chao.mapper.MenuMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ImageServiceImplTest {

    @Mock
    private MenuMapper menuMapper;

    private ImageServiceImpl imageService;

    @BeforeEach
    void setUp() {
        imageService = new ImageServiceImpl(menuMapper);
    }

    // ========== generateFileName 测试 ==========

    private String invokeGenerateFileName(String originalFilename) throws Exception {
        Method method = ImageServiceImpl.class.getDeclaredMethod("generateFileName", String.class);
        method.setAccessible(true);
        try {
            return (String) method.invoke(imageService, originalFilename);
        } catch (InvocationTargetException e) {
            throw (Exception) e.getCause();
        }
    }

    @Test
    @DisplayName("generateFileName - 正常文件名，返回日期目录 + UUID + 原扩展名")
    void generateFileName_normalFile_returnsUuidWithExtension() throws Exception {
        String result = invokeGenerateFileName("test.png");

        assertNotNull(result);
        assertTrue(result.endsWith(".png"), "文件名应以 .png 结尾");
        assertTrue(result.matches("\\d{4}-\\d{2}-\\d{2}/[0-9a-fA-F-]+\\.png"),
                "格式应为 yyyy-MM-dd/uuid.png，实际: " + result);
    }

    @Test
    @DisplayName("generateFileName - 带路径的文件名，正确提取扩展名")
    void generateFileName_fileWithMultipleDots_extractsLastExtension() throws Exception {
        String result = invokeGenerateFileName("my.photo.file.jpg");

        assertTrue(result.endsWith(".jpg"));
    }

    @Test
    @DisplayName("generateFileName - 文件名为空，抛出 BusinessException")
    void generateFileName_emptyName_throwsException() {
        assertThrows(BusinessException.class, () -> invokeGenerateFileName(""));
    }

    @Test
    @DisplayName("generateFileName - 文件名为 null，抛出 BusinessException")
    void generateFileName_nullName_throwsException() {
        assertThrows(BusinessException.class, () -> invokeGenerateFileName(null));
    }

    @Test
    @DisplayName("generateFileName - 无扩展名，抛出 BusinessException")
    void generateFileName_noExtension_throwsException() {
        assertThrows(BusinessException.class, () -> invokeGenerateFileName("noextension"));
    }

    @Test
    @DisplayName("generateFileName - 非图片扩展名，抛出 BusinessException")
    void generateFileName_nonImageExtension_throwsException() {
        assertThrows(BusinessException.class, () -> invokeGenerateFileName("note.txt"));
    }

    // ========== imageUpload 测试 ==========

    @Test
    @DisplayName("imageUpload - 正常文件上传，不抛异常")
    void imageUpload_validFile_doesNotThrow(@TempDir Path tempDir) throws Exception {
        Menu menu = new Menu("测试菜", new BigDecimal("1.00"));
        menu.setId(1L);
        when(menuMapper.selectById(1L)).thenReturn(menu);

        // imageDir 由 Spring 的 @Value 注入，单测里用临时目录顶上
        Field field = ImageServiceImpl.class.getDeclaredField("imageDir");
        field.setAccessible(true);
        field.set(imageService, tempDir.toString());

        MockMultipartFile file = new MockMultipartFile(
                "file", "dish.png", "image/png", "fake image data".getBytes());

        assertDoesNotThrow(() -> imageService.imageUpload(1L, file));
    }
}
