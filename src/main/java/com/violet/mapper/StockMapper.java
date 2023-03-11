package com.violet.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.violet.pojo.Stock;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface StockMapper extends BaseMapper<Stock> {
    Stock selectStockForUpdate(long l);
}