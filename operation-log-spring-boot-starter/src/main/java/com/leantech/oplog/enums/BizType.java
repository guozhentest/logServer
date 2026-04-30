package com.leantech.oplog.enums;

public enum BizType {
    REG("REG", "挂号"),
    PAY("PAY", "缴费"),
    CLI("CLI", "实名认证/门诊"),
    ONLINE("ONLINE", "在线问诊"),
    INP("INP", "住院"),
    DRUG("DRUG", "药房"),
    EXA("EXA", "检查检验"),
    SYS("SYS", "系统管理"),
    OTHER("OTHER", "其他");

    private final String code;
    private final String desc;

    BizType(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static BizType fromCode(String code) {
        if (code == null) {
            return OTHER;
        }
        for (BizType type : values()) {
            if (type.code.equalsIgnoreCase(code)) {
                return type;
            }
        }
        return OTHER;
    }
}
