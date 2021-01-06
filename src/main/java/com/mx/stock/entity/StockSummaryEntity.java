package com.mx.stock.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.frameworkset.orm.annotation.ESRouting;
import lombok.Data;

import java.util.Date;

@Data
public class StockSummaryEntity extends StockBaseEntity {

    @ESRouting
    private String routing;
    /*时间*/
    private String day;
    /*时间戳*/
    @JsonProperty("day_stamp")
    private Long dayStamp;
    /*收盘价*/
    @JsonProperty("tc_close")
    private Double tClose;
    /*最高价*/
    private Double high;
    /*最低价*/
    private Double low;
    /*开盘价*/
    @JsonProperty("t_open")
    private Double tOpen;
    /*前收盘价*/
    @JsonProperty("l_close")
    private Double lClose;
    /*涨跌额*/
    private Double chg;
    /*涨跌幅*/
    private Double pchg;
    /*换手率*/
    @JsonProperty("turn_over")
    private Double turnOver;
    /*成交量*/
    @JsonProperty("vo_turn_over")
    private Long voTurnOver;
    /*成交金额*/
    @JsonProperty("va_turn_over")
    private Long vaTurnOver;
    /*总市值*/
    @JsonProperty("t_cap")
    private Long tCap;
    /*流通市值*/
    @JsonProperty("m_cap")
    private Long mCap;
    /*创建时间*/
    private Date createTime = new Date();
}
