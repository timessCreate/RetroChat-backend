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
                         is_delete  tinyint  default 0 not null comment '是否删除'
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

-- 聊天室表
CREATE TABLE chat_room (
                           id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '聊天室ID',
                           name VARCHAR(200) NOT NULL COMMENT '聊天室名称',
                           description TEXT NULL COMMENT '聊天室描述',
                           type TINYINT NOT NULL DEFAULT 1 COMMENT '聊天室类型：1-公开群聊 2-私密群聊 3-一对一私聊',
                           owner_id BIGINT NOT NULL COMMENT '创建者用户ID',
                           max_members INT DEFAULT 500 COMMENT '最大成员数',
                           current_members INT DEFAULT 1 COMMENT '当前成员数',
                           avatar_url VARCHAR(500) NULL COMMENT '聊天室头像',
                           is_active TINYINT DEFAULT 1 COMMENT '是否活跃：0-已解散 1-活跃',
                           last_message_id BIGINT NULL COMMENT '最后一条消息ID',
                           last_message_content TEXT NULL COMMENT '最后一条消息内容（冗余）',
                           last_activity_time DATETIME(3) NOT NULL COMMENT '最后活动时间',
                           create_time DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
                           update_time DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
                           is_delete tinyint default 0 not null  comment '是否删除',

                           INDEX idx_owner_id (owner_id),
                           INDEX idx_type (type),
                           INDEX idx_last_activity_time (last_activity_time),
                           INDEX idx_is_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='聊天室表';

-- 聊天室成员表
CREATE TABLE chat_room_member (
                                  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
                                  room_id BIGINT NOT NULL COMMENT '聊天室ID',
                                  user_id BIGINT NOT NULL COMMENT '用户ID',
                                  user_nickname VARCHAR(100) NOT NULL COMMENT '用户在群内的昵称',
                                  role TINYINT NOT NULL DEFAULT 1 COMMENT '成员角色：1-普通成员 2-管理员 3-群主',
                                  join_time DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '加入时间',
                                  last_read_message_id BIGINT NULL COMMENT '最后阅读的消息ID',
                                  is_muted TINYINT DEFAULT 0 COMMENT '是否禁言：0-否 1-是',
                                  mute_until DATETIME(3) NULL COMMENT '禁言截止时间',
                                  is_delete tinyint default 0 not null  comment '是否删除',

                                  UNIQUE KEY uk_room_user (room_id, user_id),
                                  INDEX idx_user_id (user_id),
                                  INDEX idx_room_id (room_id),
                                  INDEX idx_role (role)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='聊天室成员表';

-- 聊天消息表
CREATE TABLE chat_message (
                              id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '消息ID',
                              content TEXT NOT NULL COMMENT '消息内容',
                              sender_id BIGINT NOT NULL COMMENT '发送者用户ID',
                              sender_name VARCHAR(100) NOT NULL COMMENT '发送者用户名（冗余存储）',
                              message_type TINYINT NOT NULL DEFAULT 1 COMMENT '消息类型：1-群聊 2-私聊 3-系统消息',
                              chat_room_id BIGINT NULL COMMENT '聊天室ID（群聊时使用）',
                              receiver_id BIGINT NULL COMMENT '接收者用户ID（私聊时使用）',
                              receiver_name VARCHAR(100) NULL COMMENT '接收者用户名（冗余存储）',
                              timestamp DATETIME(3) NOT NULL COMMENT '消息时间戳（精确到毫秒）',
                              message_format TINYINT DEFAULT 1 COMMENT '消息格式：1-文本 2-图片 3-文件 4-语音',
                              file_url VARCHAR(500) NULL COMMENT '文件/图片/语音URL',
                              file_size BIGINT NULL COMMENT '文件大小（字节）',
                              reply_to_id BIGINT NULL COMMENT '回复的消息ID',
                              is_read TINYINT DEFAULT 0 COMMENT '是否已读：0-未读 1-已读（私聊使用）',
                              create_time DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
                              update_time DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
                              is_delete tinyint default 0 not null  comment '是否删除',

                              INDEX idx_sender_id (sender_id),
                              INDEX idx_receiver_id (receiver_id),
                              INDEX idx_chat_room_id (chat_room_id),
                              INDEX idx_timestamp (timestamp),
                              INDEX idx_sender_receiver (sender_id, receiver_id),
                              INDEX idx_room_timestamp (chat_room_id, timestamp)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='聊天消息表';



-- 好友关系表（支持申请-同意流程、备注、拉黑等）
CREATE TABLE friend (
                        id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',

                        user_id BIGINT UNSIGNED NOT NULL COMMENT '用户ID（主动发起方）',
                        friend_id BIGINT UNSIGNED NOT NULL COMMENT '好友ID（被添加方）',

                        status TINYINT NOT NULL DEFAULT 0 COMMENT '关系状态：0-待确认 1-已同意 2-已拒绝 3-已拉黑',
                        remark VARCHAR(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '好友备注名（仅对自己可见）',

    -- 扩展字段（按需启用）
                        group_id BIGINT UNSIGNED DEFAULT NULL COMMENT '好友分组ID（可关联 friend_group 表）',
                        avatar VARCHAR(255) DEFAULT NULL COMMENT '自定义好友头像（覆盖原用户头像）',
                        top_order INT NOT NULL DEFAULT 0 COMMENT '置顶排序：0-不置顶 >0-置顶（数值越小越靠前）',
                        mute_until DATETIME DEFAULT NULL COMMENT '消息免打扰截止时间',

                        created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间（申请时间）',
                        updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '最后更新时间（同意/拉黑时间）',

    -- 主键 & 唯一约束（防止重复添加）
                        PRIMARY KEY (id),
                        UNIQUE KEY uk_user_friend (user_id, friend_id) COMMENT '用户与好友唯一关系',

    -- 高性能查询索引
                        KEY idx_user_status (user_id, status, updated_at) COMMENT '查“我的好友/待确认”',
                        KEY idx_friend_status (friend_id, status, updated_at) COMMENT '查“谁加了我”',
                        KEY idx_user_top (user_id, top_order DESC, updated_at DESC) COMMENT '查置顶好友列表'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='好友关系表';
