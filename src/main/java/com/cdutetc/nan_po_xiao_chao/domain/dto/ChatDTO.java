package com.cdutetc.nan_po_xiao_chao.domain.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
@Builder
public class ChatDTO {

    @NotNull(message = "用户ID不能为空")
    public String userId;

    @NotNull(message = "图片不能为空")
    public List<MultipartFile> image;

    @NotNull(message = "消息不能为空")
    public String message;
}
