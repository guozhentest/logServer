package com.leantech.hislog.annotation;

import java.lang.annotation.*;

/**
 * 标记需要自动记录 HIS 调用日志的方法
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface HisApiLog {

    /**
     * 操作描述
     */
    String value() default "";

    /**
     * 业务类型编码（如 REG、PAY）
     */
    String bizType() default "";

    /**
     * 业务子类编码（可选）
     */
    String subBizType() default "";
}