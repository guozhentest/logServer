package com.leantech.admin.agent.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.leantech.admin.agent.entity.HospitalInfoEntity;
import com.leantech.admin.agent.mapper.HospitalInfoMapper;
import com.leantech.admin.common.ServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class HospitalService {

    private final HospitalInfoMapper mapper;

    public List<HospitalInfoEntity> listAllDetail() {
        return mapper.selectList(
                new LambdaQueryWrapper<HospitalInfoEntity>().orderByDesc(HospitalInfoEntity::getUpdateTime));
    }

    public HospitalInfoEntity getById(Long id) {
        return mapper.selectById(id);
    }

    public HospitalInfoEntity getByOrgCode(String orgCode) {
        return mapper.selectOne(
                new LambdaQueryWrapper<HospitalInfoEntity>()
                        .eq(HospitalInfoEntity::getOrgCode, orgCode));
    }

    public void update(Long id, Map<String, Object> data) {
        HospitalInfoEntity entity = mapper.selectById(id);
        if (entity == null) {
            throw new ServiceException("医院不存在");
        }
        if (data.containsKey("orgName") && data.get("orgName") != null) {
            entity.setOrgName((String) data.get("orgName"));
        }
        if (data.containsKey("baseUrl") && data.get("baseUrl") != null) {
            entity.setBaseUrl((String) data.get("baseUrl"));
        }
        if (data.containsKey("apiKey") && data.get("apiKey") != null) {
            entity.setApiKey((String) data.get("apiKey"));
        }
        if (data.containsKey("status") && data.get("status") != null) {
            entity.setStatus((Integer) data.get("status"));
        }
        entity.setUpdateTime(LocalDateTime.now());
        mapper.updateById(entity);
    }
}
