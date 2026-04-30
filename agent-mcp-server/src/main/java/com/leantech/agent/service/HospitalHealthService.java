package com.leantech.agent.service;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class HospitalHealthService {

    // 机构代码 -> 最近心跳时间戳
    private final Map<String, Long> heartbeatMap = new ConcurrentHashMap<>();

    // 心跳超时阈值：90秒无心跳视为离线
    private static final long OFFLINE_THRESHOLD_MS = 90_000;

    /**
     * 更新机构的心跳时间
     */
    public void updateHeartbeat(String orgCode) {
        heartbeatMap.put(orgCode, System.currentTimeMillis());
    }

    /**
     * 判断机构是否在线
     */
    public boolean isOnline(String orgCode) {
        Long last = heartbeatMap.get(orgCode);
        return last != null && (System.currentTimeMillis() - last) < OFFLINE_THRESHOLD_MS;
    }

    /**
     * 获取所有在线机构（清理超时记录）
     */
    public Map<String, Long> getOnlineHospitals() {
        long now = System.currentTimeMillis();
        heartbeatMap.entrySet().removeIf(e -> (now - e.getValue()) >= OFFLINE_THRESHOLD_MS);
        return heartbeatMap;
    }
}