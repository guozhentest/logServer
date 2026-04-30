package com.leantech.hislog.service.impl;

import com.leantech.hislog.entity.UserOperationLogDetailEntity;
import com.leantech.hislog.entity.UserOperationLogEntity;
import com.leantech.hislog.mapper.DetailMapper;
import com.leantech.hislog.mapper.LogMapper;
import com.leantech.hislog.service.OperationLogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.annotation.Transactional;

public class OperationLogServiceImpl implements OperationLogService { // 移除 @Service

    private static final Logger log = LoggerFactory.getLogger(OperationLogServiceImpl.class);

    private final LogMapper logMapper;
    private final DetailMapper detailMapper;

    public OperationLogServiceImpl(LogMapper logMapper, DetailMapper detailMapper) {
        this.logMapper = logMapper;
        this.detailMapper = detailMapper;
    }

    @Override
    @Transactional
    public Long save(UserOperationLogEntity logEntity, UserOperationLogDetailEntity detailEntity) {
        try {
            logMapper.insert(logEntity);
            if (detailEntity != null) {
                detailEntity.setLogId(logEntity.getId());
                detailMapper.insert(detailEntity);
            }
            return logEntity.getId();
        } catch (Exception e) {
            log.error("保存操作日志失败", e);
            throw new RuntimeException("保存操作日志失败", e);
        }
    }

    @Override
    @Async("hisLogAsyncExecutor")
    public void saveAsync(UserOperationLogEntity logEntity, UserOperationLogDetailEntity detailEntity) {
        save(logEntity, detailEntity);
    }
}