package com.leantech.oplog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.leantech.oplog.entity.OperationLogDetailEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DetailMapper extends BaseMapper<OperationLogDetailEntity> {
}
