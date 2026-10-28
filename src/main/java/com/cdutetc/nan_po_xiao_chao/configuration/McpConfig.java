package com.cdutetc.nan_po_xiao_chao.configuration;

import com.cdutetc.nan_po_xiao_chao.tool.MenuTool;
import com.cdutetc.nan_po_xiao_chao.tool.OrderTool;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 第20课：把本店工具注册到 MCP Server。
 * 注册后任何支持 MCP 的客户端都能通过 SSE 端点查菜单、下单。
 */
@Configuration
public class McpConfig {

    @Bean
    public ToolCallbackProvider nanpoTools(MenuTool menuTool, OrderTool orderTool) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(menuTool, orderTool)
                .build();
    }
}
