package com.leantech.agent.service;

import com.leantech.agent.entity.BizTypeDictEntity;
import com.leantech.agent.entity.HospitalInfoEntity;
import com.leantech.agent.entity.ServiceTypeDictEntity;
import com.leantech.agent.model.HospitalRegisterRequest;
import com.leantech.agent.repository.BizTypeDictRepository;
import com.leantech.agent.repository.HospitalInfoRepository;
import com.leantech.agent.repository.ServiceTypeDictRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class HospitalRegistrationService {
    private final HospitalInfoRepository hospitalInfoRepository;
    private final BizTypeDictRepository bizTypeDictRepository;
    private final ServiceTypeDictRepository serviceTypeDictRepository;
    private final HospitalRouteService routeService;

    public HospitalRegistrationService(HospitalInfoRepository hospitalInfoRepository,
                                       BizTypeDictRepository bizTypeDictRepository,
                                       ServiceTypeDictRepository serviceTypeDictRepository,
                                       HospitalRouteService routeService) {
        this.hospitalInfoRepository = hospitalInfoRepository;
        this.bizTypeDictRepository = bizTypeDictRepository;
        this.serviceTypeDictRepository = serviceTypeDictRepository;
        this.routeService = routeService;
    }

    @Transactional
    public void register(HospitalRegisterRequest request) {
        String orgCode = request.getOrgCode();

        // 1. 机构基本信息
        HospitalInfoEntity existing = hospitalInfoRepository.findByOrgCode(orgCode);
        if (existing != null) {
            // 已存在则更新
            existing.setOrgName(request.getOrgName());
            existing.setBaseUrl(request.getBaseUrl());
            existing.setApiKey(request.getApiKey());
            existing.setStatus(1);
            hospitalInfoRepository.updateByOrgCode(existing);
        } else {
            // 不存在则插入
            HospitalInfoEntity hospital = new HospitalInfoEntity();
            hospital.setOrgCode(orgCode);
            hospital.setOrgName(request.getOrgName());
            hospital.setBaseUrl(request.getBaseUrl());
            hospital.setApiKey(request.getApiKey());
            hospital.setStatus(1);
            hospitalInfoRepository.insert(hospital);
        }

        // 2. 业务类型字典
        bizTypeDictRepository.deleteByOrgCode(orgCode);
        if (request.getBizTypes() != null) {
            for (var biz : request.getBizTypes()) {
                BizTypeDictEntity entity = new BizTypeDictEntity();
                entity.setOrgCode(orgCode);
                entity.setTypeCode(biz.getTypeCode());
                entity.setTypeName(biz.getTypeName());
                entity.setKeywords(biz.getKeywords());
                entity.setSortOrder(biz.getSortOrder());
                entity.setIsEnabled(1);
                bizTypeDictRepository.insert(entity);
            }
        }

        // 3. 服务类型字典
        serviceTypeDictRepository.deleteByOrgCode(orgCode);
        if (request.getServiceTypes() != null) {
            for (var svc : request.getServiceTypes()) {
                ServiceTypeDictEntity entity = new ServiceTypeDictEntity();
                entity.setOrgCode(orgCode);
                entity.setTypeCode(svc.getTypeCode());
                entity.setTypeName(svc.getTypeName());
                entity.setClassKeywords(svc.getClassKeywords());
                entity.setSortOrder(svc.getSortOrder());
                entity.setIsEnabled(1);
                serviceTypeDictRepository.insert(entity);
            }
        }

        // 4. 更新 Redis 路由
        routeService.updateRoute(orgCode, request.getBaseUrl(), request.getApiKey());
    }
}