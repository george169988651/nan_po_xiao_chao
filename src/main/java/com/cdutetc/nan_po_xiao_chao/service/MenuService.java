package com.cdutetc.nan_po_xiao_chao.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cdutetc.nan_po_xiao_chao.domain.Result;
import com.cdutetc.nan_po_xiao_chao.domain.dto.OrderDTO;
import com.cdutetc.nan_po_xiao_chao.domain.po.Menu;

public interface MenuService extends IService<Menu> {

    Result queryMenu();

    Result order(OrderDTO orderDTO);

}
