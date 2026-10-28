package com.cdutetc.nan_po_xiao_chao.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cdutetc.nan_po_xiao_chao.domain.PageResult;
import com.cdutetc.nan_po_xiao_chao.domain.dto.PageDTO;
import com.cdutetc.nan_po_xiao_chao.domain.po.Menu;
import com.cdutetc.nan_po_xiao_chao.domain.po.Order;
import com.cdutetc.nan_po_xiao_chao.domain.vo.OrderItemVO;
import com.cdutetc.nan_po_xiao_chao.domain.vo.OrderPageVO;
import com.cdutetc.nan_po_xiao_chao.domain.vo.OrderVO;
import com.cdutetc.nan_po_xiao_chao.exception.BusinessException;
import com.cdutetc.nan_po_xiao_chao.mapper.MenuMapper;
import com.cdutetc.nan_po_xiao_chao.mapper.OrderMapper;
import com.cdutetc.nan_po_xiao_chao.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {

    private final MenuMapper menuMapper;

    /**
     * 分页查询订单接口
     * @param pageDTO 分页参数
     * @return
     */
    @Override
    public PageResult queryOrderPage(PageDTO pageDTO) {

        Page<Order> orderPage = new Page<>(pageDTO.getCurrentPage(), pageDTO.getPageSize());

        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<Order>().orderByDesc(Order::getCreateTime);

        Page<Order> page = getBaseMapper().selectPage(orderPage, wrapper);

        log.info("page:{}",page.getRecords());

        return PageResult.success(page.getRecords(),page.getTotal());
    }

    /**
     * 保存订单
     * @param orders
     */
    @Override
    public void saveOrder(List<Order> orders) {
        if(orders==null){
            return ;
        }
        log.debug("orders:{}",orders);
        saveBatch(orders);
    }

    @Override
    public OrderPageVO getOrderPage(int page, int size) {
        if (page < 0) {
            page = 0;
        }
        if (size <= 0) {
            size = 10;
        }
        Page<Order> query = new Page<>(page + 1L, size);
        Page<Order> result = getBaseMapper().selectPage(query,
                new LambdaQueryWrapper<Order>().orderByDesc(Order::getCreateTime));

        OrderPageVO vo = new OrderPageVO();
        vo.setRecords(result.getRecords().stream().map(this::toVO).toList());
        vo.setTotalElements(result.getTotal());
        vo.setTotalPages((int) result.getPages());
        vo.setPageNum(page);
        vo.setPageSize(size);
        return vo;
    }

    @Override
    public OrderVO getOrder(Long id) {
        Order order = getById(id);
        if (order == null) {
            throw new BusinessException(40402, "该订单不存在");
        }
        return toVO(order);
    }

    @Override
    public List<OrderVO> searchByDish(String dishName) {
        if (dishName == null || dishName.isBlank()) {
            throw new BusinessException(400, "菜名不能为空");
        }
        Menu menu = menuMapper.selectOne(new LambdaQueryWrapper<Menu>().eq(Menu::getName, dishName));
        if (menu == null) {
            throw new BusinessException(40401, "该菜品不存在");
        }
        return list(new LambdaQueryWrapper<Order>().orderByDesc(Order::getCreateTime)).stream()
                .filter(order -> order.getOrderList() != null
                        && order.getOrderList().contains(menu.getId()))
                .map(this::toVO)
                .toList();
    }

    /**
     * 账本 → 小票。order_list 里同一菜单 id 出现几次就代表点了几份,
     * 所以先统计次数,再批量查菜单补菜名和单价。
     */
    private OrderVO toVO(Order order) {
        Map<Long, Integer> counts = new LinkedHashMap<>();
        if (order.getOrderList() != null) {
            for (Long menuId : order.getOrderList()) {
                if (menuId != null) {
                    counts.merge(menuId, 1, Integer::sum);
                }
            }
        }

        List<OrderItemVO> items = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        if (!counts.isEmpty()) {
            Map<Long, Menu> menuMap = menuMapper.selectBatchIds(counts.keySet()).stream()
                    .collect(Collectors.toMap(Menu::getId, menu -> menu, (a, b) -> a));

            for (Map.Entry<Long, Integer> entry : counts.entrySet()) {
                Menu menu = menuMap.get(entry.getKey());
                if (menu == null) {
                    continue;
                }
                int count = entry.getValue();
                BigDecimal unitPrice = menu.getPrice();
                BigDecimal subTotal = unitPrice.multiply(BigDecimal.valueOf(count));
                OrderItemVO item = OrderItemVO.of(menu.getName(), count, subTotal);
                item.setUnitPrice(unitPrice);
                items.add(item);
                total = total.add(subTotal);
            }
        }

        return OrderVO.of(order.getId(), order.getUserId(), order.getUserName(), items, total,
                order.getMessage(), total, order.getCreateTime());
    }
}
