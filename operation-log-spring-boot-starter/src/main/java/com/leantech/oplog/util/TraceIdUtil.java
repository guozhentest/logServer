package com.leantech.oplog.util;

import org.slf4j.MDC;

import java.util.UUID;

public final class TraceIdUtil {

    private static final ThreadLocal<String> MDC_TRACE_ID = new ThreadLocal<String>();
    private static final ThreadLocal<String> BIZ_TRACE_ID = new ThreadLocal<String>();
    private static final ThreadLocal<String> ORG_CODE = new ThreadLocal<String>();
    private static final ThreadLocal<String> USER_ID = new ThreadLocal<String>();
    private static final ThreadLocal<String> LOGIN_ID = new ThreadLocal<String>();

    private TraceIdUtil() {
    }

    public static void init(String orgCode, String userId, String loginId) {
        String mdcTraceId = UUID.randomUUID().toString().replace("-", "");
        String bizTraceId = String.format("%s_%s_%s", orgCode, userId, loginId);

        MDC.put("traceId", mdcTraceId);
        MDC.put("bizTraceId", bizTraceId);
        MDC.put("orgCode", orgCode);
        MDC.put("userId", userId);
        MDC.put("loginId", loginId);

        MDC_TRACE_ID.set(mdcTraceId);
        BIZ_TRACE_ID.set(bizTraceId);
        ORG_CODE.set(orgCode);
        USER_ID.set(userId);
        LOGIN_ID.set(loginId);
    }

    public static String getMdcTraceId() {
        return MDC_TRACE_ID.get();
    }

    public static String getBizTraceId() {
        return BIZ_TRACE_ID.get();
    }

    public static String getOrgCode() {
        return ORG_CODE.get();
    }

    public static String getUserId() {
        return USER_ID.get();
    }

    public static String getLoginId() {
        return LOGIN_ID.get();
    }

    public static void setMdcTraceId(String traceId) {
        if (traceId != null) {
            MDC_TRACE_ID.set(traceId);
            MDC.put("traceId", traceId);
        }
    }

    public static void setBizTraceId(String bizTraceId) {
        if (bizTraceId != null) {
            BIZ_TRACE_ID.set(bizTraceId);
            MDC.put("bizTraceId", bizTraceId);
        }
    }

    public static void setOrgCode(String orgCode) {
        if (orgCode != null) {
            ORG_CODE.set(orgCode);
            MDC.put("orgCode", orgCode);
        }
    }

    public static void setUserId(String userId) {
        if (userId != null) {
            USER_ID.set(userId);
            MDC.put("userId", userId);
        }
    }

    public static void setLoginId(String loginId) {
        if (loginId != null) {
            LOGIN_ID.set(loginId);
            MDC.put("loginId", loginId);
        }
    }

    public static void clear() {
        MDC_TRACE_ID.remove();
        BIZ_TRACE_ID.remove();
        ORG_CODE.remove();
        USER_ID.remove();
        LOGIN_ID.remove();
        MDC.remove("traceId");
        MDC.remove("bizTraceId");
        MDC.remove("orgCode");
        MDC.remove("userId");
        MDC.remove("loginId");
    }
}
