package com.leantech.agent.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.leantech.agent.entity.HospitalInfoEntity;
import com.leantech.agent.mapper.HospitalInfoMapper;
import com.leantech.agent.model.HospitalInfo;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

@Repository
public class HospitalRepository {
    private final HospitalInfoMapper mapper;

    public HospitalRepository(HospitalInfoMapper mapper) {
        this.mapper = mapper;
    }

    public List<HospitalInfo> findAllActive() {
        return mapper.selectList(new LambdaQueryWrapper<HospitalInfoEntity>()
                .eq(HospitalInfoEntity::getStatus, 1)
                .orderByAsc(HospitalInfoEntity::getOrgName))
                .stream()
                .map(e -> new HospitalInfo(e.getOrgCode(), e.getOrgName()))
                .collect(Collectors.toList());
    }
}