package com.leantech.oplog.enums;

public enum ServiceType {
    HIS("HIS", "HIS服务"),
    TH_API("TH-API", "TH-API服务"),
    IM("IM", "IM即时通讯"),
    THIRD("THIRD", "第三方服务"),
    MSG("MSG", "消息服务"),
    SUPERVISE("SUPERVISE", "监管服务"),
    TIMER("TIMER", "定时任务"),
    SYS("SYS", "系统内部任务"),
    OTHER("OTHER", "其他服务");

    private final String code;
    private final String desc;

    ServiceType(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static ServiceType fromCode(String code) {
        if (code == null) {
            return OTHER;
        }
        for (ServiceType type : values()) {
            if (type.code.equalsIgnoreCase(code)) {
                return type;
            }
        }
        return OTHER;
    }

    public static ServiceType inferByClassName(String className) {
        if (className == null) {
            return SYS;
        }
        String lower = className.toLowerCase();
        if (lower.contains("his")) {
            return HIS;
        }
        if (lower.contains("th")) {
            return TH_API;
        }
        if (lower.contains("im")) {
            return IM;
        }
        if (lower.contains("third")) {
            return THIRD;
        }
        if (lower.contains("message")) {
            return MSG;
        }
        if (lower.contains("supervise")) {
            return SUPERVISE;
        }
        if (lower.contains("timmer") || lower.contains("timer")) {
            return TIMER;
        }
        return SYS;
    }
}
