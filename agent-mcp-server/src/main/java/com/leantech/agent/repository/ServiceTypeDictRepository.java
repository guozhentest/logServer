package com.leantech.agent.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.leantech.agent.entity.ServiceTypeDictEntity;
import com.leantech.agent.mapper.ServiceTypeDictMapper;
import org.springframework.stereotype.Repository;

@Repository
public class ServiceTypeDictRepository {
    private final ServiceTypeDictMapper mapper;

    public ServiceTypeDictRepository(ServiceTypeDictMapper mapper) {
        this.mapper = mapper;
    }

    public int deleteByOrgCode(String orgCode) {
        return mapper.delete(new LambdaQueryWrapper<ServiceTypeDictEntity>()
                .eq(ServiceTypeDictEntity::getOrgCode, orgCode));
    }

    public void insert(ServiceTypeDictEntity entity) {
        mapper.insert(entity);
    }
}