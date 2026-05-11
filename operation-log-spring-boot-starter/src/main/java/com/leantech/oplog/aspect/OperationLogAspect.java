package com.leantech.oplog.aspect;

import com.leantech.oplog.annotation.OperationLog;
import com.leantech.oplog.cache.DictCacheManager;
import com.leantech.oplog.config.OperationLogProperties;
import com.leantech.oplog.entity.OperationLogDetailEntity;
import com.leantech.oplog.entity.OperationLogEntity;
import com.leantech.oplog.enums.BizType;
import com.leantech.oplog.enums.ServiceType;
import com.leantech.oplog.service.OperationLogService;
import com.leantech.oplog.util.JsonUtil;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPOutputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.regex.Pattern;

@Aspect
public class OperationLogAspect {

    private static final Logger log = LoggerFactory.getLogger(OperationLogAspect.class);
    private static final int MAX_OPERATION_LENGTH = 200;
    private static final int MAX_API_NAME_LENGTH = 200;
    private static final int MAX_BIZ_TYPE_LENGTH = 50;
    private static final int MAX_ORDER_NO_LENGTH = 100;

    private static final Pattern ORDER_NO_PATTERN = Pattern.compile("^[a-zA-Z0-9_-]+$");
    private static final Pattern HAS_DIGIT_PATTERN = Pattern.compile("\\d");

    private final OperationLogService operationLogService;
    private final OperationLogProperties properties;
    private final DictCacheManager dictCacheManager;

    @Value("${th.organizationcode:UNKNOWN}")
    private String organizationCode;

    public OperationLogAspect(OperationLogService operationLogService,
                              OperationLogProperties properties,
                              DictCacheManager dictCacheManager) {
        this.operationLogService = operationLogService;
        this.properties = properties;
        this.dictCacheManager = dictCacheManager;
    }

    @Around("@annotation(operationLog)")
    public Object around(ProceedingJoinPoint joinPoint, OperationLog operationLog) throws Throwable {
        long startTime = System.currentTimeMillis();
        String responseStatus = "SUCCESS";
        Object result = null;
        Throwable throwable = null;
        try {
            result = joinPoint.proceed();
            return result;
        } catch (Throwable ex) {
            responseStatus = "FAILURE";
            throwable = ex;
            throw ex;
        } finally {
            long costMs = System.currentTimeMillis() - startTime;
            try {
                saveLog(joinPoint, operationLog, result, responseStatus, costMs, throwable);
            } catch (Exception ex) {
                log.error("保存操作日志失败", ex);
            }
        }
    }

    private void saveLog(ProceedingJoinPoint joinPoint, OperationLog operationLog, Object result,
                         String responseStatus, long costMs, Throwable throwable) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String simpleClassName = signature.getDeclaringType().getSimpleName();
        String fullClassName = signature.getDeclaringType().getName(); // 全限定类名，用于服务类型字典匹配
        String methodName = signature.getName();
        Object[] args = joinPoint.getArgs();
        String[] paramNames = signature.getParameterNames();

        log.info("开始记录操作日志 -> 类: {}, 方法: {}", simpleClassName, methodName);

        // ========== 1. 动态提取 method 与 methodDescribe ==========
        String dynamicApiName = null;
        String dynamicDescribe = null;
        String apiNameParam = properties.getDynamicParam().getApiNameParam();
        String describeParam = properties.getDynamicParam().getDescribeParam();
        if (paramNames != null && paramNames.length == args.length) {
            for (int i = 0; i < paramNames.length; i++) {
                String name = paramNames[i];
                Object value = args[i];
                if (value == null) continue;
                if (apiNameParam != null && apiNameParam.equals(name)) {
                    dynamicApiName = String.valueOf(value);
                    log.info("动态提取 apiName: {} = {}", name, dynamicApiName);
                } else if (describeParam != null && describeParam.equals(name)) {
                    dynamicDescribe = String.valueOf(value);
                    log.info("动态提取 describe: {} = {}", name, dynamicDescribe);
                }
            }
        }
        if (dynamicApiName == null || dynamicDescribe == null) {
            for (Object arg : args) {
                if (arg instanceof String) {
                    String str = (String) arg;
                    if (dynamicApiName == null && isLikelyApiName(str)) {
                        dynamicApiName = str;
                        log.info("特征推断 apiName: {}", str);
                    } else if (dynamicDescribe == null && isLikelyDescribe(str)) {
                        dynamicDescribe = str;
                        log.info("特征推断 describe: {}", str);
                    }
                }
            }
        }

        String apiName = dynamicApiName != null ? dynamicApiName : (simpleClassName + "." + methodName);
        String operationDesc = StringUtils.hasText(operationLog.value()) ? operationLog.value()
                : (dynamicDescribe != null ? dynamicDescribe : apiName);
        operationDesc = truncate(operationDesc, MAX_OPERATION_LENGTH);
        log.info("最终操作描述: {}", operationDesc);

        // ========== 2. 提取用户信息 ==========
        String orgCode = properties.getUserInfo().getFixedOrgCode();
        if (!StringUtils.hasText(orgCode)) {
            orgCode = organizationCode;
        }
        String userId = null;
        String loginId = null;
        String requestUrl = null;
        String hisRequestId = null;
        try {
            ServletRequestAttributes attributes = currentRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                requestUrl = request.getRequestURL().toString();
                String traceHeaderName = properties.getHeader().getTraceHeaderName();
                String traceHeader = request.getHeader(traceHeaderName);
                hisRequestId = traceHeader;
                if (StringUtils.hasText(traceHeader)) {
                    loginId = traceHeader.split("-")[0];
                } else if (request.getSession(false) != null) {
                    loginId = request.getSession(false).getId();
                }
                String attributeKey = properties.getUserInfo().getAttributeKey();
                String userIdField = properties.getUserInfo().getUserIdField();
                Object userObj = request.getAttribute(attributeKey);
                if (userObj != null) {
                    Object uid = invokeGetter(userObj, userIdField);
                    if (uid != null) {
                        userId = String.valueOf(uid);
                    }
                }
            }
        } catch (Exception ex) {
            log.warn("提取用户信息失败: {}", ex.getMessage());
        }

        if (!StringUtils.hasText(orgCode)) orgCode = "UNKNOWN";
        if (!StringUtils.hasText(userId)) userId = "UNKNOWN";
        if (!StringUtils.hasText(loginId)) loginId = "UNKNOWN";
        log.info("用户信息: orgCode={}, userId={}, loginId={}", orgCode, userId, loginId);

        // ========== 3. 服务类型 ==========
        ServiceType serviceType = operationLog.serviceType();
        String serviceTypeCode = serviceType != ServiceType.OTHER ? serviceType.getCode() : null;
        log.info("注解指定服务类型: {}", serviceTypeCode);
        if (!StringUtils.hasText(serviceTypeCode) && properties.isUseDictTable()) {
            try {
                serviceTypeCode = dictCacheManager.matchServiceTypeByClassName(fullClassName);
                log.info("字典表匹配服务类型: {}", serviceTypeCode);
            } catch (Exception ex) {
                log.warn("服务类型字典匹配失败", ex);
            }
        }
        if (!StringUtils.hasText(serviceTypeCode)) {
            serviceTypeCode = ServiceType.SYS.getCode();
            log.info("服务类型兜底为 SYS");
        }
        log.info("最终服务类型: {}", serviceTypeCode);

        // ========== 4. 业务类型推断 ==========
        String bizTypeValue = null;

        // 4.1 注解指定
        if (operationLog.bizType() != BizType.OTHER) {
            bizTypeValue = operationLog.bizType().getCode();
            log.info("业务类型 -> 注解 bizType: {}", bizTypeValue);
        } else if (StringUtils.hasText(operationLog.bizTypeCode())) {
            bizTypeValue = operationLog.bizTypeCode();
            log.info("业务类型 -> 注解 bizTypeCode: {}", bizTypeValue);
        }

        // 4.2 参数推断
        if (!StringUtils.hasText(bizTypeValue)) {
            bizTypeValue = inferBizTypeFromArgs(paramNames, args);
            if (StringUtils.hasText(bizTypeValue)) {
                log.info("业务类型 -> 参数推断: {}", bizTypeValue);
            }
        }

        // 4.3 字典表关键词匹配
        if (!StringUtils.hasText(bizTypeValue) && StringUtils.hasText(operationDesc)
                && properties.isUseDictTable()) {
            try {
                bizTypeValue = dictCacheManager.matchBizTypeByKeyword(operationDesc);
                if (StringUtils.hasText(bizTypeValue)) {
                    log.info("业务类型 -> 字典表匹配: {}", bizTypeValue);
                }
            } catch (Exception ex) {
                log.warn("字典表匹配业务类型失败", ex);
            }
        }

        // 4.4 自定义配置文件关键词匹配
        if (!StringUtils.hasText(bizTypeValue) && StringUtils.hasText(operationDesc)) {
            bizTypeValue = matchBizTypeByCustomKeywords(operationDesc);
            if (StringUtils.hasText(bizTypeValue)) {
                log.info("业务类型 -> 自定义关键词匹配: {}", bizTypeValue);
            }
        }

        // 最终兜底
        if (!StringUtils.hasText(bizTypeValue)) {
            bizTypeValue = BizType.OTHER.getCode();
            log.info("业务类型 -> 兜底为 OTHER");
        }
        bizTypeValue = truncate(bizTypeValue, MAX_BIZ_TYPE_LENGTH);
        log.info("最终业务类型: {}", bizTypeValue);

        // ========== 5. 子业务类型提取 ==========
        String subBizType = operationLog.subBizType();
        log.info("注解 subBizType: {}", subBizType);
        if (!StringUtils.hasText(subBizType)) {
            String subParam = properties.getDynamicParam().getSubBizParam();
            if (subParam != null && paramNames != null && args != null) {
                for (int i = 0; i < paramNames.length; i++) {
                    if (subParam.equals(paramNames[i])) {
                        Object val = args[i];
                        if (val != null) {
                            subBizType = val.toString();
                            log.info("从参数提取子业务类型: {}", subBizType);
                            break;
                        }
                    }
                }
            }
        }
        log.info("最终子业务类型: {}", subBizType);

        // ========== 5.5 订单号提取 ==========
        String orderNo = null;
        String orderName = operationLog.orderName();
        if (StringUtils.hasText(orderName) && paramNames != null && args != null) {
            for (int i = 0; i < paramNames.length; i++) {
                if (orderName.equals(paramNames[i])) {
                    Object val = args[i];
                    if (val != null) {
                        orderNo = val.toString();
                        log.info("注解 orderName={} -> 订单号: {}", orderName, orderNo);
                        break;
                    }
                }
            }
        }
        if (!StringUtils.hasText(orderNo)) {
            String orderNoParam = properties.getDynamicParam().getOrderNoParam();
            if (orderNoParam != null && paramNames != null && args != null) {
                for (int i = 0; i < paramNames.length; i++) {
                    if (orderNoParam.equals(paramNames[i])) {
                        Object val = args[i];
                        if (val != null) {
                            orderNo = val.toString();
                            log.info("配置 orderNoParam={} -> 订单号: {}", orderNoParam, orderNo);
                            break;
                        }
                    }
                }
            }
        }
        if (!StringUtils.hasText(orderNo)) {
            orderNo = extractOrderNoFromArgs(paramNames, args);
            if (StringUtils.hasText(orderNo)) {
                log.info("自动识别订单号: {}", orderNo);
            }
        }
        orderNo = truncate(orderNo, MAX_ORDER_NO_LENGTH);
        log.info("最终订单号: {}", orderNo);

        // ========== 6. 构造 TraceID ==========
        StringBuilder bizTraceIdBuilder = new StringBuilder()
                .append(orgCode).append("_")
                .append(userId).append("_")
                .append(loginId);
        if (StringUtils.hasText(bizTypeValue)) {
            bizTraceIdBuilder.append("_").append(bizTypeValue);
        }
        if (StringUtils.hasText(subBizType)) {
            bizTraceIdBuilder.append("_").append(subBizType);
        }
        String traceId = bizTraceIdBuilder.toString();
        log.info("生成 TraceID: {}", traceId);

        // ========== 7. 保存实体 ==========
        OperationLogEntity logEntity = new OperationLogEntity();
        logEntity.setTraceId(traceId);
        logEntity.setOrgCode(orgCode);
        logEntity.setUserId(userId);
        logEntity.setLoginId(loginId);
        logEntity.setBizTypeCode(bizTypeValue);
        logEntity.setSubBizCode(StringUtils.hasText(subBizType) ? subBizType : null);
        logEntity.setServiceType(serviceTypeCode);
        logEntity.setRequestUrl(requestUrl);
        logEntity.setLogLevel("INFO");
        logEntity.setOperation(operationDesc);
        logEntity.setApiName(truncate(apiName, MAX_API_NAME_LENGTH));
        logEntity.setRequestId(hisRequestId);
        logEntity.setResponseStatus(responseStatus);
        logEntity.setCostMs((int) costMs);
        logEntity.setOrderNo(StringUtils.hasText(orderNo) ? orderNo : null);
        logEntity.setCreatedAt(LocalDateTime.now());

        OperationLogDetailEntity detailEntity = new OperationLogDetailEntity();
        detailEntity.setRequestBody(processBody(args));
        detailEntity.setResponseBody(processBody(result));
        detailEntity.setErrorStack(extractErrorStack(throwable));
        detailEntity.setCreatedAt(LocalDateTime.now());

        operationLogService.saveAsync(logEntity, detailEntity);
        log.info("操作日志实体已提交");
    }

    // ==================== 辅助方法 ====================
    private String processBody(Object body) {
        String json = JsonUtil.toJson(body);
        if (!properties.isCompressBody() || !StringUtils.hasText(json)) return json;
        return gzipBase64(json);
    }

    private String gzipBase64(String value) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (GZIPOutputStream gzip = new GZIPOutputStream(bos)) {
            gzip.write(value.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            return value;
        }
        return Base64.getEncoder().encodeToString(bos.toByteArray());
    }

    private String extractErrorStack(Throwable throwable) {
        if (throwable == null) return null;
        StringWriter sw = new StringWriter();
        throwable.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }

    private ServletRequestAttributes currentRequestAttributes() {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        return (attributes instanceof ServletRequestAttributes) ? (ServletRequestAttributes) attributes : null;
    }

    private String truncate(String value, int maxLength) {
        if (value != null && value.length() > maxLength) {
            return value.substring(0, maxLength);
        }
        return value;
    }

    private boolean isLikelyApiName(String value) {
        return value.contains(".") || value.contains("HIS") || value.matches(".*[A-Z]+\\..*");
    }

    private boolean isLikelyDescribe(String value) {
        return value.length() > 3 && !value.contains(".") && value.matches(".*[\\u4e00-\\u9fa5]+.*");
    }

    private String inferBizTypeFromArgs(String[] paramNames, Object[] args) {
        if (paramNames != null && args != null && paramNames.length == args.length) {
            for (int i = 0; i < paramNames.length; i++) {
                if (args[i] == null) continue;
                String name = paramNames[i];
                if ("bizType".equals(name) || "type".equals(name) || "businessType".equals(name)) {
                    String val = normalizeBizTypeValue(String.valueOf(args[i]));
                    if (StringUtils.hasText(val)) return val;
                }
            }
        }
        if (args != null) {
            for (Object arg : args) {
                if (arg instanceof String) {
                    String val = normalizeBizTypeValue((String) arg);
                    if (StringUtils.hasText(val)) return val;
                }
            }
        }
        return null;
    }

    /**
     * 仅当值可精确匹配 BizType 枚举（非 OTHER）时返回编码，否则返回 null。
     */
    private String normalizeBizTypeValue(String value) {
        if (!StringUtils.hasText(value)) return null;
        BizType bizType = BizType.fromCode(value);
        return bizType != BizType.OTHER ? bizType.getCode() : null;
    }

    private String extractOrderNoFromArgs(String[] paramNames, Object[] args) {
        if (paramNames != null && args != null && paramNames.length == args.length) {
            for (int i = 0; i < paramNames.length; i++) {
                if (args[i] == null) continue;
                String name = paramNames[i].toLowerCase();
                if (name.contains("orderno") || name.contains("orderid") || 
                    name.contains("tradeno") || name.contains("transactionno") ||
                    name.contains("order_no") || name.contains("order_id") ||
                    name.contains("trade_no") || name.contains("transaction_no")) {
                    String val = String.valueOf(args[i]);
                    if (isValidOrderNo(val)) {
                        return val;
                    }
                }
            }
        }
        if (args != null) {
            for (Object arg : args) {
                if (arg instanceof String) {
                    String val = (String) arg;
                    if (isValidOrderNo(val)) {
                        return val;
                    }
                }
            }
        }
        return null;
    }

    private boolean isValidOrderNo(String value) {
        if (!StringUtils.hasText(value)) return false;
        String trimmed = value.trim();
        if (trimmed.length() < 8 || trimmed.length() > 64) return false;
        return ORDER_NO_PATTERN.matcher(trimmed).matches() && HAS_DIGIT_PATTERN.matcher(trimmed).find();
    }

    /**
     * 从自定义配置文件关键词中匹配业务类型。
     */
    private String matchBizTypeByCustomKeywords(String text) {
        if (!StringUtils.hasText(text)) return null;
        String lower = text.toLowerCase();
        Map<BizType, List<String>> custom = properties.getBizTypeKeywords();
        if (custom != null && !custom.isEmpty()) {
            log.info("尝试自定义关键词匹配, 描述: {}", lower);
            for (Map.Entry<BizType, List<String>> entry : custom.entrySet()) {
                if (entry.getKey() == null || entry.getValue() == null) continue;
                for (String kw : entry.getValue()) {
                    if (StringUtils.hasText(kw) && lower.contains(kw.toLowerCase())) {
                        log.info("自定义关键词命中: {} -> {}", kw, entry.getKey().getCode());
                        return entry.getKey().getCode();
                    }
                }
            }
            log.info("自定义关键词未命中");
        }
        return null;
    }

    private Object invokeGetter(Object obj, String fieldName) {
        try {
            String getter = "get" + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
            Method method = obj.getClass().getMethod(getter);
            return method.invoke(obj);
        } catch (Exception e) {
            return null;
        }
    }
}
