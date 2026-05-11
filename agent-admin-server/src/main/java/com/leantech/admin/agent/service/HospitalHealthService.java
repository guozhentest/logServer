package com.leantech.admin.agent.service;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class HospitalHealthService {

    private final Map<String, Long> heartbeatMap = new ConcurrentHashMap<>();

    private static final long OFFLINE_THRESHOLD_MS = 90_000;

    public void updateHeartbeat(String orgCode) {
        heartbeatMap.put(orgCode, System.currentTimeMillis());
    }

    public boolean isOnline(String orgCode) {
        Long last = heartbeatMap.get(orgCode);
        return last != null && (System.currentTimeMillis() - last) < OFFLINE_THRESHOLD_MS;
    }

    public Long getLastHeartbeatTime(String orgCode) {
        return heartbeatMap.get(orgCode);
    }

    public Map<String, Long> getOnlineHospitals() {
        long now = System.currentTimeMillis();
        heartbeatMap.entrySet().removeIf(e -> (now - e.getValue()) >= OFFLINE_THRESHOLD_MS);
        return heartbeatMap;
    }
}
