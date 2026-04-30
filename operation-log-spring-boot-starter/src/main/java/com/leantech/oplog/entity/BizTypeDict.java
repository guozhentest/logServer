package com.leantech.oplog.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;

@Data
@TableName("operation_biz_type_dict")
public class BizTypeDict {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String typeCode;
    private String typeName;
    private String keywords;
    private Integer isEnabled;
    private Integer sortOrder;
    private Date createTime;
    private Date updateTime;
}
