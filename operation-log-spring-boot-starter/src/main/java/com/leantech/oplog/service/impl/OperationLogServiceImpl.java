package com.leantech.oplog.service.impl;

import com.leantech.oplog.entity.OperationLogDetailEntity;
import com.leantech.oplog.entity.OperationLogEntity;
import com.leantech.oplog.mapper.DetailMapper;
import com.leantech.oplog.mapper.LogMapper;
import com.leantech.oplog.model.LogQueryRequest;
import com.leantech.oplog.model.LogQueryResponse;
import com.leantech.oplog.model.LogSummaryVO;
import com.leantech.oplog.service.OperationLogService;
import org.springframework.beans.BeanUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.sql.Timestamp;
import java.util.LinkedHashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class OperationLogServiceImpl implements OperationLogService {

    private final LogMapper logMapper;
    private final DetailMapper detailMapper;
    private final JdbcTemplate jdbcTemplate;

    public OperationLogServiceImpl(LogMapper logMapper, DetailMapper detailMapper, JdbcTemplate jdbcTemplate) {
        this.logMapper = logMapper;
        this.detailMapper = detailMapper;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    @Transactional
    public Long save(OperationLogEntity logEntity, OperationLogDetailEntity detailEntity) {
        logMapper.insert(logEntity);
        if (detailEntity != null) {
            detailEntity.setLogId(logEntity.getId());
            detailMapper.insert(detailEntity);
        }
        return logEntity.getId();
    }

    @Override
    @Async("operationLogAsyncExecutor")
    public void saveAsync(OperationLogEntity logEntity, OperationLogDetailEntity detailEntity) {
        save(logEntity, detailEntity);
    }

    @Override
    public LogQueryResponse.PageData<LogSummaryVO> queryLogs(LogQueryRequest request, int maxLimit, int truncateLength) {
        // 构建动态 SQL
        StringBuilder sql = new StringBuilder("SELECT * FROM operation_log_main WHERE 1=1 ");
        List<Object> params = new ArrayList<>();
        addWhere(request, sql, params);

        // 查询总数
        String countSql = "SELECT COUNT(*) FROM (" + sql + ") AS tmp";
        Long total = jdbcTemplate.queryForObject(countSql, Long.class, params.toArray());

        // 分页参数
        final int page = request.getPage() != null && request.getPage() > 0 ? request.getPage() : 1;
        int rawSize = request.getSize() != null && request.getSize() > 0 ? request.getSize() : 20;
        final int size = (rawSize > maxLimit) ? maxLimit : rawSize;
        final int offset = (page - 1) * size;

        // 查询分页数据
        List<OperationLogEntity> list = jdbcTemplate.query(
                sql.toString() + " ORDER BY created_at DESC LIMIT ?, ?",
                ps -> {
                    int index = 1;
                    for (Object param : params) {
                        ps.setObject(index++, param);
                    }
                    ps.setInt(index++, offset);
                    ps.setInt(index, size);
                },
                (rs, rowNum) -> {
                    OperationLogEntity entity = new OperationLogEntity();
                    entity.setId(rs.getLong("id"));
                    entity.setTraceId(rs.getString("trace_id"));
                    entity.setOrgCode(rs.getString("org_code"));
                    entity.setUserId(rs.getString("user_id"));
                    entity.setLoginId(rs.getString("login_id"));
                    entity.setBizTypeCode(rs.getString("biz_type_code"));
                    entity.setSubBizCode(rs.getString("sub_biz_code"));
                    entity.setServiceType(rs.getString("service_type"));
                    entity.setRequestUrl(rs.getString("request_url"));
                    entity.setLogLevel(rs.getString("log_level"));
                    entity.setOperation(rs.getString("operation"));
                    entity.setApiName(rs.getString("api_name"));
                    entity.setRequestId(rs.getString("request_id"));
                    entity.setResponseStatus(rs.getString("response_status"));
                    entity.setCostMs(rs.getInt("cost_ms"));
                    try {
                        entity.setOrderNo(rs.getString("order_no"));
                    } catch (java.sql.SQLException e) {
                        entity.setOrderNo(null);
                    }
                    // 兼容 Druid
                    java.sql.Timestamp ts = rs.getTimestamp("created_at");
                    if (ts != null) {
                        entity.setCreatedAt(ts.toLocalDateTime());
                    }
                    return entity;
                }
        );

        // 转换为 VO，详情表也使用 JdbcTemplate 查询，避免 MyBatis-Plus 与 Druid 冲突
        List<LogSummaryVO> records = list.stream().map(entity -> {
            LogSummaryVO vo = new LogSummaryVO();
            BeanUtils.copyProperties(entity, vo);

            // 使用 JdbcTemplate 查询详情，彻底绕开 MyBatis-Plus
            List<OperationLogDetailEntity> details = jdbcTemplate.query(
                    "SELECT id, log_id, request_body, response_body, error_stack, created_at FROM operation_log_detail WHERE log_id = ?",
                    ps -> ps.setLong(1, entity.getId()),
                    (rs, rowNum2) -> {
                        OperationLogDetailEntity detail = new OperationLogDetailEntity();
                        detail.setId(rs.getLong("id"));
                        detail.setLogId(rs.getLong("log_id"));
                        detail.setRequestBody(rs.getString("request_body"));
                        detail.setResponseBody(rs.getString("response_body"));
                        detail.setErrorStack(rs.getString("error_stack"));
                        Timestamp ts = rs.getTimestamp("created_at");
                        if (ts != null) {
                            detail.setCreatedAt(ts.toLocalDateTime());
                        }
                        return detail;
                    }
            );

            if (!details.isEmpty()) {
                OperationLogDetailEntity detail = details.get(0);
                vo.setHasDetail(true);
                vo.setRequestBodyPreview(truncate(detail.getRequestBody(), truncateLength));
                vo.setResponseBodyPreview(truncate(detail.getResponseBody(), truncateLength));
            } else {
                vo.setHasDetail(false);
            }
            return vo;
        }).collect(Collectors.toList());

        LogQueryResponse.PageData<LogSummaryVO> pageData = new LogQueryResponse.PageData<>();
        pageData.setTotal(total);
        pageData.setPage(page);
        pageData.setSize(size);
        pageData.setRecords(records);
        return pageData;
    }

    private void addWhere(LogQueryRequest request, StringBuilder sql, List<Object> params) {
        if (StringUtils.hasText(request.getOrgCode())) {
            sql.append(" AND org_code = ?");
            params.add(request.getOrgCode());
        }
        if (StringUtils.hasText(request.getUserId())) {
            sql.append(" AND user_id = ?");
            params.add(request.getUserId());
        }
        if (StringUtils.hasText(request.getLoginId())) {
            sql.append(" AND login_id = ?");
            params.add(request.getLoginId());
        }
        if (StringUtils.hasText(request.getBizTypeCode())) {
            sql.append(" AND biz_type_code = ?");
            params.add(request.getBizTypeCode());
        }
        if (StringUtils.hasText(request.getServiceType())) {
            sql.append(" AND service_type = ?");
            params.add(request.getServiceType());
        }
        if (StringUtils.hasText(request.getResponseStatus())) {
            sql.append(" AND response_status = ?");
            params.add(request.getResponseStatus());
        }
        if (StringUtils.hasText(request.getTraceIdPrefix())) {
            sql.append(" AND trace_id LIKE ?");
            params.add(request.getTraceIdPrefix() + "%");
        }
        if (StringUtils.hasText(request.getOrderNo())) {
            sql.append(" AND order_no = ?");
            params.add(request.getOrderNo());
        }
        if (request.getStartTime() != null) {
            sql.append(" AND created_at >= ?");
            params.add(Timestamp.valueOf(request.getStartTime()));
        }
        if (request.getEndTime() != null) {
            sql.append(" AND created_at <= ?");
            params.add(Timestamp.valueOf(request.getEndTime()));
        }
    }

    private String truncate(String str, int length) {
        if (str == null) return null;
        return str.length() > length ? str.substring(0, length) + "..." : str;
    }

    @Override
    public Map<String, Object> queryDetail(String logId) {
        Map<String, Object> result = new LinkedHashMap<>();

        List<Map<String, Object>> mains = jdbcTemplate.query(
                "SELECT * FROM operation_log_main WHERE id = ?",
                ps -> ps.setLong(1, Long.parseLong(logId)),
                (rs, rowNum) -> {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("id", rs.getLong("id"));
                    row.put("traceId", rs.getString("trace_id"));
                    row.put("orgCode", rs.getString("org_code"));
                    row.put("userId", rs.getString("user_id"));
                    row.put("loginId", rs.getString("login_id"));
                    row.put("bizTypeCode", rs.getString("biz_type_code"));
                    row.put("subBizCode", rs.getString("sub_biz_code"));
                    row.put("serviceType", rs.getString("service_type"));
                    row.put("requestUrl", rs.getString("request_url"));
                    row.put("logLevel", rs.getString("log_level"));
                    row.put("operation", rs.getString("operation"));
                    row.put("apiName", rs.getString("api_name"));
                    row.put("requestId", rs.getString("request_id"));
                    row.put("responseStatus", rs.getString("response_status"));
                    row.put("costMs", rs.getInt("cost_ms"));
                    try { row.put("orderNo", rs.getString("order_no")); }
                    catch (java.sql.SQLException e) { row.put("orderNo", null); }
                    try { row.put("requestHeaders", rs.getString("request_headers")); }
                    catch (java.sql.SQLException e) { row.put("requestHeaders", null); }
                    Timestamp ts = rs.getTimestamp("created_at");
                    if (ts != null) row.put("createdAt", ts.toLocalDateTime().toString());
                    return row;
                });

        if (mains.isEmpty()) return result;
        result.putAll(mains.get(0));

        List<Map<String, Object>> details = jdbcTemplate.query(
                "SELECT request_body, response_body, error_stack FROM operation_log_detail WHERE log_id = ?",
                ps -> ps.setLong(1, Long.parseLong(logId)),
                (rs, rowNum) -> {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("requestBody", rs.getString("request_body"));
                    row.put("responseBody", rs.getString("response_body"));
                    row.put("errorStack", rs.getString("error_stack"));
                    return row;
                });

        if (!details.isEmpty()) {
            result.putAll(details.get(0));
        }
        return result;
    }
}