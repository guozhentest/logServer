CREATE DATABASE IF NOT EXISTS agent_admin DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE agent_admin;

CREATE TABLE IF NOT EXISTS sys_user (
    user_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_name VARCHAR(30) NOT NULL COMMENT '用户账号',
    nick_name VARCHAR(30) DEFAULT '' COMMENT '用户昵称',
    password VARCHAR(200) DEFAULT '' COMMENT '密码',
    email VARCHAR(50) DEFAULT '' COMMENT '邮箱',
    phonenumber VARCHAR(11) DEFAULT '' COMMENT '手机号',
    sex CHAR(1) DEFAULT '0' COMMENT '性别 0男 1女 2未知',
    avatar VARCHAR(200) DEFAULT '' COMMENT '头像',
    status CHAR(1) DEFAULT '0' COMMENT '状态 0正常 1停用',
    del_flag CHAR(1) DEFAULT '0' COMMENT '删除标志 0正常 2删除',
    remark VARCHAR(500) DEFAULT '' COMMENT '备注',
    login_ip VARCHAR(128) DEFAULT '' COMMENT '最后登录IP',
    login_date DATETIME COMMENT '最后登录时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY idx_user_name (user_name)
) ENGINE=InnoDB COMMENT='系统用户表';

INSERT INTO sys_user (user_name, nick_name, password, status) VALUES
('admin', '超级管理员', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '0');

CREATE TABLE IF NOT EXISTS sys_role (
    role_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    role_name VARCHAR(30) NOT NULL COMMENT '角色名称',
    role_key VARCHAR(100) NOT NULL COMMENT '角色权限字符串',
    role_sort INT DEFAULT 0 COMMENT '显示顺序',
    status CHAR(1) DEFAULT '0' COMMENT '状态 0正常 1停用',
    del_flag CHAR(1) DEFAULT '0' COMMENT '删除标志 0正常 2删除',
    remark VARCHAR(500) DEFAULT '' COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY idx_role_name (role_name),
    UNIQUE KEY idx_role_key (role_key)
) ENGINE=InnoDB COMMENT='系统角色表';

INSERT INTO sys_role (role_name, role_key, role_sort) VALUES
('超级管理员', 'admin', 1),
('普通用户', 'common', 2);

CREATE TABLE IF NOT EXISTS sys_user_role (
    user_id BIGINT NOT NULL COMMENT '用户ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    PRIMARY KEY (user_id, role_id)
) ENGINE=InnoDB COMMENT='用户角色关联表';

INSERT INTO sys_user_role (user_id, role_id) VALUES (1, 1);

CREATE TABLE IF NOT EXISTS sys_menu (
    menu_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    menu_name VARCHAR(50) NOT NULL COMMENT '菜单名称',
    parent_id BIGINT DEFAULT 0 COMMENT '父菜单ID',
    order_num INT DEFAULT 0 COMMENT '显示顺序',
    path VARCHAR(200) DEFAULT '' COMMENT '路由地址',
    component VARCHAR(255) DEFAULT NULL COMMENT '组件路径',
    query VARCHAR(255) DEFAULT '' COMMENT '路由参数',
    is_frame VARCHAR(1) DEFAULT '1' COMMENT '是否外链',
    is_cache VARCHAR(1) DEFAULT '0' COMMENT '是否缓存',
    menu_type VARCHAR(1) DEFAULT '' COMMENT '菜单类型 M目录 C菜单 F按钮',
    visible VARCHAR(1) DEFAULT '0' COMMENT '显示状态 0显示 1隐藏',
    status VARCHAR(1) DEFAULT '0' COMMENT '状态 0正常 1停用',
    perms VARCHAR(100) DEFAULT '' COMMENT '权限标识',
    icon VARCHAR(100) DEFAULT '#' COMMENT '菜单图标',
    remark VARCHAR(500) DEFAULT '' COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB COMMENT='系统菜单表';

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, menu_type, icon, perms) VALUES
(1, '系统管理', 0, 1, 'system', NULL, 'M', 'system', ''),
(2, '用户管理', 1, 1, 'user', 'system/user/index', 'C', 'user', 'system:user:list'),
(3, '角色管理', 1, 2, 'role', 'system/role/index', 'C', 'peoples', 'system:role:list'),
(4, '菜单管理', 1, 3, 'menu', 'system/menu/index', 'C', 'tree-table', 'system:menu:list'),
(10, '智能中心', 0, 2, 'agent', NULL, 'M', 'server', ''),
(11, '在线医院', 10, 1, 'hospital', 'agent/hospital/index', 'C', 'build', 'agent:hospital:list'),
(12, '日志查询', 10, 2, 'log', 'agent/log/index', 'C', 'log', 'agent:log:list'),
(13, '日志明细', 10, 3, 'log-detail', 'agent/log/detail', 'C', 'documentation', 'agent:log:query');

CREATE TABLE IF NOT EXISTS sys_role_menu (
    role_id BIGINT NOT NULL COMMENT '角色ID',
    menu_id BIGINT NOT NULL COMMENT '菜单ID',
    PRIMARY KEY (role_id, menu_id)
) ENGINE=InnoDB COMMENT='角色菜单关联表';

INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(1, 1), (1, 2), (1, 3), (1, 4), (1, 10), (1, 11), (1, 12), (1, 13);
