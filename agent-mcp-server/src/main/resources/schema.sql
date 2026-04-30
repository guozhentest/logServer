-- 机构信息表
CREATE TABLE IF NOT EXISTS agent_hospital_info (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    org_code VARCHAR(50) NOT NULL COMMENT '机构代码',
    org_name VARCHAR(100) NOT NULL COMMENT '机构名称',
    base_url VARCHAR(200) NOT NULL COMMENT '接口基础地址（http://ip:port）',
    api_key VARCHAR(64) DEFAULT NULL COMMENT 'API签名密钥',
    status TINYINT DEFAULT 1 COMMENT '状态 1-启用 0-禁用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_org_code (org_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='机构信息表';

-- 业务类型字典表（中心版）
CREATE TABLE IF NOT EXISTS agent_biz_type_dict (
    id BIGINT NOT NULL AUTO_INCREMENT,
    org_code VARCHAR(50) NOT NULL COMMENT '机构代码，* 表示全局通用',
    type_code VARCHAR(20) NOT NULL,
    type_name VARCHAR(50) NOT NULL,
    keywords TEXT COMMENT '匹配关键词，JSON数组',
    is_enabled TINYINT DEFAULT 1,
    sort_order INT DEFAULT 0 COMMENT '优先级，越小越高',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_org_type (org_code, type_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='业务类型字典（中心版）';

-- 服务类型字典表（中心版）
CREATE TABLE IF NOT EXISTS agent_service_type_dict (
    id BIGINT NOT NULL AUTO_INCREMENT,
    org_code VARCHAR(50) NOT NULL COMMENT '机构代码，* 表示全局通用',
    type_code VARCHAR(20) NOT NULL,
    type_name VARCHAR(50) NOT NULL,
    class_keywords TEXT COMMENT '匹配类名关键词，JSON数组',
    is_enabled TINYINT DEFAULT 1,
    sort_order INT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_org_type (org_code, type_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='服务类型字典（中心版）';

CREATE TABLE IF NOT EXISTS agent_chat_history (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    conversation_id VARCHAR(64) NOT NULL COMMENT '会话ID',
    user_question TEXT NOT NULL COMMENT '用户原始问题',
    assistant_answer TEXT COMMENT '助手最终回答',
    model_name VARCHAR(50) COMMENT '使用的模型名称',
    total_duration_ms INT COMMENT '总耗时(毫秒)',
    tokens_input INT DEFAULT 0 COMMENT '输入token数',
    tokens_output INT DEFAULT 0 COMMENT '输出token数',
    status VARCHAR(20) DEFAULT 'success' COMMENT '状态: success / error',
    error_message TEXT COMMENT '错误信息',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_conversation_id (conversation_id),
    KEY idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Agent聊天历史记录表';

CREATE TABLE IF NOT EXISTS agent_feedback (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    conversation_id VARCHAR(64) NOT NULL COMMENT '会话ID',
    user_question TEXT COMMENT '用户原始问题',
    assistant_answer TEXT COMMENT '助手回答',
    rating TINYINT COMMENT '用户评分 1-5',
    is_adopted BOOLEAN DEFAULT FALSE COMMENT '是否已被采纳为训练语料',
    tags JSON COMMENT '标签，如["挂号","失败","准确"]',
    comment TEXT COMMENT '用户补充说明',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_conversation_id (conversation_id),
    KEY idx_rating (rating)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Agent反馈记录表';

-- 初始化机构（示例）
INSERT INTO agent_hospital_info (org_code, org_name, base_url, api_key) VALUES
('HOS01', '阜阳市肿瘤医院', 'http://192.168.1.101:8080', 'your-api-key')
ON DUPLICATE KEY UPDATE org_name = VALUES(org_name), base_url = VALUES(base_url);

-- 全局默认业务类型（org_code = '*'）
INSERT INTO agent_biz_type_dict (org_code, type_code, type_name, keywords, is_enabled, sort_order) VALUES
('*', 'ONLINE', '在线问诊', '["问诊","在线问诊","图文问诊","视频问诊","病历","病例","诊断","医嘱"]', 1, 0),
('*', 'REG', '挂号', '["挂号","排班","号源","科室"]', 1, 1),
('*', 'PAY', '缴费', '["缴费","支付","收银","费用"]', 1, 2),
('*', 'CLI', '实名认证/门诊', '["建档","查卡","实名","绑卡"]', 1, 3),
('*', 'DRUG', '药房', '["购药","发药","退药","处方","药品"]', 1, 4),
('*', 'EXA', '检查检验', '["报告","检验","检查"]', 1, 5),
('*', 'INP', '住院', '["住院","入院","出院","转科"]', 1, 6),
('*', 'SYS', '系统管理', '[]', 1, 98),
('*', 'OTHER', '其他', '[]', 1, 99)
ON DUPLICATE KEY UPDATE
    type_name = VALUES(type_name),
    keywords = VALUES(keywords),
    is_enabled = VALUES(is_enabled),
    sort_order = VALUES(sort_order);

-- 全局默认服务类型（org_code = '*'）
INSERT INTO agent_service_type_dict (org_code, type_code, type_name, class_keywords, is_enabled, sort_order) VALUES
('*', 'HIS', 'HIS服务', '["com.leantech.impl.his."]', 1, 1),
('*', 'TH_API', 'TH-API服务', '["com.leantech.service.th."]', 1, 2),
('*', 'IM', 'IM服务', '["com.leantech.service.defaultimpl.im."]', 1, 3),
('*', 'THIRD', '第三方服务', '["com.leantech.service.third."]', 1, 4),
('*', 'MSG', '消息服务', '["com.leantech.message."]', 1, 5),
('*', 'SUPERVISE', '监管服务', '["com.leantech.supervise."]', 1, 6),
('*', 'TIMER', '定时任务', '["com.leantech.impl.timmer.", "com.leantech.timer."]', 1, 7),
('*', 'SYS', '系统内部任务', '[]', 1, 98),
('*', 'OTHER', '其他服务', '[]', 1, 99)
ON DUPLICATE KEY UPDATE
    type_name = VALUES(type_name),
    class_keywords = VALUES(class_keywords),
    is_enabled = VALUES(is_enabled),
    sort_order = VALUES(sort_order);

CREATE TABLE IF NOT EXISTS agent_user (
                                          id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                          username VARCHAR(50) NOT NULL UNIQUE,
                                          password VARCHAR(100) NOT NULL,
                                          role VARCHAR(20) DEFAULT 'OPERATOR',
                                          enabled TINYINT DEFAULT 1,
                                          create_time DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 默认管理员账号 admin / admin123
INSERT INTO agent_user (username, password, role) VALUES ('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EHsM8', 'ADMIN');