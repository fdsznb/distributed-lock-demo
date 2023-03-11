package com.violet.utils;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public class RedisDistributedLock {


    private StringRedisTemplate redisTemplate;
    // 线程局部变量，可以在线程内共享参数
    private String lockName;
    private String uuid;

    private Integer expire = 30;
    private static final ThreadLocal<String> THREAD_LOCAL = new
            ThreadLocal<>();

    public RedisDistributedLock(StringRedisTemplate redisTemplate, String lockName) {
        this.redisTemplate = redisTemplate;
        this.lockName = lockName;
        this.uuid = THREAD_LOCAL.get();
        if (StringUtils.isBlank(uuid)) {
            this.uuid = UUID.randomUUID().toString();
            THREAD_LOCAL.set(uuid);
        }
    }

    public void lock() {
        this.lock(expire);
    }

    public void lock(Integer expire) {
        this.expire = expire;
        String script = "if (redis.call('exists', KEYS[1]) == 0 or " +
                "redis.call('hexists', KEYS[1], ARGV[1]) == 1) " +
                "then" +
                " redis.call('hincrby', KEYS[1], ARGV[1], 1);" +
                " redis.call('expire', KEYS[1], ARGV[2]);" +
                " return 1;" +
                "else" +
                " return 0;" +
                "end";
        if (!this.redisTemplate.execute(new DefaultRedisScript<>(script,
                Boolean.class), Arrays.asList(lockName), uuid, expire.toString())) {
            try {
                // 没有获取到锁，重试
                Thread.sleep(60);
                lock(expire);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        renewExpire();
    }

    public void unlock() {
        String script = "if(redis.call('hexists', KEYS[1], ARGV[1]) == 0) " +
                "then " +
                " return nil; " +
                "elseif(redis.call('hincrby', KEYS[1], ARGV[1], -1) > 0) " +
                "then " +
                " return 0; " +
                "else " +
                " redis.call('del', KEYS[1]); " +
                " return 1; " +
                "end;";

        // 如果返回值没有使用Boolean，Spring-data-redis 进行类型转换时将会把 null 转为 false，这就会影响我们逻辑判断
        // 所以返回类型只好使用 Long：null-解锁失败；0-重入次数减1；1-解锁成功。
        Long result = this.redisTemplate.execute(new DefaultRedisScript<>
                (script, Long.class), Arrays.asList(lockName), uuid);

        // 如果未返回值，代表尝试解其他线程的锁
        if (result == null) {
            throw new IllegalMonitorStateException("attempt to unlock lock," +
                    "not locked by lockName: "
                    + lockName + " with request: " + uuid);
        } else if (result == 1) {
            THREAD_LOCAL.remove();
        }
        this.uuid = null;
    }

    private static final Timer TIMER = new Timer();

    /**
     * 开启定时器，自动续期
     */
    private void renewExpire() {
        String script = "if(redis.call('hexists', KEYS[1], ARGV[1]) == 1) then " +
                "redis.call('expire', KEYS[1], ARGV[2]); return 1; else return 0; end";
        TIMER.schedule(new TimerTask() {
            @Override
            public void run() {
                // 如果uuid为空，则终止定时任务
                if (StringUtils.isNotBlank(uuid)) {
                    redisTemplate.execute(new DefaultRedisScript<>(script,
                                    Boolean.class), Arrays.asList(lockName), RedisDistributedLock.this.uuid,
                            expire.toString());
                    renewExpire();
                }
            }
        }, expire * 1000 / 3);
    }
}
