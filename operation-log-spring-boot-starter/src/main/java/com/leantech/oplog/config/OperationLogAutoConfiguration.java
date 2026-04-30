package com.leantech.oplog.config;

import com.leantech.oplog.agent.AgentRegisterRunner;
import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.leantech.oplog.aspect.OperationLogAspect;
import com.leantech.oplog.cache.DictCacheManager;
import com.leantech.oplog.mapper.BizTypeDictMapper;
import com.leantech.oplog.mapper.DetailMapper;
import com.leantech.oplog.mapper.LogMapper;
import com.leantech.oplog.mapper.ServiceTypeDictMapper;
import com.leantech.oplog.service.OperationLogService;
import com.leantech.oplog.service.impl.OperationLogServiceImpl;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
@EnableAspectJAutoProxy
@EnableAsync
@EnableConfigurationProperties({OperationLogProperties.class, AgentRegisterProperties.class})
@MapperScan(basePackages = "com.leantech.oplog.mapper")
public class OperationLogAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(value = OperationLogService.class)
    public OperationLogService operationLogService(LogMapper logMapper, DetailMapper detailMapper, JdbcTemplate jdbcTemplate) {
        return new OperationLogServiceImpl(logMapper, detailMapper, jdbcTemplate);
    }

    @Bean
    @ConditionalOnMissingBean(value = DictCacheManager.class)
    public DictCacheManager dictCacheManager(BizTypeDictMapper bizTypeDictMapper,
                                             ServiceTypeDictMapper serviceTypeDictMapper) {
        return new DictCacheManager(bizTypeDictMapper, serviceTypeDictMapper);
    }

    @Bean
    @ConditionalOnMissingBean(value = OperationLogAspect.class)
    public OperationLogAspect operationLogAspect(OperationLogService operationLogService,
                                                 OperationLogProperties properties,
                                                 DictCacheManager dictCacheManager) {
        return new OperationLogAspect(operationLogService, properties, dictCacheManager);
    }


    @Bean(name = "mybatisPlusInterceptorForLog")
    @Primary
    @ConditionalOnMissingBean(MybatisPlusInterceptor.class)
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }
    @Bean(name = "operationLogAsyncExecutor")
    public Executor operationLogAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(20);
        executor.setQueueCapacity(500);
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setThreadNamePrefix("oplog-async-");
        executor.initialize();
        return executor;
    }

    @Bean
    @ConditionalOnMissingBean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    @ConditionalOnMissingBean(name = "operationLogAgentRegisterRunner")
    public ApplicationRunner operationLogAgentRegisterRunner(AgentRegisterProperties properties,
                                                             OperationLogProperties opLogProperties,
                                                             JdbcTemplate jdbcTemplate,
                                                             RestTemplate restTemplate,
                                                             @org.springframework.beans.factory.annotation.Qualifier("operationLogAsyncExecutor")
                                                             Executor executor) {
        return new AgentRegisterRunner(properties, opLogProperties, jdbcTemplate, restTemplate, executor);
    }


}
