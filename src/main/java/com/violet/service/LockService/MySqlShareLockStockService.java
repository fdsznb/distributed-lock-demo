package com.violet.service.LockService;

import com.violet.mapper.StockMapper;
import com.violet.pojo.Stock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MySqlShareLockStockService {
    @Autowired
    private StockMapper stockMapper;

    @Transactional
    public void checkAndLock() {
        // 先查询库存是否充足
        Stock stock = this.stockMapper.selectStockForUpdate(1L);

        // 再减库存
        // 再减库存
        if (stock != null && stock.getCount() > 0) {
            stock.setCount(stock.getCount() - 1);
            this.stockMapper.updateById(stock);
        }
    }
}