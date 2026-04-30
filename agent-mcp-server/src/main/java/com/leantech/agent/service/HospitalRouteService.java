package com.leantech.agent.service;

import com.alibaba.fastjson2.JSON;
import com.leantech.agent.model.HospitalRouteInfo;
import jakarta.annotation.PostConstruct;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class HospitalRouteService {

    private static final String REDIS_KEY = "hospital:route:config";

    private final RedisTemplate<String, String> redisTemplate;
    private final JdbcTemplate jdbcTemplate;
    // 缓存医院路由信息（JSON 字符串，解析后为 HospitalRouteInfo）
    private final Map<String, HospitalRouteInfo> localCache = new ConcurrentHashMap<>();

    public HospitalRouteService(RedisTemplate<String, String> redisTemplate,
                                JdbcTemplate jdbcTemplate) {
        this.redisTemplate = redisTemplate;
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostConstruct
    public void loadRoutes() {
        refreshFromSources();
    }

    /**
     * 获取医院完整路由信息
     */
    public HospitalRouteInfo getRouteInfo(String orgCode) {
        HospitalRouteInfo info = localCache.get(orgCode);
        if (info != null) return info;

        String json = readFromRedis(orgCode);
        if (json != null && !json.isEmpty()) {
            info = HospitalRouteInfo.fromJson(json);
            if (info != null) {
                localCache.put(orgCode, info);
                return info;
            }
        }

        // 回退到数据库
        info = readFromDatabase(orgCode);
        if (info != null) {
            localCache.put(orgCode, info);
            writeToRedis(orgCode, info);
        }
        return info;
    }

    // 兼容旧方法
    public String getHospitalUrl(String orgCode) {
        HospitalRouteInfo info = getRouteInfo(orgCode);
        return info != null ? info.getBaseUrl() : null;
    }

    public String getHospitalApiKey(String orgCode) {
        HospitalRouteInfo info = getRouteInfo(orgCode);
        return info != null ? info.getApiKey() : null;
    }

    /**
     * 更新路由（含 apiKey），同时写入 Redis 和本地缓存
     */
    public void updateRoute(String orgCode, String baseUrl, String apiKey) {
        HospitalRouteInfo info = new HospitalRouteInfo();
        info.setBaseUrl(baseUrl);
        info.setApiKey(apiKey);
        writeToRedis(orgCode, info);
        localCache.put(orgCode, info);
    }

    @Scheduled(fixedDelay = 60000)
    public void refreshCache() {
        refreshFromSources();
    }

    private void refreshFromSources() {
        localCache.clear();
        Map<String, HospitalRouteInfo> routes = readAllFromRedis();
        if (routes.isEmpty()) {
            routes = readAllFromDatabase();
            routes.forEach(this::writeToRedis);
        }
        localCache.putAll(routes);
    }

    private Map<String, HospitalRouteInfo> readAllFromRedis() {
        try {
            Map<Object, Object> entries = redisTemplate.opsForHash().entries(REDIS_KEY);
            Map<String, HospitalRouteInfo> routes = new HashMap<>();
            entries.forEach((key, value) -> {
                if (key != null && value != null) {
                    HospitalRouteInfo info = HospitalRouteInfo.fromJson(String.valueOf(value));
                    if (info != null) {
                        routes.put(String.valueOf(key), info);
                    }
                }
            });
            return routes;
        } catch (DataAccessException ex) {
            return Map.of();
        }
    }

    private String readFromRedis(String orgCode) {
        try {
            Object value = redisTemplate.opsForHash().get(REDIS_KEY, orgCode);
            return value == null ? null : String.valueOf(value);
        } catch (DataAccessException ex) {
            return null;
        }
    }

    private HospitalRouteInfo readFromDatabase(String orgCode) {
        // 从数据库读取 base_url 和 api_key
        return jdbcTemplate.query(
                "SELECT base_url, api_key FROM agent_hospital_info WHERE status = 1 AND org_code = ?",
                ps -> ps.setString(1, orgCode),
                rs -> {
                    if (rs.next()) {
                        HospitalRouteInfo info = new HospitalRouteInfo();
                        info.setBaseUrl(rs.getString("base_url"));
                        info.setApiKey(rs.getString("api_key"));
                        return info;
                    }
                    return null;
                }
        );
    }

    private Map<String, HospitalRouteInfo> readAllFromDatabase() {
        return jdbcTemplate.query(
                "SELECT org_code, base_url, api_key FROM agent_hospital_info WHERE status = 1",
                rs -> {
                    Map<String, HospitalRouteInfo> routes = new HashMap<>();
                    while (rs.next()) {
                        HospitalRouteInfo info = new HospitalRouteInfo();
                        String orgCode = rs.getString("org_code");
                        info.setBaseUrl(rs.getString("base_url"));
                        info.setApiKey(rs.getString("api_key"));
                        routes.put(orgCode, info);
                    }
                    return routes;
                }
        );
    }

    private void writeToRedis(String orgCode, HospitalRouteInfo info) {
        try {
            redisTemplate.opsForHash().put(REDIS_KEY, orgCode, JSON.toJSONString(info));
        } catch (DataAccessException ignored) {
        }
    }
}