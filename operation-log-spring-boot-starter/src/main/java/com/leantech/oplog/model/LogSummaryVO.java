package com.leantech.oplog.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class LogSummaryVO {
    private Long id;
    private String traceId;
    private String orgCode;
    private String userId;
    private String loginId;
    private String bizTypeCode;
    private String serviceType;
    private String requestUrl;
    private String operation;
    private String apiName;
    private String responseStatus;
    private Integer costMs;
    private String orderNo;
    private LocalDateTime createdAt;
    private Boolean hasDetail;
    private String requestBodyPreview;
    private String responseBodyPreview;
}
