package com.cdutetc.nan_po_xiao_chao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cdutetc.nan_po_xiao_chao.domain.po.ChatMemory;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface JdbcChatMemoryMapper extends BaseMapper<ChatMemory> {
}
