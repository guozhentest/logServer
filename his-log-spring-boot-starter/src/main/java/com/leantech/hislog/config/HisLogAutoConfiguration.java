package com.leantech.hislog.config;

import com.leantech.hislog.aspect.HisApiLogAspect;
import com.leantech.hislog.mapper.DetailMapper;
import com.leantech.hislog.mapper.LogMapper;
import com.leantech.hislog.service.OperationLogService;
import com.leantech.hislog.service.impl.OperationLogServiceImpl;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
@EnableAspectJAutoProxy
@EnableAsync
@EnableConfigurationProperties(HisLogProperties.class)
@MapperScan(basePackages = "com.leantech.hislog.mapper")
public class HisLogAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(value = OperationLogService.class)
    public OperationLogService operationLogService(LogMapper logMapper, DetailMapper detailMapper) {
        return new OperationLogServiceImpl(logMapper, detailMapper);
    }

    @Bean
    @ConditionalOnMissingBean(value = HisApiLogAspect.class)
    public HisApiLogAspect hisApiLogAspect(OperationLogService operationLogService) {
        return new HisApiLogAspect(operationLogService);
    }

    @Bean(name = "hisLogAsyncExecutor")
    public Executor hisLogAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(20);
        executor.setQueueCapacity(500);
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setThreadNamePrefix("his-log-async-");
        executor.initialize();
        return executor;
    }
}