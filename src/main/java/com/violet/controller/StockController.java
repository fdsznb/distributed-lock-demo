package com.violet.controller;

import com.violet.service.LockService.RedissonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class StockController {

    @Autowired
    private RedissonService stockService;

    @GetMapping("check/lock")
    public String checkAndLock() {
        this.stockService.checkAndLock();
        return "验库存并锁库存成功！";
    }
}