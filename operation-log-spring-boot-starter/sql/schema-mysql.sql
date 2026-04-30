-- ============================================================
-- 操作日志组件建表脚本（MySQL）
-- 包含主表、详情表、业务类型字典表、服务类型字典表及初始数据
-- 可重复执行，数据冲突时自动更新
-- ============================================================

-- 1. 主表
CREATE TABLE IF NOT EXISTS `user_operation_log` (
                                                    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
                                                    `trace_id` VARCHAR(200) NOT NULL COMMENT '业务语义化TraceID',
                                                    `org_code` VARCHAR(50) NOT NULL COMMENT '机构代码',
                                                    `user_id` VARCHAR(50) NOT NULL COMMENT '用户工号',
                                                    `login_id` VARCHAR(50) NOT NULL COMMENT '登录会话ID',
                                                    `biz_type_code` VARCHAR(10) DEFAULT NULL COMMENT '业务类型编码',
                                                    `sub_biz_code` VARCHAR(10) DEFAULT NULL COMMENT '业务子类编码',
                                                    `service_type` VARCHAR(20) DEFAULT 'HIS' COMMENT '服务类型',
                                                    `request_url` VARCHAR(500) DEFAULT NULL COMMENT '请求地址',
                                                    `log_level` VARCHAR(10) NOT NULL DEFAULT 'INFO' COMMENT '日志级别',
                                                    `operation` VARCHAR(200) DEFAULT NULL COMMENT '操作摘要',
                                                    `his_api_name` VARCHAR(200) DEFAULT NULL COMMENT '接口名称',
                                                    `his_request_id` VARCHAR(100) DEFAULT NULL COMMENT '请求流水号',
                                                    `response_status` VARCHAR(20) DEFAULT NULL COMMENT '响应状态',
                                                    `cost_ms` INT DEFAULT 0 COMMENT '耗时(毫秒)',
                                                    `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
                                                    `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
                                                    PRIMARY KEY (`id`),
                                                    KEY `idx_trace_id` (`trace_id`(100)),
                                                    KEY `idx_org_user_login` (`org_code`, `user_id`, `login_id`),
                                                    KEY `idx_biz_type` (`biz_type_code`),
                                                    KEY `idx_service_type` (`service_type`),
                                                    KEY `idx_response_status` (`response_status`),
                                                    KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户操作日志主表';

-- 2. 详情表
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

-- 3. 业务类型字典表
CREATE TABLE IF NOT EXISTS `biz_type_dict` (
                                               `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
                                               `type_code` VARCHAR(20) NOT NULL COMMENT '类型编码',
                                               `type_name` VARCHAR(50) NOT NULL COMMENT '类型名称',
                                               `keywords` TEXT COMMENT '匹配关键词，JSON数组格式',
                                               `is_enabled` TINYINT DEFAULT 1 COMMENT '是否启用',
                                               `sort_order` INT DEFAULT 0 COMMENT '排序',
                                               `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
                                               `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                               PRIMARY KEY (`id`),
                                               UNIQUE KEY `uk_type_code` (`type_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='业务类型字典';

-- 4. 服务类型字典表
CREATE TABLE IF NOT EXISTS `service_type_dict` (
                                                   `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
                                                   `type_code` VARCHAR(20) NOT NULL COMMENT '类型编码',
                                                   `type_name` VARCHAR(50) NOT NULL COMMENT '类型名称',
                                                   `class_keywords` TEXT COMMENT '匹配类名关键词，JSON数组格式',
                                                   `is_enabled` TINYINT DEFAULT 1 COMMENT '是否启用',
                                                   `sort_order` INT DEFAULT 0 COMMENT '排序',
                                                   `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
                                                   `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                                   PRIMARY KEY (`id`),
                                                   UNIQUE KEY `uk_type_code` (`type_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='服务类型字典';

-- 5. 初始化业务类型数据（带冲突更新）
INSERT INTO biz_type_dict (type_code, type_name, keywords, is_enabled, sort_order) VALUES
                                                                                       ('REG', '挂号', '["挂号","排班","号源","科室"]', 1, 1),
                                                                                       ('PAY', '缴费', '["缴费","支付","收银","费用"]', 1, 2),
                                                                                       ('CLI', '实名认证/门诊', '["建档","查卡","实名","绑卡"]', 1, 3),
                                                                                       ('ONLINE', '在线问诊', '["问诊","在线问诊","图文问诊","视频问诊","病历","病例","诊断","医嘱"]', 1, 4),
                                                                                       ('INP', '住院', '["住院","入院","出院","转科"]', 1, 5),
                                                                                       ('DRUG', '药房', '["购药","发药","退药","处方","药品"]', 1, 6),
                                                                                       ('EXA', '检查检验', '["报告","检验","检查"]', 1, 7),
                                                                                       ('SYS', '系统管理', '[]', 1, 98),
                                                                                       ('OTHER', '其他', '[]', 1, 99)
ON DUPLICATE KEY UPDATE
                     `type_name` = VALUES(`type_name`),
                     `keywords` = VALUES(`keywords`),
                     `is_enabled` = VALUES(`is_enabled`),
                     `sort_order` = VALUES(`sort_order`);

-- 6. 初始化服务类型数据（带冲突更新，关键词使用包路径前缀）
INSERT INTO service_type_dict (type_code, type_name, class_keywords, is_enabled, sort_order) VALUES
                                                                                                 ('HIS', 'HIS服务', '["com.leantech.impl.his."]', 1, 1),
                                                                                                 ('TH_API', 'TH-API服务', '["com.leantech.service.th."]', 1, 2),
                                                                                                 ('IM', 'IM服务', '["com.leantech.service.defaultimpl.im."]', 1, 3),
                                                                                                 ('THIRD', '第三方服务', '["com.leantech.service.third."]', 1, 4),
                                                                                                 ('MSG', '消息服务', '["com.leantech.message."]', 1, 5),
                                                                                                 ('SUPERVISE', '监管服务', '["com.leantech.supervise."]', 1, 6),
                                                                                                 ('TIMER', '定时任务', '["com.leantech.impl.timmer.", "com.leantech.timer."]', 1, 7),
                                                                                                 ('SYS', '系统内部任务', '[]', 1, 98),
                                                                                                 ('OTHER', '其他服务', '[]', 1, 99)
ON DUPLICATE KEY UPDATE
                     `type_name` = VALUES(`type_name`),
                     `class_keywords` = VALUES(`class_keywords`),
                     `is_enabled` = VALUES(`is_enabled`),
                     `sort_order` = VALUES(`sort_order`);