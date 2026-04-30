package com.leantech.agent.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

@Data
@TableName("agent_service_type_dict")
public class ServiceTypeDictEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String orgCode;
    private String typeCode;
    private String typeName;
    private String classKeywords;
    private Integer isEnabled;
    private Integer sortOrder;
}