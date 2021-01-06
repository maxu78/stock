package com.mx.stock.entity;

import lombok.Data;

@Data
public class StockBaseEntity {


    /*股票代码*/
    private String code;
    /*股票名称*/
    private String name;
    /*股票类型(上交所还是深交所)*/
    private String type;
}
