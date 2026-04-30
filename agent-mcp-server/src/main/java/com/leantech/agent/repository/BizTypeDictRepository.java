package com.leantech.agent.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.leantech.agent.entity.BizTypeDictEntity;
import com.leantech.agent.mapper.BizTypeDictMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class BizTypeDictRepository {
    private final BizTypeDictMapper mapper;

    public BizTypeDictRepository(BizTypeDictMapper mapper) {
        this.mapper = mapper;
    }

    public int deleteByOrgCode(String orgCode) {
        return mapper.delete(new LambdaQueryWrapper<BizTypeDictEntity>()
                .eq(BizTypeDictEntity::getOrgCode, orgCode));
    }

    public void insert(BizTypeDictEntity entity) {
        mapper.insert(entity);
    }
}