package com.cdutetc.nan_po_xiao_chao.tool;

import com.cdutetc.nan_po_xiao_chao.domain.po.Menu;
import com.cdutetc.nan_po_xiao_chao.service.MenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 第20课：给 MCP 客户端看的菜单工具。
 * 和 AiTool 里的 getMenu 是同一个数据源，区别是这里不依赖 ToolContext，外部 MCP 客户端也能调。
 */
@Component
@RequiredArgsConstructor
public class MenuTool {

    private final MenuService menuService;

    @Tool(description = "获取本店当前完整菜单（菜名、价格）")
    public List<Menu> getMenu() {
        @SuppressWarnings("unchecked")
        List<Menu> menus = (List<Menu>) menuService.queryMenu().getData();
        return menus == null ? List.of() : menus;
    }
}
