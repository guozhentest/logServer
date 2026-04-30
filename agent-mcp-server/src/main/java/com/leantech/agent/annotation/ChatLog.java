package com.leantech.agent.annotation;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ChatLog {
    /** 是否保存到 MongoDB 聊天记忆 */
    boolean saveMemory() default true;
    /** 是否保存到 MySQL 审计记录 */
    boolean saveHistory() default true;
}