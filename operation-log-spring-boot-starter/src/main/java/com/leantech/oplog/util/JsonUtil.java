package com.leantech.oplog.util;

import com.alibaba.fastjson2.JSON;

public final class JsonUtil {

    private JsonUtil() {
    }

    public static String toJson(Object obj) {
        if (obj == null) {
            return null;
        }
        try {
            return JSON.toJSONString(obj);
        } catch (Exception ex) {
            return String.valueOf(obj);
        }
    }
}
