package com.leantech.hislog.util;

import com.alibaba.fastjson2.JSON;

public class JsonUtil {

    public static String toJson(Object obj) {
        if (obj == null) {
            return null;
        }
        try {
            return JSON.toJSONString(obj);
        } catch (Exception e) {
            return obj.toString();
        }
    }
}