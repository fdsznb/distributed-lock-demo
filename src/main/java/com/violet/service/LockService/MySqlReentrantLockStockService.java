package com.violet.service.LockService;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.violet.mapper.StockMapper;
import com.violet.pojo.Stock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MySqlReentrantLockStockService {
    @Autowired
    private StockMapper stockMapper;

    @Transactional
    public void checkAndLock() {
        // 先查询库存是否充足
        Stock stock = this.stockMapper.selectById(1L);

        // 再减库存
        if (stock != null && stock.getCount() > 0) {
            // 获取版本号
            Long version = stock.getVersion();
            stock.setCount(stock.getCount() - 1);
            // 每次更新 版本号 + 1
            stock.setVersion(stock.getVersion() + 1);
            // 更新之前先判断是否是之前查询的那个版本，如果不是重试
            if (this.stockMapper.update(stock, new UpdateWrapper<Stock>()
                    .eq("id", stock.getId()).eq("version", version)) == 0) {
                checkAndLock();
            }
        }
    }
}