package com.leantech.agent.model;

import lombok.Data;

@Data
public class QueryRequest {
    private String orgCode;
    private String userId;
    private String bizTypeCode;
    private String serviceType;
    private String responseStatus;
    private String orderNo;
    private String startTime;
    private String endTime;
    private String traceIdPrefix;
    private Integer page;
    private Integer size;
}
