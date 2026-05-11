package com.leantech.admin.agent.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.leantech.admin.agent.entity.BizTypeDictEntity;
import com.leantech.admin.agent.entity.ServiceTypeDictEntity;
import com.leantech.admin.agent.mapper.BizTypeDictMapper;
import com.leantech.admin.agent.mapper.ServiceTypeDictMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class LogDictService {

    private final BizTypeDictMapper bizMapper;
    private final ServiceTypeDictMapper svcMapper;

    public Map<String, Object> getDict(String orgCode) {
        List<BizTypeDictEntity> bizOrg = bizMapper.selectList(
                new LambdaQueryWrapper<BizTypeDictEntity>()
                        .eq(BizTypeDictEntity::getOrgCode, orgCode)
                        .eq(BizTypeDictEntity::getIsEnabled, 1)
        );
        List<BizTypeDictEntity> bizGlobal = bizMapper.selectList(
                new LambdaQueryWrapper<BizTypeDictEntity>()
                        .eq(BizTypeDictEntity::getOrgCode, "*")
                        .eq(BizTypeDictEntity::getIsEnabled, 1)
        );

        List<ServiceTypeDictEntity> svcOrg = svcMapper.selectList(
                new LambdaQueryWrapper<ServiceTypeDictEntity>()
                        .eq(ServiceTypeDictEntity::getOrgCode, orgCode)
                        .eq(ServiceTypeDictEntity::getIsEnabled, 1)
        );
        List<ServiceTypeDictEntity> svcGlobal = svcMapper.selectList(
                new LambdaQueryWrapper<ServiceTypeDictEntity>()
                        .eq(ServiceTypeDictEntity::getOrgCode, "*")
                        .eq(ServiceTypeDictEntity::getIsEnabled, 1)
        );

        Set<String> orgBizCodes = new HashSet<>();
        List<Map<String, String>> bizResult = new ArrayList<>();
        for (BizTypeDictEntity e : bizOrg) {
            orgBizCodes.add(e.getTypeCode());
            bizResult.add(toOption(e.getTypeCode(), e.getTypeName(), e.getSortOrder()));
        }
        for (BizTypeDictEntity e : bizGlobal) {
            if (!orgBizCodes.contains(e.getTypeCode())) {
                bizResult.add(toOption(e.getTypeCode(), e.getTypeName(), e.getSortOrder()));
            }
        }
        bizResult.sort(Comparator.comparingInt(o -> Integer.parseInt(o.get("_sort"))));

        Set<String> orgSvcCodes = new HashSet<>();
        List<Map<String, String>> svcResult = new ArrayList<>();
        for (ServiceTypeDictEntity e : svcOrg) {
            orgSvcCodes.add(e.getTypeCode());
            svcResult.add(toOption(e.getTypeCode(), e.getTypeName(), e.getSortOrder()));
        }
        for (ServiceTypeDictEntity e : svcGlobal) {
            if (!orgSvcCodes.contains(e.getTypeCode())) {
                svcResult.add(toOption(e.getTypeCode(), e.getTypeName(), e.getSortOrder()));
            }
        }
        svcResult.sort(Comparator.comparingInt(o -> Integer.parseInt(o.get("_sort"))));

        bizResult.forEach(o -> o.remove("_sort"));
        svcResult.forEach(o -> o.remove("_sort"));

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("bizTypes", bizResult);
        result.put("serviceTypes", svcResult);
        return result;
    }

    private Map<String, String> toOption(String code, String name, Integer sortOrder) {
        Map<String, String> m = new LinkedHashMap<>();
        m.put("value", code);
        m.put("label", name);
        m.put("_sort", String.valueOf(sortOrder != null ? sortOrder : 0));
        return m;
    }
}
