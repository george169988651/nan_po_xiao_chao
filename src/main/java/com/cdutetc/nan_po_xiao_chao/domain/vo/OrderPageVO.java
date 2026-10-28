package com.cdutetc.nan_po_xiao_chao.domain.vo;

import lombok.Data;

import java.util.List;

/**
 * 分页小票 VO（出参）：翻账本的一页。
 * 第12课新增：字段对应 Spring Data 的 Page 概念——
 * records(本页数据)/totalElements(总条数)/totalPages(总页数)/pageNum(页号，从0起)/pageSize。
 * 注意 VO 是纯数据类，不 import 框架类型——Page 的组装转换留在 Service 层。
 */
@Data
public class OrderPageVO {

    private List<OrderVO> records;
    private Long totalElements;
    private Integer totalPages;
    private Integer pageNum;
    private Integer pageSize;
}
