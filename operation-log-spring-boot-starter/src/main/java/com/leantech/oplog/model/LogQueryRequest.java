package com.leantech.oplog.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class LogQueryRequest {
    private String orgCode;
    private String userId;
    private String loginId;
    private String traceIdPrefix;
    private String bizTypeCode;
    private String serviceType;
    private String responseStatus;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer page = 1;
    private Integer size = 20;
}
