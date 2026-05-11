package com.leantech.admin.agent.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("agent_biz_type_dict")
public class BizTypeDictEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String orgCode;
    private String typeCode;
    private String typeName;
    private String keywords;
    private Integer isEnabled;
    private Integer sortOrder;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
