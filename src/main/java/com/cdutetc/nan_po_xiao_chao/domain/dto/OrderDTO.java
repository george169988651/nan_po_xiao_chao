package com.cdutetc.nan_po_xiao_chao.domain.dto;

import lombok.Data;

import java.util.List;

@Data
public class OrderDTO {

    private List<DishCount> dishList;

    @Data
    public static class DishCount {
        private String dishName;
        private Integer count;
    }
}
