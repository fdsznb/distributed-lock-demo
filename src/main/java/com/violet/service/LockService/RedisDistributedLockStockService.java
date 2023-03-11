package com.violet.service.LockService;

import com.violet.mapper.LockMapper;
import com.violet.mapper.StockMapper;
import com.violet.pojo.Stock;
import com.violet.utils.RedisDistributedLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class RedisDistributedLockStockService {
    @Autowired
    private StockMapper stockMapper;
    @Autowired
    private LockMapper lockMapper;
    @Autowired
    private StringRedisTemplate redisTemplate;

    public void checkAndLock() {
        // 加锁，获取锁失败重试
//        String uuid = UUID.randomUUID().toString();
//        while (!this.redisTemplate.opsForValue().setIfAbsent("lock", uuid, 3, TimeUnit.SECONDS)) {
//            try {
//                Thread.sleep(10);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }

        // 加锁
        RedisDistributedLock lock = new RedisDistributedLock(this.redisTemplate, "lock");
        lock.lock();


        // 先查询库存是否充足
        Stock stock = this.stockMapper.selectById(1L);
        // 再减库存
        if (stock != null && stock.getCount() > 0) {
            stock.setCount(stock.getCount() - 1);
            this.stockMapper.updateById(stock);
        }

        // 测试可重入
        testSubLock();




        // 释放锁
//        this.redisTemplate.delete("lock");

        // 释放锁
//        String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return " +
//                "redis.call('del', KEYS[1]) else return 0 end";
//        this.redisTemplate.execute(new DefaultRedisScript<>(script,
//                Long.class), Arrays.asList("lock"), uuid);

        // 释放锁
        lock.unlock();
    }


    public void testSubLock() {
        RedisDistributedLock lock = new RedisDistributedLock(this.redisTemplate, "lock");

        lock.lock();
        System.out.println("测试可重入。");
        lock.unlock();

    }
}
