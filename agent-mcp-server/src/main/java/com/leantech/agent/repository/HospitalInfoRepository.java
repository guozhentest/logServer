package com.leantech.agent.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.leantech.agent.entity.HospitalInfoEntity;
import com.leantech.agent.mapper.HospitalInfoMapper;
import org.springframework.stereotype.Repository;

@Repository
public class HospitalInfoRepository {
    private final HospitalInfoMapper mapper;

    public HospitalInfoRepository(HospitalInfoMapper mapper) {
        this.mapper = mapper;
    }

    // 原有查询方法保留，新增插入或更新
    public void insertOrUpdate(HospitalInfoEntity entity) {
        mapper.insert(entity);
    }

    public HospitalInfoEntity findByOrgCode(String orgCode) {
        return mapper.selectOne(new LambdaQueryWrapper<HospitalInfoEntity>()
                .eq(HospitalInfoEntity::getOrgCode, orgCode));
    }

    // HospitalInfoRepository.java
    public void insert(HospitalInfoEntity entity) {
        mapper.insert(entity);
    }

    public void updateByOrgCode(HospitalInfoEntity entity) {
        mapper.update(entity, new LambdaQueryWrapper<HospitalInfoEntity>()
                .eq(HospitalInfoEntity::getOrgCode, entity.getOrgCode()));
    }
}