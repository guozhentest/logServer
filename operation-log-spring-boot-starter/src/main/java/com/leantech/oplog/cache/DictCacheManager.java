package com.leantech.oplog.cache;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.leantech.oplog.entity.BizTypeDict;
import com.leantech.oplog.entity.ServiceTypeDict;
import com.leantech.oplog.mapper.BizTypeDictMapper;
import com.leantech.oplog.mapper.ServiceTypeDictMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DictCacheManager {

    private static final Logger log = LoggerFactory.getLogger(DictCacheManager.class);

    private final BizTypeDictMapper bizTypeDictMapper;
    private final ServiceTypeDictMapper serviceTypeDictMapper;

    private final Map<String, BizTypeDict> bizTypeCodeCache = new ConcurrentHashMap<>();
    private final Map<String, List<String>> bizTypeKeywordCache = new ConcurrentHashMap<>();
    private final Map<String, Integer> bizTypePriorityMap = new ConcurrentHashMap<>();
    private final Map<String, ServiceTypeDict> serviceTypeCodeCache = new ConcurrentHashMap<>();
    private final Map<String, List<String>> serviceTypeClassKeywordCache = new ConcurrentHashMap<>();

    public DictCacheManager(BizTypeDictMapper bizTypeDictMapper,
                            ServiceTypeDictMapper serviceTypeDictMapper) {
        this.bizTypeDictMapper = bizTypeDictMapper;
        this.serviceTypeDictMapper = serviceTypeDictMapper;
    }

    @PostConstruct
    public void loadAll() {
        try {
            loadBizTypeDict();
            loadServiceTypeDict();
            log.info("字典缓存加载完成，业务类型: {} 条，服务类型: {} 条",
                    bizTypeCodeCache.size(), serviceTypeCodeCache.size());
        } catch (Exception e) {
            log.error("字典缓存加载失败，将降级使用默认规则", e);
            bizTypeCodeCache.clear();
            bizTypeKeywordCache.clear();
            bizTypePriorityMap.clear();
            serviceTypeCodeCache.clear();
            serviceTypeClassKeywordCache.clear();
        }
    }

    private void loadBizTypeDict() {
        List<BizTypeDict> list = bizTypeDictMapper.selectList(
                new QueryWrapper<BizTypeDict>().eq("is_enabled", 1).orderByAsc("sort_order"));
        for (BizTypeDict dict : list) {
            bizTypeCodeCache.put(dict.getTypeCode(), dict);
            bizTypePriorityMap.put(dict.getTypeCode(), dict.getSortOrder());
            if (dict.getKeywords() != null && !dict.getKeywords().isEmpty()) {
                List<String> keywords = parseJsonArray(dict.getKeywords());
                bizTypeKeywordCache.put(dict.getTypeCode(), keywords);
            }
        }
    }

    private void loadServiceTypeDict() {
        List<ServiceTypeDict> list = serviceTypeDictMapper.selectList(
                new QueryWrapper<ServiceTypeDict>().eq("is_enabled", 1).orderByAsc("sort_order"));
        for (ServiceTypeDict dict : list) {
            serviceTypeCodeCache.put(dict.getTypeCode(), dict);
            if (dict.getClassKeywords() != null && !dict.getClassKeywords().isEmpty()) {
                List<String> keywords = parseJsonArray(dict.getClassKeywords());
                serviceTypeClassKeywordCache.put(dict.getTypeCode(), keywords);
            }
        }
    }

    private List<String> parseJsonArray(String json) {
        List<String> result = new ArrayList<>();
        try {
            Object parsed = JSON.parse(json);
            if (parsed instanceof JSONArray) {
                JSONArray array = (JSONArray) parsed;
                for (Object item : array) {
                    if (item instanceof String) {
                        result.add((String) item);
                    }
                }
            }
        } catch (Exception e) {
            log.warn("解析 JSON 数组失败: {}", json, e);
        }
        return result;
    }

    public void refresh() {
        bizTypeCodeCache.clear();
        bizTypeKeywordCache.clear();
        bizTypePriorityMap.clear();
        serviceTypeCodeCache.clear();
        serviceTypeClassKeywordCache.clear();
        loadAll();
    }

    public String matchBizTypeByKeyword(String text) {
        if (text == null) return null;
        String lower = text.toLowerCase();
        String bestCode = null;
        int bestPriority = Integer.MAX_VALUE;
        for (Map.Entry<String, List<String>> entry : bizTypeKeywordCache.entrySet()) {
            String code = entry.getKey();
            List<String> keywords = entry.getValue();
            if (keywords == null) {
                continue;
            }
            for (String kw : keywords) {
                if (kw != null && lower.contains(kw.toLowerCase())) {
                    int priority = bizTypePriorityMap.getOrDefault(code, Integer.MAX_VALUE);
                    if (priority < bestPriority) {
                        bestPriority = priority;
                        bestCode = code;
                    }
                    break;
                }
            }
        }
        return bestCode;
    }

    public String matchServiceTypeByClassName(String className) {
        if (className == null) return null;
        String lower = className.toLowerCase();
        for (Map.Entry<String, List<String>> entry : serviceTypeClassKeywordCache.entrySet()) {
            for (String kw : entry.getValue()) {
                if (lower.contains(kw.toLowerCase())) {
                    return entry.getKey();
                }
            }
        }
        return null;
    }

    public boolean containsBizType(String code) {
        return code != null && bizTypeCodeCache.containsKey(code);
    }

    public boolean containsServiceType(String code) {
        return code != null && serviceTypeCodeCache.containsKey(code);
    }

    public Map<String, BizTypeDict> getBizTypeCodeCache() { return bizTypeCodeCache; }
    public Map<String, ServiceTypeDict> getServiceTypeCodeCache() { return serviceTypeCodeCache; }
}
