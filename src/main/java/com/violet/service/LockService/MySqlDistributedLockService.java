package com.violet.service.LockService;

import com.violet.mapper.LockMapper;
import com.violet.mapper.StockMapper;
import com.violet.pojo.Lock;
import com.violet.pojo.Stock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class MySqlDistributedLockService {
    @Autowired
    private StockMapper stockMapper;

    @Autowired
    private LockMapper lockMapper;

    /*** 数据库分布式锁 */
    public void checkAndLock() {
        // 加锁
        Lock lock = new Lock(null, "lock", this.getClass().getName(), new Date(), null);

        try {
            this.lockMapper.insert(lock);
        } catch (Exception ex) {
            try {
                Thread.sleep(50);
                this.checkAndLock();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // 先查询库存是否充足
        Stock stock = this.stockMapper.selectById(1L);

        // 再减库存
        if (stock != null && stock.getCount() > 0) {
            stock.setCount(stock.getCount() - 1);
            this.stockMapper.updateById(stock);
        }

        // 释放锁
        this.lockMapper.deleteById(lock.getId());
    }

}