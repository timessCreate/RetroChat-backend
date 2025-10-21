-- 用户表
CREATE TABLE `user` (
                         `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
                         `username` VARCHAR(50) UNIQUE NOT NULL,
                         `password` CHAR(60) NOT NULL COMMENT 'BCrypt加密固定60字符',
                         `email` VARCHAR(100) UNIQUE,
                         `phone` VARCHAR(20) UNIQUE,
                         user_avatar    varchar(1024)  null comment '用户头像',
                         user_profile   varchar(512)   null comment '用户简介',
                         `status` TINYINT DEFAULT 1 COMMENT '1-正常 0-禁用',
                         `last_login` DATETIME,
                         `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
                         `update_time` DATETIME ON UPDATE CURRENT_TIMESTAMP,
                         isDelete  tinyint  default 0 not null comment '是否删除'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 角色表
CREATE TABLE `role` (
                         `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
                         `name` VARCHAR(50) UNIQUE NOT NULL COMMENT '角色名称',
                         `code` VARCHAR(50) UNIQUE NOT NULL COMMENT '角色编码(ROLE_ADMIN)',
                         `description` VARCHAR(200) COMMENT '角色描述',
                         `status` TINYINT DEFAULT 1 COMMENT '1-启用 0-禁用',
                         `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
                         `update_time` DATETIME ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 权限表
CREATE TABLE `permission` (
                               `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
                               `name` VARCHAR(50) NOT NULL COMMENT '权限名称',
                               `code` VARCHAR(100) UNIQUE NOT NULL COMMENT '权限标识符(system:user:create)',
                               `type` TINYINT NOT NULL COMMENT '1-菜单 2-按钮 3-API',
                               `path` VARCHAR(200) COMMENT '前端路由路径',
                               `component` VARCHAR(100) COMMENT '前端组件',
                               `icon` VARCHAR(50) COMMENT '图标',
                               `parent_id` BIGINT DEFAULT 0 COMMENT '父权限ID',
                               `order_num` INT DEFAULT 0 COMMENT '排序号',
                               `status` TINYINT DEFAULT 1 COMMENT '1-启用 0-禁用',
                               `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
                               `update_time` DATETIME ON UPDATE CURRENT_TIMESTAMP,
                               INDEX `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 用户角色关联表
CREATE TABLE `user_role` (
                              `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
                              `user_id` BIGINT NOT NULL,
                              `role_id` BIGINT NOT NULL,
                              `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
                              UNIQUE KEY `uk_user_role` (`user_id`, `role_id`),
                              FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
                              FOREIGN KEY (`role_id`) REFERENCES `role` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 角色权限关联表
CREATE TABLE `role_permissions` (
                                    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
                                    `role_id` BIGINT NOT NULL,
                                    `permission_id` BIGINT NOT NULL,
                                    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
                                    UNIQUE KEY `uk_role_permission` (`role_id`, `permission_id`),
                                    FOREIGN KEY (`role_id`) REFERENCES `role` (`id`) ON DELETE CASCADE,
                                    FOREIGN KEY (`permission_id`) REFERENCES `permission` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- 登录日志表
CREATE TABLE `login_log` (
                              `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
                              `user_id` BIGINT,
                              `ip` VARCHAR(45) COMMENT 'IPv4/IPv6地址',
                              `device` VARCHAR(200) COMMENT '设备信息',
                              `os` VARCHAR(50) COMMENT '操作系统',
                              `browser` VARCHAR(50) COMMENT '浏览器类型',
                              `status` TINYINT COMMENT '1-成功 0-失败',
                              `location` VARCHAR(100) COMMENT 'IP地理位置',
                              `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
                              INDEX `idx_user_id` (`user_id`),
                              INDEX `idx_created_time` (`created_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

