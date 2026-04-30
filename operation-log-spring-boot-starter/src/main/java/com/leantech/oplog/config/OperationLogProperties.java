package com.leantech.oplog.config;

import com.leantech.oplog.enums.BizType;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ConfigurationProperties(prefix = "operation.log")
public class OperationLogProperties {

    private boolean enabled = true;
    private boolean compressBody = false;
    private boolean useDictTable = true;
    private UserInfo userInfo = new UserInfo();
    private Header header = new Header();
    private Api api = new Api();
    private DynamicParam dynamicParam = new DynamicParam();
    private Map<BizType, List<String>> bizTypeKeywords = new HashMap<BizType, List<String>>();


    public static class UserInfo {
        private String attributeKey = "XCX_LOGIN_INFO";
        private String userIdField = "userId";
        private String fixedOrgCode;

        public String getAttributeKey() {
            return attributeKey;
        }

        public void setAttributeKey(String attributeKey) {
            this.attributeKey = attributeKey;
        }

        public String getUserIdField() {
            return userIdField;
        }

        public void setUserIdField(String userIdField) {
            this.userIdField = userIdField;
        }

        public String getFixedOrgCode() {
            return fixedOrgCode;
        }

        public void setFixedOrgCode(String fixedOrgCode) {
            this.fixedOrgCode = fixedOrgCode;
        }
    }

    public static class Header {
        private String traceHeaderName = "x-trace";
        private String apiKeyHeaderName = "X-API-Key";

        public String getTraceHeaderName() {
            return traceHeaderName;
        }

        public void setTraceHeaderName(String traceHeaderName) {
            this.traceHeaderName = traceHeaderName;
        }

        public String getApiKeyHeaderName() {
            return apiKeyHeaderName;
        }

        public void setApiKeyHeaderName(String apiKeyHeaderName) {
            this.apiKeyHeaderName = apiKeyHeaderName;
        }
    }

    public static class Api {
        private boolean enabled = true;
        private String apiKey;
        private int maxLimit = 1000;
        private int bodyTruncateLength = 500;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getApiKey() {
            return apiKey;
        }

        public void setApiKey(String apiKey) {
            this.apiKey = apiKey;
        }

        public int getMaxLimit() {
            return maxLimit;
        }

        public void setMaxLimit(int maxLimit) {
            this.maxLimit = maxLimit;
        }

        public int getBodyTruncateLength() {
            return bodyTruncateLength;
        }

        public void setBodyTruncateLength(int bodyTruncateLength) {
            this.bodyTruncateLength = bodyTruncateLength;
        }
    }

    public static class DynamicParam {
        private String apiNameParam = "method";
        private String describeParam = "methodDescribe";
        private String subBizParam = "subBizType";   // 新增

        public String getApiNameParam() { return apiNameParam; }
        public void setApiNameParam(String apiNameParam) { this.apiNameParam = apiNameParam; }

        public String getDescribeParam() { return describeParam; }
        public void setDescribeParam(String describeParam) { this.describeParam = describeParam; }

        public String getSubBizParam() { return subBizParam; }
        public void setSubBizParam(String subBizParam) { this.subBizParam = subBizParam; }
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isCompressBody() {
        return compressBody;
    }

    public void setCompressBody(boolean compressBody) {
        this.compressBody = compressBody;
    }

    public boolean isUseDictTable() {
        return useDictTable;
    }

    public void setUseDictTable(boolean useDictTable) {
        this.useDictTable = useDictTable;
    }

    public UserInfo getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(UserInfo userInfo) {
        this.userInfo = userInfo;
    }

    public Header getHeader() {
        return header;
    }

    public void setHeader(Header header) {
        this.header = header;
    }

    public Api getApi() {
        return api;
    }

    public void setApi(Api api) {
        this.api = api;
    }

    public DynamicParam getDynamicParam() {
        return dynamicParam;
    }

    public void setDynamicParam(DynamicParam dynamicParam) {
        this.dynamicParam = dynamicParam;
    }

    public Map<BizType, List<String>> getBizTypeKeywords() {
        return bizTypeKeywords;
    }

    public void setBizTypeKeywords(Map<BizType, List<String>> bizTypeKeywords) {
        this.bizTypeKeywords = bizTypeKeywords;
    }
}
