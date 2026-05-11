package com.leantech.oplog.service;

import com.leantech.oplog.entity.OperationLogDetailEntity;
import com.leantech.oplog.entity.OperationLogEntity;
import com.leantech.oplog.model.LogQueryRequest;
import com.leantech.oplog.model.LogQueryResponse;
import com.leantech.oplog.model.LogSummaryVO;

import java.util.Map;

public interface OperationLogService {

    Long save(OperationLogEntity logEntity, OperationLogDetailEntity detailEntity);

    void saveAsync(OperationLogEntity logEntity, OperationLogDetailEntity detailEntity);

    LogQueryResponse.PageData<LogSummaryVO> queryLogs(LogQueryRequest request, int maxLimit, int truncateLength);

    Map<String, Object> queryDetail(String logId);
}
