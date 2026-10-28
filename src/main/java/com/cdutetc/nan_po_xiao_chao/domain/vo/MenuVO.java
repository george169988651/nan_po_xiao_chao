package com.cdutetc.nan_po_xiao_chao.domain.vo;


import com.cdutetc.nan_po_xiao_chao.domain.po.Menu;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 菜单 VO（第14课新增）。
 * 旧 /menu 返回 Map{菜名:价格}——加了 imageUrl 之后 Map 装不下了，
 * 第10课的 VO 思想再次救场：出参按前端需要定义形状。
 */
@Data
public class MenuVO {

    private Long id;
    private String dishName;
    private BigDecimal price;
    private String imageUrl;

    public static MenuVO from(Menu menu) {
        MenuVO vo = new MenuVO();
        vo.setId(menu.getId());
        vo.setDishName(menu.getName());
        vo.setPrice(menu.getPrice());
        return vo;
    }
}
