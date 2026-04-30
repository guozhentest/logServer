package com.leantech.oplog.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("operation_log_main")
public class OperationLogEntity {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String traceId;
    private String orgCode;
    private String userId;
    private String loginId;
    private String bizTypeCode;
    private String subBizCode;
    private String serviceType;
    private String requestUrl;
    private String logLevel;
    private String operation;
    private String apiName;
    private String requestId;
    private String responseStatus;
    private Integer costMs;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
