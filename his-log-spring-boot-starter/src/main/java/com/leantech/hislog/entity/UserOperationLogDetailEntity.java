package com.leantech.hislog.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user_operation_log_detail")
public class UserOperationLogDetailEntity {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long logId;
    private String requestBody;
    private String responseBody;
    private String errorStack;
    private LocalDateTime createdAt;
}