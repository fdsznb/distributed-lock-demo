package com.violet.pojo;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("db_lock")
public class Lock {
    private Long id;
    private String lockName;
    private String className;
    private String methodName;
    private String serverName;
    private String threadName;
    private Date createTime;
    private String desc;

    public Lock (Long id, String lockName, String className, Date createTime, String desc) {
        this.id = id;
        this.lockName = lockName;
        this.className = className;
        this.createTime = createTime;
        this.desc = desc;
    }
}