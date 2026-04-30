package com.leantech.agent.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.leantech.agent.entity.AgentUserEntity;
import com.leantech.agent.mapper.AgentUserMapper;
import org.springframework.stereotype.Repository;

@Repository
public class UserRepository {
    private final AgentUserMapper mapper;

    public UserRepository(AgentUserMapper mapper) {
        this.mapper = mapper;
    }

    public AgentUserEntity findByUsername(String username) {
        return mapper.selectOne(new LambdaQueryWrapper<AgentUserEntity>()
                .eq(AgentUserEntity::getUsername, username)
                .eq(AgentUserEntity::getEnabled, 1));
    }

    // UserRepository.java
    public void updatePassword(String username, String newPassword) {
        mapper.update(null, new LambdaUpdateWrapper<AgentUserEntity>()
                .eq(AgentUserEntity::getUsername, username)
                .set(AgentUserEntity::getPassword, newPassword));
    }
}