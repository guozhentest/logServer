package com.leantech.hislog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.leantech.hislog.entity.UserOperationLogEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface LogMapper extends BaseMapper<UserOperationLogEntity> {
}