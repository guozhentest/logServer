package com.leantech.agent.service;

import jakarta.annotation.PostConstruct;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class DictionaryCache {

    private final JdbcTemplate jdbcTemplate;
    private volatile List<BizTypeVO> bizTypes = new CopyOnWriteArrayList<>();
    private volatile List<ServiceTypeVO> serviceTypes = new CopyOnWriteArrayList<>();

    public DictionaryCache(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostConstruct
    public void init() {
        reload();
    }

    public void reload() {
        bizTypes = jdbcTemplate.query(
                "SELECT type_code, type_name FROM agent_biz_type_dict WHERE is_enabled = 1 AND org_code = '*' ORDER BY sort_order",
                (rs, rowNum) -> new BizTypeVO(rs.getString("type_code"), rs.getString("type_name"))
        );
        serviceTypes = jdbcTemplate.query(
                "SELECT type_code, type_name FROM agent_service_type_dict WHERE is_enabled = 1 AND org_code = '*' ORDER BY sort_order",
                (rs, rowNum) -> new ServiceTypeVO(rs.getString("type_code"), rs.getString("type_name"))
        );
    }

    public List<BizTypeVO> getBizTypes() {
        return bizTypes;
    }

    public List<ServiceTypeVO> getServiceTypes() {
        return serviceTypes;
    }

    public List<BizTypeVO> getBizTypes(String orgCode) {
        List<BizTypeVO> result = jdbcTemplate.query(
                "SELECT type_code, type_name FROM agent_biz_type_dict WHERE is_enabled = 1 AND org_code = ? ORDER BY sort_order",
                (rs, rowNum) -> new BizTypeVO(rs.getString("type_code"), rs.getString("type_name")),
                orgCode
        );
        return result.isEmpty() ? getBizTypes() : result;
    }

    public List<ServiceTypeVO> getServiceTypes(String orgCode) {
        List<ServiceTypeVO> result = jdbcTemplate.query(
                "SELECT type_code, type_name FROM agent_service_type_dict WHERE is_enabled = 1 AND org_code = ? ORDER BY sort_order",
                (rs, rowNum) -> new ServiceTypeVO(rs.getString("type_code"), rs.getString("type_name")),
                orgCode
        );
        return result.isEmpty() ? getServiceTypes() : result;
    }

    public record BizTypeVO(String code, String name) {
    }

    public record ServiceTypeVO(String code, String name) {
    }
}
