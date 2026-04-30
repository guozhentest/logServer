package com.leantech.agent.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("agent_user")
public class AgentUserEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String username;
    @TableField("password")
    private String password;
    private String role;
    private Integer enabled;
    private LocalDateTime createTime;
}