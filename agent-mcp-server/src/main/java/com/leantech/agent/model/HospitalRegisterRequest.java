package com.leantech.agent.model;

import lombok.Data;

import java.util.List;

@Data
public class HospitalRegisterRequest {

    private String orgCode;
    private String orgName;
    private String baseUrl;
    private String apiKey;

    private List<BizTypeItem> bizTypes;
    private List<ServiceTypeItem> serviceTypes;

    @Data
    public static class BizTypeItem {

        private String typeCode;
        private String typeName;
        private String keywords;
        private Integer sortOrder;
    }

    @Data
    public static class ServiceTypeItem {

        private String typeCode;
        private String typeName;
        private String classKeywords;
        private Integer sortOrder;
    }
}
