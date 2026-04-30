package com.leantech.oplog.annotation;

import com.leantech.oplog.enums.BizType;
import com.leantech.oplog.enums.ServiceType;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OperationLog {

    String value() default "";

    BizType bizType() default BizType.OTHER;

    String bizTypeCode() default "";

    String subBizType() default "";

    ServiceType serviceType() default ServiceType.OTHER;
}
