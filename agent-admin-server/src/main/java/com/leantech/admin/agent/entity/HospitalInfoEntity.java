package com.leantech.admin.agent.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("agent_hospital_info")
public class HospitalInfoEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String orgCode;
    private String orgName;
    private String baseUrl;
    private String apiKey;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
