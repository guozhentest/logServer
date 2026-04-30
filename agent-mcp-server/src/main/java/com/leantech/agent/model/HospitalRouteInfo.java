package com.leantech.agent.model;

import com.alibaba.fastjson2.JSON;
import lombok.Data;

@Data
public class HospitalRouteInfo {
    private String baseUrl;
    private String apiKey;

    public static HospitalRouteInfo fromJson(String json) {
        if (json == null || json.isEmpty()) return null;
        try {
            return JSON.parseObject(json, HospitalRouteInfo.class);
        } catch (Exception e) {
            // 兼容旧版纯 URL 字符串
            HospitalRouteInfo info = new HospitalRouteInfo();
            info.setBaseUrl(json);
            return info;
        }
    }
}