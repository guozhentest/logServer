package com.leantech.hislog.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user_operation_log")
public class UserOperationLogEntity {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String traceId;
    private String orgCode;
    private String userId;
    private String loginId;
    private String bizTypeCode;
    private String subBizCode;
    private String logLevel;
    private String operation;
    private String hisApiName;
    private String hisRequestId;
    private String responseStatus;
    private Integer costMs;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}