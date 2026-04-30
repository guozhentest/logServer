package com.leantech.hislog.service;

import com.leantech.hislog.entity.UserOperationLogDetailEntity;
import com.leantech.hislog.entity.UserOperationLogEntity;

public interface OperationLogService {

    /**
     * 同步保存日志
     */
    Long save(UserOperationLogEntity logEntity, UserOperationLogDetailEntity detailEntity);

    /**
     * 异步保存日志
     */
    void saveAsync(UserOperationLogEntity logEntity, UserOperationLogDetailEntity detailEntity);
}