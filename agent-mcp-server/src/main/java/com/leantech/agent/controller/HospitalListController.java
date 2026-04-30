package com.leantech.agent.controller;

import com.leantech.agent.model.ApiResult;
import com.leantech.agent.model.HospitalInfo;
import com.leantech.agent.repository.HospitalRepository;
import com.leantech.agent.service.HospitalHealthService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/agent")
public class HospitalListController extends BaseController {

    private final HospitalRepository hospitalRepository;
    private final HospitalHealthService healthService;

    public HospitalListController(HospitalRepository hospitalRepository,
                                  HospitalHealthService healthService) {
        this.hospitalRepository = hospitalRepository;
        this.healthService = healthService;
    }

    @GetMapping("/hospitals")
    public ApiResult<List<HospitalInfo>> getHospitals() {
        List<HospitalInfo> onlineHospitals = hospitalRepository.findAllActive()
                .stream()
                .filter(h -> healthService.isOnline(h.getOrgCode()))
                .collect(Collectors.toList());
        return ok(onlineHospitals);
    }
}