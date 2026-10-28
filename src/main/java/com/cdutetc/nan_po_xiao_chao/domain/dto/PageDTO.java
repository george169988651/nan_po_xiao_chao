package com.cdutetc.nan_po_xiao_chao.domain.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PageDTO {

    @NotNull(message = "当前页不能为空")
    private Long currentPage;

    @NotNull(message = "分页大小不能为空")
    private Long pageSize;

}
