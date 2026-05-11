package com.leantech.oplog.model;

import com.fasterxml.jackson.annotation.JsonFormat;
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
    private String orderNo;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    private Integer page = 1;
    private Integer size = 20;
}
