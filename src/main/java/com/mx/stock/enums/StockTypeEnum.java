package com.mx.stock.enums;

public enum StockTypeEnum {

    SZ("sz", "深证"), SH("sh", "上证"), UNKNOW("un", "未知");
    String code;
    String desc;

    StockTypeEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static StockTypeEnum getStockType(String code) {
        if(code == null || code.length() != 6) {
            return UNKNOW;
        } else if(code.startsWith("0") || code.startsWith("3")) {
            return SZ;
        } else if(code.startsWith("6")) {
            return SH;
        } else {
            return UNKNOW;
        }

    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
