-- 主表
CREATE TABLE IF NOT EXISTS `user_operation_log` (
                                                    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
                                                    `trace_id` VARCHAR(200) NOT NULL COMMENT '业务语义化TraceID',
                                                    `org_code` VARCHAR(50) NOT NULL COMMENT '机构代码',
                                                    `user_id` VARCHAR(50) NOT NULL COMMENT '用户工号',
                                                    `login_id` VARCHAR(50) NOT NULL COMMENT '登录会话ID',
                                                    `biz_type_code` VARCHAR(10) DEFAULT NULL COMMENT '业务类型编码',
                                                    `sub_biz_code` VARCHAR(10) DEFAULT NULL COMMENT '业务子类编码',
                                                    `log_level` VARCHAR(10) NOT NULL DEFAULT 'INFO' COMMENT '日志级别',
                                                    `operation` VARCHAR(200) DEFAULT NULL COMMENT '操作摘要',
                                                    `his_api_name` VARCHAR(200) DEFAULT NULL COMMENT 'HIS接口名称',
                                                    `his_request_id` VARCHAR(100) DEFAULT NULL COMMENT 'HIS请求流水号',
                                                    `response_status` VARCHAR(20) DEFAULT NULL COMMENT '响应状态',
                                                    `cost_ms` INT DEFAULT 0 COMMENT '耗时(毫秒)',
                                                    `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
                                                    `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
                                                    PRIMARY KEY (`id`),
                                                    KEY `idx_trace_id` (`trace_id`(100)),
                                                    KEY `idx_org_user_login` (`org_code`, `user_id`, `login_id`),
                                                    KEY `idx_biz_type` (`biz_type_code`),
                                                    KEY `idx_response_status` (`response_status`),
                                                    KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户操作日志主表';

-- 详情表
CREATE TABLE IF NOT EXISTS `user_operation_log_detail` (
                                                           `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
                                                           `log_id` BIGINT NOT NULL COMMENT '关联主表ID',
                                                           `request_body` LONGTEXT COMMENT '请求体',
                                                           `response_body` LONGTEXT COMMENT '响应体',
                                                           `error_stack` TEXT COMMENT '异常堆栈',
                                                           `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
                                                           PRIMARY KEY (`id`),
                                                           KEY `idx_log_id` (`log_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户操作日志详情表';