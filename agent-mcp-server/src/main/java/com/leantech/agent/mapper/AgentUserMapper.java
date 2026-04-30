package com.leantech.agent.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.leantech.agent.entity.AgentUserEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AgentUserMapper extends BaseMapper<AgentUserEntity> {
}