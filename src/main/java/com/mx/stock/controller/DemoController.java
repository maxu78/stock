package com.mx.stock.controller;

import com.mx.stock.job.StockSpider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DemoController {

    @Autowired
    StockSpider stockSpider;

    @GetMapping("/history/execute")
    public void execute() {
        stockSpider.executeHistory();
    }
}
