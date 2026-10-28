package com.cdutetc.nan_po_xiao_chao.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.Pair;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cdutetc.nan_po_xiao_chao.configuration.CaffeineConfig;
import com.cdutetc.nan_po_xiao_chao.context.UserContext;
import com.cdutetc.nan_po_xiao_chao.domain.Result;
import com.cdutetc.nan_po_xiao_chao.domain.dto.OrderDTO;
import com.cdutetc.nan_po_xiao_chao.domain.po.Menu;
import com.cdutetc.nan_po_xiao_chao.domain.po.Order;
import com.cdutetc.nan_po_xiao_chao.domain.vo.OrderItemVO;
import com.cdutetc.nan_po_xiao_chao.domain.vo.OrderVO;
import com.cdutetc.nan_po_xiao_chao.exception.BusinessException;
import com.cdutetc.nan_po_xiao_chao.mapper.MenuMapper;
import com.cdutetc.nan_po_xiao_chao.mapper.OrderMapper;
import com.cdutetc.nan_po_xiao_chao.service.MenuService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cglib.core.Local;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MenuServiceImpl extends ServiceImpl<MenuMapper, Menu> implements MenuService {

    private final OrderMapper orderMapper;

    /**
     * 查询菜单接口
     * @return
     */
    @Override
    @Cacheable(value = CaffeineConfig.CACHE_MENU, key = "'Menu'")
    public Result queryMenu() {
        List<Menu> list = lambdaQuery().list();
        log.info("{}",list);
        return Result.success(list);
    }

    /**
     * 下单接口
     * @param orderDTO 订单
     * @return
     */
    @Transactional()
    @Override
    public Result order(OrderDTO orderDTO) {
        if (orderDTO == null || orderDTO.getDishList() == null || orderDTO.getDishList().isEmpty()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST.value(), "下单接口异常,订单列表为空");
        }
        List<OrderDTO.DishCount> dishList = orderDTO.getDishList();

        //2.检验菜单
        List<String> dishNameList = dishList.stream()
                .map(OrderDTO.DishCount::getDishName)
                .filter(Objects::nonNull)
                .toList();

        //3 查询菜单单价，创建Map映射
        Map<String, Menu> dishMap = lambdaQuery().
                in(Menu::getName,dishNameList).
                list().stream().
                collect(Collectors.toMap(menu -> menu.getName(), menu -> menu));

        //4.计算价格,计算订单详细
        BigDecimal totalPrice = new BigDecimal(0);
        ArrayList<Long> menuIdList = new ArrayList<>(dishMap.size());
        ArrayList<OrderItemVO> vos = new ArrayList<>(dishMap.size());
        //用普通 for 循环,lambda 里没法给 totalPrice 重新赋值(要求 effectively final)
        for (OrderDTO.DishCount item : dishList) {
            if (item == null || item.getDishName() == null || item.getDishName().isBlank()) {
                continue;
            }
            Menu menu = dishMap.get(item.getDishName());
            if (menu == null) {
                throw new BusinessException(40401, "该菜品不存在: " + item.getDishName());
            }
            if (item.getCount() == null || item.getCount() < 0) {
                throw new BusinessException(HttpStatus.BAD_REQUEST.value(), "下单服务，订单数量不能为负数");
            }
            if (item.getCount() == 0) {
                continue;
            }

            BigDecimal price = menu.getPrice().multiply(new BigDecimal(item.getCount()));
            totalPrice = totalPrice.add(price);
            // order_list 用重复 id 表示份数:点 3 份就存 3 个 id
            for (int i = 0; i < item.getCount(); i++) {
                menuIdList.add(menu.getId());
            }
            OrderItemVO itemVO = OrderItemVO.of(menu.getName(), item.getCount(), price);
            itemVO.setUnitPrice(menu.getPrice());
            vos.add(itemVO);
        }

        if (menuIdList.isEmpty()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST.value(), "订单为空,请至少选择一道菜");
        }

        //5.保存订单(下单人取登录态,不能再写死)
        Pair<String, String> currentUser = UserContext.getCurrentUser();
        log.debug("当前登录用户: {}", currentUser);
        Long userId = Long.valueOf(currentUser.getKey());
        String userName = currentUser.getValue();

        Order order = Order.builder().
                orderList(menuIdList).
                userId(userId).
                userName(userName).
                createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();

        log.info("{}",order);

        orderMapper.insert(order);

        //6.返回订单
        return Result.success(
                OrderVO.of(order.getId(), userId, userName, vos, totalPrice,
                        null, totalPrice, order.getCreateTime())
        );
    }

}
