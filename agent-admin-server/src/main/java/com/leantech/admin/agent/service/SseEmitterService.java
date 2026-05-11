package com.leantech.admin.agent.service;

import com.alibaba.fastjson2.JSON;
import com.leantech.admin.agent.entity.HospitalInfoEntity;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
@EnableScheduling
public class SseEmitterService {

    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();
    private final HospitalService hospitalService;
    private final HospitalHealthService healthService;

    private final Map<String, Boolean> knownOnlineStatus = new HashMap<>();

    public SseEmitterService(HospitalService hospitalService, HospitalHealthService healthService) {
        this.hospitalService = hospitalService;
        this.healthService = healthService;
    }

    public SseEmitter subscribe() {
        SseEmitter emitter = new SseEmitter(0L);
        emitters.add(emitter);
        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));
        emitter.onError(e -> emitters.remove(emitter));

        try {
            List<HospitalInfoEntity> all = hospitalService.listAllDetail();
            List<Map<String, Object>> list = all.stream().map(h -> {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("id", h.getId());
                m.put("orgCode", h.getOrgCode());
                m.put("orgName", h.getOrgName());
                m.put("baseUrl", h.getBaseUrl());
                m.put("apiKey", h.getApiKey());
                m.put("status", h.getStatus());
                m.put("online", healthService.isOnline(h.getOrgCode()));
                Long hb = healthService.getLastHeartbeatTime(h.getOrgCode());
                m.put("lastHeartbeatTime", hb);
                knownOnlineStatus.put(h.getOrgCode(), healthService.isOnline(h.getOrgCode()));
                return m;
            }).toList();
            Map<String, Object> init = new LinkedHashMap<>();
            init.put("type", "init");
            init.put("data", list);
            emitter.send(SseEmitter.event().name("init").data(JSON.toJSONString(init)));
        } catch (Exception e) {
            emitters.remove(emitter);
        }
        return emitter;
    }

    public void notifyHeartbeat(String orgCode) {
        knownOnlineStatus.put(orgCode, true);
        sendUpdate(orgCode, true, System.currentTimeMillis());
    }

    @Scheduled(fixedRate = 5000)
    public void checkHeartbeatExpiry() {
        List<HospitalInfoEntity> all = hospitalService.listAllDetail();
        for (HospitalInfoEntity h : all) {
            String orgCode = h.getOrgCode();
            boolean nowOnline = healthService.isOnline(orgCode);
            Boolean wasOnline = knownOnlineStatus.get(orgCode);
            if (wasOnline != null && wasOnline && !nowOnline) {
                knownOnlineStatus.put(orgCode, false);
                Long lastHb = healthService.getLastHeartbeatTime(orgCode);
                sendUpdate(orgCode, false, lastHb != null ? lastHb : System.currentTimeMillis());
            }
        }
    }

    private void sendUpdate(String orgCode, boolean online, Long lastHeartbeatTime) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("type", "status");
        data.put("orgCode", orgCode);
        data.put("online", online);
        data.put("lastHeartbeatTime", lastHeartbeatTime);
        String json = JSON.toJSONString(data);
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event().name("status").data(json));
            } catch (Exception e) {
                emitters.remove(emitter);
            }
        }
    }
}
