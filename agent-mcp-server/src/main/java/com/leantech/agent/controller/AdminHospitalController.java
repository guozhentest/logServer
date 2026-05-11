package com.leantech.agent.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.leantech.agent.entity.HospitalInfoEntity;
import com.leantech.agent.mapper.HospitalInfoMapper;
import com.leantech.agent.model.ApiResult;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/agent/hospital")
@RequiredArgsConstructor
public class AdminHospitalController {

    private final HospitalInfoMapper mapper;

    @GetMapping("/detail")
    public ApiResult<List<HospitalInfoEntity>> listDetail() {
        List<HospitalInfoEntity> list = mapper.selectList(
                new LambdaQueryWrapper<HospitalInfoEntity>().orderByDesc(HospitalInfoEntity::getUpdateTime));
        return ApiResult.success(list);
    }

    @PutMapping("/{id}")
    public ApiResult<Void> update(@PathVariable Long id, @RequestBody HospitalUpdateRequest request) {
        HospitalInfoEntity entity = mapper.selectById(id);
        if (entity == null) {
            return ApiResult.fail("医院不存在");
        }
        if (request.getBaseUrl() != null) entity.setBaseUrl(request.getBaseUrl());
        if (request.getApiKey() != null) entity.setApiKey(request.getApiKey());
        if (request.getStatus() != null) entity.setStatus(request.getStatus());
        if (request.getOrgName() != null) entity.setOrgName(request.getOrgName());
        entity.setUpdateTime(LocalDateTime.now());
        mapper.updateById(entity);
        return ApiResult.success();
    }

    @Data
    public static class HospitalUpdateRequest {
        private String orgName;
        private String baseUrl;
        private String apiKey;
        private Integer status;
    }
}
