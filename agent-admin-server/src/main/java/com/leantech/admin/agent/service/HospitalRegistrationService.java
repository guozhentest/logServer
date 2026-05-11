package com.leantech.admin.agent.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.leantech.admin.agent.entity.BizTypeDictEntity;
import com.leantech.admin.agent.entity.HospitalInfoEntity;
import com.leantech.admin.agent.entity.ServiceTypeDictEntity;
import com.leantech.admin.agent.mapper.BizTypeDictMapper;
import com.leantech.admin.agent.mapper.HospitalInfoMapper;
import com.leantech.admin.agent.mapper.ServiceTypeDictMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class HospitalRegistrationService {

    private final HospitalInfoMapper hospitalMapper;
    private final BizTypeDictMapper bizTypeDictMapper;
    private final ServiceTypeDictMapper svcTypeDictMapper;

    @Transactional
    public void register(String orgCode, String orgName, String baseUrl, String apiKey,
                         List<Map<String, String>> bizTypes, List<Map<String, String>> serviceTypes) {

        HospitalInfoEntity hospital = hospitalMapper.selectOne(
                new LambdaQueryWrapper<HospitalInfoEntity>()
                        .eq(HospitalInfoEntity::getOrgCode, orgCode));

        if (hospital != null) {
            hospital.setOrgName(orgName);
            hospital.setBaseUrl(baseUrl);
            hospital.setApiKey(apiKey);
            hospital.setStatus(1);
            hospital.setUpdateTime(LocalDateTime.now());
            hospitalMapper.updateById(hospital);
        } else {
            hospital = new HospitalInfoEntity();
            hospital.setOrgCode(orgCode);
            hospital.setOrgName(orgName);
            hospital.setBaseUrl(baseUrl);
            hospital.setApiKey(apiKey);
            hospital.setStatus(1);
            hospital.setCreateTime(LocalDateTime.now());
            hospital.setUpdateTime(LocalDateTime.now());
            hospitalMapper.insert(hospital);
        }

        bizTypeDictMapper.delete(new LambdaQueryWrapper<BizTypeDictEntity>()
                .eq(BizTypeDictEntity::getOrgCode, orgCode));
        if (bizTypes != null) {
            int order = 0;
            for (Map<String, String> bt : bizTypes) {
                BizTypeDictEntity entity = new BizTypeDictEntity();
                entity.setOrgCode(orgCode);
                entity.setTypeCode(bt.get("typeCode"));
                entity.setTypeName(bt.get("typeName"));
                entity.setIsEnabled(1);
                entity.setSortOrder(order++);
                entity.setCreateTime(LocalDateTime.now());
                entity.setUpdateTime(LocalDateTime.now());
                bizTypeDictMapper.insert(entity);
            }
        }

        svcTypeDictMapper.delete(new LambdaQueryWrapper<ServiceTypeDictEntity>()
                .eq(ServiceTypeDictEntity::getOrgCode, orgCode));
        if (serviceTypes != null) {
            int order = 0;
            for (Map<String, String> st : serviceTypes) {
                ServiceTypeDictEntity entity = new ServiceTypeDictEntity();
                entity.setOrgCode(orgCode);
                entity.setTypeCode(st.get("typeCode"));
                entity.setTypeName(st.get("typeName"));
                entity.setIsEnabled(1);
                entity.setSortOrder(order++);
                entity.setCreateTime(LocalDateTime.now());
                entity.setUpdateTime(LocalDateTime.now());
                svcTypeDictMapper.insert(entity);
            }
        }
    }
}
