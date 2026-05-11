package com.leantech.admin.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName("sys_user")
public class SysUser {
    @TableId(type = IdType.AUTO)
    private Long userId;
    private String userName;
    private String nickName;
    private String password;
    private String email;
    private String phonenumber;
    private String sex;
    private String avatar;
    private String status;
    private String delFlag;
    private String remark;
    private String loginIp;
    private LocalDateTime loginDate;
    @TableField(exist = false)
    private List<Long> roleIds;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
