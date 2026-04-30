package com.leantech.hislog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.leantech.hislog.entity.UserOperationLogDetailEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DetailMapper extends BaseMapper<UserOperationLogDetailEntity> {
}