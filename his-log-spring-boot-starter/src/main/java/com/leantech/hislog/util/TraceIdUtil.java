package com.leantech.hislog.util;

import org.slf4j.MDC;

import java.util.UUID;

/**
 * TraceID 工具类，管理 MDC 的 UUID traceId 和业务语义化 traceId
 * 核心字段均存入 MDC，同时保留 ThreadLocal 作为降级方案
 */
public class TraceIdUtil {

    // ThreadLocal 作为备用（兼容异步线程未传递 MDC 的场景）
    private static final ThreadLocal<String> MDC_TRACE_ID = new ThreadLocal<>();
    private static final ThreadLocal<String> BIZ_TRACE_ID = new ThreadLocal<>();
    private static final ThreadLocal<String> ORG_CODE = new ThreadLocal<>();
    private static final ThreadLocal<String> USER_ID = new ThreadLocal<>();
    private static final ThreadLocal<String> LOGIN_ID = new ThreadLocal<>();

    private static final String MDC_KEY_TRACE_ID = "traceId";
    private static final String MDC_KEY_BIZ_TRACE_ID = "bizTraceId";
    private static final String MDC_KEY_ORG_CODE = "orgCode";
    private static final String MDC_KEY_USER_ID = "userId";
    private static final String MDC_KEY_LOGIN_ID = "loginId";

    /**
     * 初始化（在请求入口调用）
     */
    public static void init(String orgCode, String userId, String loginId) {
        System.err.println("[HIS-LOG] 最终使用的 orgCode: " + orgCode);
        System.err.println("[HIS-LOG] 最终使用的 userId: " + userId);
        System.err.println("[HIS-LOG] 最终使用的 loginId: " + loginId);
        System.err.println("========================================");
        // 生成 MDC UUID
        String mdcTraceId = UUID.randomUUID().toString().replace("-", "");
        // 生成业务语义化 TraceID
        String bizTraceId = String.format("%s_%s_%s", orgCode, userId, loginId);

        // 存入 MDC（主方案）
        MDC.put(MDC_KEY_TRACE_ID, mdcTraceId);
        MDC.put(MDC_KEY_BIZ_TRACE_ID, bizTraceId);
        MDC.put(MDC_KEY_ORG_CODE, orgCode);
        MDC.put(MDC_KEY_USER_ID, userId);
        MDC.put(MDC_KEY_LOGIN_ID, loginId);

        // 同时存入 ThreadLocal（降级方案）
        MDC_TRACE_ID.set(mdcTraceId);
        BIZ_TRACE_ID.set(bizTraceId);
        ORG_CODE.set(orgCode);
        USER_ID.set(userId);
        LOGIN_ID.set(loginId);
    }

    // ========== Getter：优先从 MDC 获取，若为空则降级到 ThreadLocal ==========
    public static String getMdcTraceId() {
        String value = MDC.get(MDC_KEY_TRACE_ID);
        return value != null ? value : MDC_TRACE_ID.get();
    }

    public static String getBizTraceId() {
        String value = MDC.get(MDC_KEY_BIZ_TRACE_ID);
        return value != null ? value : BIZ_TRACE_ID.get();
    }

    public static String getOrgCode() {
        String value = MDC.get(MDC_KEY_ORG_CODE);
        return value != null ? value : ORG_CODE.get();
    }

    public static String getUserId() {
        String value = MDC.get(MDC_KEY_USER_ID);
        return value != null ? value : USER_ID.get();
    }

    public static String getLoginId() {
        String value = MDC.get(MDC_KEY_LOGIN_ID);
        return value != null ? value : LOGIN_ID.get();
    }

    // ========== Setter（供 TaskDecorator 或特殊场景使用） ==========
    public static void setMdcTraceId(String traceId) {
        if (traceId != null) {
            MDC.put(MDC_KEY_TRACE_ID, traceId);
            MDC_TRACE_ID.set(traceId);
        }
    }

    public static void setBizTraceId(String bizTraceId) {
        if (bizTraceId != null) {
            MDC.put(MDC_KEY_BIZ_TRACE_ID, bizTraceId);
            BIZ_TRACE_ID.set(bizTraceId);
        }
    }

    public static void setOrgCode(String orgCode) {
        if (orgCode != null) {
            MDC.put(MDC_KEY_ORG_CODE, orgCode);
            ORG_CODE.set(orgCode);
        }
    }

    public static void setUserId(String userId) {
        if (userId != null) {
            MDC.put(MDC_KEY_USER_ID, userId);
            USER_ID.set(userId);
        }
    }

    public static void setLoginId(String loginId) {
        if (loginId != null) {
            MDC.put(MDC_KEY_LOGIN_ID, loginId);
            LOGIN_ID.set(loginId);
        }
    }

    /**
     * 清理 ThreadLocal 和 MDC（务必在请求结束时调用）
     */
    public static void clear() {
        MDC_TRACE_ID.remove();
        BIZ_TRACE_ID.remove();
        ORG_CODE.remove();
        USER_ID.remove();
        LOGIN_ID.remove();

        MDC.remove(MDC_KEY_TRACE_ID);
        MDC.remove(MDC_KEY_BIZ_TRACE_ID);
        MDC.remove(MDC_KEY_ORG_CODE);
        MDC.remove(MDC_KEY_USER_ID);
        MDC.remove(MDC_KEY_LOGIN_ID);
    }
}