# create schema `retrochat-database`;
use `retrochat-database`;




create table user
(
    id           bigint auto_increment
        primary key,
    username     varchar(50)                        not null,
    password     char(60)                           not null comment 'BCrypt加密固定60字符',
    email        varchar(100)                       null,
    phone        varchar(20)                        null,
    user_avatar  varchar(1024)                      null comment '用户头像',
    user_profile varchar(512)                       null comment '用户简介',
    status       tinyint  default 1                 null comment '1-正常 0-禁用',
    last_login   datetime                           null,
    create_time  datetime default CURRENT_TIMESTAMP null,
    update_time  datetime                           null on update CURRENT_TIMESTAMP,
    is_delete    tinyint  default 0                 not null comment '是否删除',
    constraint email
        unique (email),
    constraint phone
        unique (phone),
    constraint username
        unique (username)
)
    collate = utf8mb4_unicode_ci;

INSERT INTO `retrochat-database`.user (id, username, password, email, phone, user_avatar, user_profile, status, last_login, create_time, update_time, is_delete) VALUES (1, 'bot', 'a23b47ea6692645da1d27c87a6bed5ba', '3336314279@qq.com', null, null, null, 1, null, '2025-10-22 23:18:43', null, 0);
INSERT INTO `retrochat-database`.user (id, username, password, email, phone, user_avatar, user_profile, status, last_login, create_time, update_time, is_delete) VALUES (2, 'Andy', '32edc06ad906c63147d57c5710c0a93f', '626614039@qq.com', null, null, null, 1, null, '2025-10-24 22:29:58', null, 0);
INSERT INTO `retrochat-database`.user (id, username, password, email, phone, user_avatar, user_profile, status, last_login, create_time, update_time, is_delete) VALUES (3, 'timess', 'a23b47ea6692645da1d27c87a6bed5ba', 'xing3336314279@163.com', null, null, null, 1, null, '2025-10-25 21:31:50', null, 0);

create table role
(
    id          bigint auto_increment
        primary key,
    name        varchar(50)                        not null comment '角色名称',
    code        varchar(50)                        not null comment '角色编码(ROLE_ADMIN)',
    description varchar(200)                       null comment '角色描述',
    status      tinyint  default 1                 null comment '1-启用 0-禁用',
    create_time datetime default CURRENT_TIMESTAMP null,
    update_time datetime                           null on update CURRENT_TIMESTAMP,
    constraint code
        unique (code),
    constraint name
        unique (name)
)
    collate = utf8mb4_unicode_ci;



create table permission
(
    id          bigint auto_increment
        primary key,
    name        varchar(50)                        not null comment '权限名称',
    code        varchar(100)                       not null comment '权限标识符(system:user:create)',
    type        tinyint                            not null comment '1-菜单 2-按钮 3-API',
    path        varchar(200)                       null comment '前端路由路径',
    component   varchar(100)                       null comment '前端组件',
    icon        varchar(50)                        null comment '图标',
    parent_id   bigint   default 0                 null comment '父权限ID',
    order_num   int      default 0                 null comment '排序号',
    status      tinyint  default 1                 null comment '1-启用 0-禁用',
    create_time datetime default CURRENT_TIMESTAMP null,
    update_time datetime                           null on update CURRENT_TIMESTAMP,
    constraint code
        unique (code)
)
    collate = utf8mb4_unicode_ci;

create index idx_parent_id
    on permission (parent_id);


create table role_permissions
(
    id            bigint auto_increment
        primary key,
    role_id       bigint                             not null,
    permission_id bigint                             not null,
    create_time   datetime default CURRENT_TIMESTAMP null,
    constraint uk_role_permission
        unique (role_id, permission_id),
    constraint role_permissions_ibfk_1
        foreign key (role_id) references role (id)
            on delete cascade,
    constraint role_permissions_ibfk_2
        foreign key (permission_id) references permission (id)
            on delete cascade
)
    collate = utf8mb4_unicode_ci;

create index permission_id
    on role_permissions (permission_id);





create table chat_room
(
    id                   bigint auto_increment comment '聊天室ID'
        primary key,
    name                 varchar(200)                             not null comment '聊天室名称',
    description          text                                     null comment '聊天室描述',
    type                 tinyint     default 0                    not null comment '聊天室类型：1-公开群聊 2-私密群聊 3-一对一私聊',
    owner_id             bigint                                   not null comment '创建者用户ID',
    max_members          int         default 500                  null comment '最大成员数',
    current_members      int         default 1                    null comment '当前成员数',
    avatar_url           varchar(500)                             null comment '聊天室头像',
    is_active            tinyint     default 1                    null comment '是否活跃：0-已解散 1-活跃',
    last_message_id      bigint                                   null comment '最后一条消息ID',
    last_message_content text                                     null comment '最后一条消息内容（冗余）',
    last_activity_time   datetime(3)                              not null comment '最后活动时间',
    create_time          datetime(3) default CURRENT_TIMESTAMP(3) not null comment '创建时间',
    update_time          datetime(3) default CURRENT_TIMESTAMP(3) not null on update CURRENT_TIMESTAMP(3) comment '更新时间',
    is_delete            tinyint     default 0                    not null comment '是否删除'
)
    comment '聊天室表' collate = utf8mb4_unicode_ci;

create index idx_is_active
    on chat_room (is_active);

create index idx_last_activity_time
    on chat_room (last_activity_time);

create index idx_owner_id
    on chat_room (owner_id);

create index idx_type
    on chat_room (type);

INSERT INTO `retrochat-database`.chat_room (id, name, description, type, owner_id, max_members, current_members, avatar_url, is_active, last_message_id, last_message_content, last_activity_time, create_time, update_time, is_delete) VALUES (349496220672290816, 'chatRoom_2_1', null, 3, 2, 2, 2, null, 1, 1991887878308982784, '111111', '2025-11-21 23:14:12.342', '2025-11-21 23:14:12.336', '2025-11-21 23:14:12.343', 0);
INSERT INTO `retrochat-database`.chat_room (id, name, description, type, owner_id, max_members, current_members, avatar_url, is_active, last_message_id, last_message_content, last_activity_time, create_time, update_time, is_delete) VALUES (349496221343379456, 'chatRoom_2_1', null, 3, 2, 2, 2, null, 1, 1991887878984265728, '111111', '2025-11-21 23:14:12.503', '2025-11-21 23:14:12.496', '2025-11-21 23:14:12.505', 0);
INSERT INTO `retrochat-database`.chat_room (id, name, description, type, owner_id, max_members, current_members, avatar_url, is_active, last_message_id, last_message_content, last_activity_time, create_time, update_time, is_delete) VALUES (349496221964136448, 'chatRoom_2_1', null, 3, 2, 2, 2, null, 1, 1991887879605022720, '111111', '2025-11-21 23:14:12.652', '2025-11-21 23:14:12.645', '2025-11-21 23:14:12.654', 0);
INSERT INTO `retrochat-database`.chat_room (id, name, description, type, owner_id, max_members, current_members, avatar_url, is_active, last_message_id, last_message_content, last_activity_time, create_time, update_time, is_delete) VALUES (349496222714916864, 'chatRoom_2_1', null, 3, 2, 2, 2, null, 1, 1991887880355803136, '111111', '2025-11-21 23:14:12.830', '2025-11-21 23:14:12.823', '2025-11-21 23:14:12.831', 0);
INSERT INTO `retrochat-database`.chat_room (id, name, description, type, owner_id, max_members, current_members, avatar_url, is_active, last_message_id, last_message_content, last_activity_time, create_time, update_time, is_delete) VALUES (349496223406977024, 'chatRoom_2_1', null, 3, 2, 2, 2, null, 1, 1991887881047863296, '111111', '2025-11-21 23:14:12.996', '2025-11-21 23:14:12.989', '2025-11-21 23:14:12.997', 0);
INSERT INTO `retrochat-database`.chat_room (id, name, description, type, owner_id, max_members, current_members, avatar_url, is_active, last_message_id, last_message_content, last_activity_time, create_time, update_time, is_delete) VALUES (349496224111620096, 'chatRoom_2_1', null, 3, 2, 2, 2, null, 1, 1991887881752506368, '111111', '2025-11-21 23:14:13.163', '2025-11-21 23:14:13.156', '2025-11-21 23:14:13.163', 0);
INSERT INTO `retrochat-database`.chat_room (id, name, description, type, owner_id, max_members, current_members, avatar_url, is_active, last_message_id, last_message_content, last_activity_time, create_time, update_time, is_delete) VALUES (349496224828846080, 'chatRoom_2_1', null, 3, 2, 2, 2, null, 1, 1991887882469732352, '111111', '2025-11-21 23:14:13.334', '2025-11-21 23:14:13.327', '2025-11-21 23:14:13.335', 0);
INSERT INTO `retrochat-database`.chat_room (id, name, description, type, owner_id, max_members, current_members, avatar_url, is_active, last_message_id, last_message_content, last_activity_time, create_time, update_time, is_delete) VALUES (349496225525100544, 'chatRoom_2_1', null, 3, 2, 2, 2, null, 1, 1991887883161792512, '111111', '2025-11-21 23:14:13.502', '2025-11-21 23:14:13.493', '2025-11-21 23:14:13.503', 0);
INSERT INTO `retrochat-database`.chat_room (id, name, description, type, owner_id, max_members, current_members, avatar_url, is_active, last_message_id, last_message_content, last_activity_time, create_time, update_time, is_delete) VALUES (349496226212966400, 'chatRoom_2_1', null, 3, 2, 2, 2, null, 1, 1991887883853852672, '111111', '2025-11-21 23:14:13.664', '2025-11-21 23:14:13.657', '2025-11-21 23:14:13.665', 0);


create table chat_room_member
(
    id                   bigint auto_increment comment '主键ID'
        primary key,
    room_id              bigint                                   not null comment '聊天室ID',
    user_id              bigint                                   not null comment '用户ID',
    user_nickname        varchar(100)                             not null comment '用户在群内的昵称',
    role                 tinyint     default 1                    not null comment '成员角色：1-普通成员 2-管理员 3-群主',
    join_time            datetime(3) default CURRENT_TIMESTAMP(3) not null comment '加入时间',
    last_read_message_id bigint                                   null comment '最后阅读的消息ID',
    is_muted             tinyint     default 0                    null comment '是否禁言：0-否 1-是',
    mute_until           datetime(3)                              null comment '禁言截止时间',
    is_delete            tinyint     default 0                    not null comment '是否删除',
    constraint uk_room_user
        unique (room_id, user_id)
)
    comment '聊天室成员表' collate = utf8mb4_unicode_ci;

create index idx_role
    on chat_room_member (role);

create index idx_room_id
    on chat_room_member (room_id);

create index idx_user_id
    on chat_room_member (user_id);

INSERT INTO `retrochat-database`.chat_room_member (id, room_id, user_id, user_nickname, role, join_time, last_read_message_id, is_muted, mute_until, is_delete) VALUES (349496220680679424, 349496220672290816, 2, 'Andy', 3, '2025-11-21 23:14:12.337', null, 0, null, 0);
INSERT INTO `retrochat-database`.chat_room_member (id, room_id, user_id, user_nickname, role, join_time, last_read_message_id, is_muted, mute_until, is_delete) VALUES (349496220693262336, 349496220672290816, 1, 'bot', 3, '2025-11-21 23:14:12.340', null, 0, null, 0);
INSERT INTO `retrochat-database`.chat_room_member (id, room_id, user_id, user_nickname, role, join_time, last_read_message_id, is_muted, mute_until, is_delete) VALUES (349496221355962368, 349496221343379456, 2, 'Andy', 3, '2025-11-21 23:14:12.497', null, 0, null, 0);
INSERT INTO `retrochat-database`.chat_room_member (id, room_id, user_id, user_nickname, role, join_time, last_read_message_id, is_muted, mute_until, is_delete) VALUES (349496221368545280, 349496221343379456, 1, 'bot', 3, '2025-11-21 23:14:12.500', null, 0, null, 0);
INSERT INTO `retrochat-database`.chat_room_member (id, room_id, user_id, user_nickname, role, join_time, last_read_message_id, is_muted, mute_until, is_delete) VALUES (349496221980913664, 349496221964136448, 2, 'Andy', 3, '2025-11-21 23:14:12.645', null, 0, null, 0);
INSERT INTO `retrochat-database`.chat_room_member (id, room_id, user_id, user_nickname, role, join_time, last_read_message_id, is_muted, mute_until, is_delete) VALUES (349496221993496576, 349496221964136448, 1, 'bot', 3, '2025-11-21 23:14:12.649', null, 0, null, 0);
INSERT INTO `retrochat-database`.chat_room_member (id, room_id, user_id, user_nickname, role, join_time, last_read_message_id, is_muted, mute_until, is_delete) VALUES (349496222723305472, 349496222714916864, 2, 'Andy', 3, '2025-11-21 23:14:12.824', null, 0, null, 0);
INSERT INTO `retrochat-database`.chat_room_member (id, room_id, user_id, user_nickname, role, join_time, last_read_message_id, is_muted, mute_until, is_delete) VALUES (349496222735888384, 349496222714916864, 1, 'bot', 3, '2025-11-21 23:14:12.827', null, 0, null, 0);
INSERT INTO `retrochat-database`.chat_room_member (id, room_id, user_id, user_nickname, role, join_time, last_read_message_id, is_muted, mute_until, is_delete) VALUES (349496223419559936, 349496223406977024, 2, 'Andy', 3, '2025-11-21 23:14:12.990', null, 0, null, 0);
INSERT INTO `retrochat-database`.chat_room_member (id, room_id, user_id, user_nickname, role, join_time, last_read_message_id, is_muted, mute_until, is_delete) VALUES (349496223432142848, 349496223406977024, 1, 'bot', 3, '2025-11-21 23:14:12.993', null, 0, null, 0);
INSERT INTO `retrochat-database`.chat_room_member (id, room_id, user_id, user_nickname, role, join_time, last_read_message_id, is_muted, mute_until, is_delete) VALUES (349496224120008704, 349496224111620096, 2, 'Andy', 3, '2025-11-21 23:14:13.157', null, 0, null, 0);
INSERT INTO `retrochat-database`.chat_room_member (id, room_id, user_id, user_nickname, role, join_time, last_read_message_id, is_muted, mute_until, is_delete) VALUES (349496224132591616, 349496224111620096, 1, 'bot', 3, '2025-11-21 23:14:13.159', null, 0, null, 0);
INSERT INTO `retrochat-database`.chat_room_member (id, room_id, user_id, user_nickname, role, join_time, last_read_message_id, is_muted, mute_until, is_delete) VALUES (349496224837234688, 349496224828846080, 2, 'Andy', 3, '2025-11-21 23:14:13.328', null, 0, null, 0);
INSERT INTO `retrochat-database`.chat_room_member (id, room_id, user_id, user_nickname, role, join_time, last_read_message_id, is_muted, mute_until, is_delete) VALUES (349496224849817600, 349496224828846080, 1, 'bot', 3, '2025-11-21 23:14:13.330', null, 0, null, 0);
INSERT INTO `retrochat-database`.chat_room_member (id, room_id, user_id, user_nickname, role, join_time, last_read_message_id, is_muted, mute_until, is_delete) VALUES (349496225537683456, 349496225525100544, 2, 'Andy', 3, '2025-11-21 23:14:13.494', null, 0, null, 0);
INSERT INTO `retrochat-database`.chat_room_member (id, room_id, user_id, user_nickname, role, join_time, last_read_message_id, is_muted, mute_until, is_delete) VALUES (349496225554460672, 349496225525100544, 1, 'bot', 3, '2025-11-21 23:14:13.499', null, 0, null, 0);
INSERT INTO `retrochat-database`.chat_room_member (id, room_id, user_id, user_nickname, role, join_time, last_read_message_id, is_muted, mute_until, is_delete) VALUES (349496226225549312, 349496226212966400, 2, 'Andy', 3, '2025-11-21 23:14:13.658', null, 0, null, 0);
INSERT INTO `retrochat-database`.chat_room_member (id, room_id, user_id, user_nickname, role, join_time, last_read_message_id, is_muted, mute_until, is_delete) VALUES (349496226238132224, 349496226212966400, 1, 'bot', 3, '2025-11-21 23:14:13.661', null, 0, null, 0);



create table chat_message
(
    id             bigint auto_increment comment '消息ID'
        primary key,
    content        text                                     not null comment '消息内容',
    sender_id      bigint                                   not null comment '发送者用户ID',
    sender_name    varchar(100)                             not null comment '发送者用户名（冗余存储）',
    message_type   tinyint     default 1                    not null comment '消息类型：1-群聊 2-私聊 3-系统消息',
    chat_room_id   bigint                                   null comment '聊天室ID（群聊时使用）',
    receiver_id    bigint                                   null comment '接收者用户ID（私聊时使用）',
    receiver_name  varchar(100)                             null comment '接收者用户名（冗余存储）',
    timestamp      datetime(3)                              not null comment '消息时间戳（精确到毫秒）',
    message_format tinyint     default 1                    null comment '消息格式：1-文本 2-图片 3-文件 4-语音',
    file_url       varchar(500)                             null comment '文件/图片/语音URL',
    file_size      bigint                                   null comment '文件大小（字节）',
    reply_to_id    bigint                                   null comment '回复的消息ID',
    is_read        tinyint     default 0                    null comment '是否已读：0-未读 1-已读（私聊使用）',
    create_time    datetime(3) default CURRENT_TIMESTAMP(3) not null comment '创建时间',
    update_time    datetime(3) default CURRENT_TIMESTAMP(3) not null on update CURRENT_TIMESTAMP(3) comment '更新时间',
    is_delete      tinyint     default 0                    not null comment '是否删除'
)
    comment '聊天消息表' collate = utf8mb4_unicode_ci;

create index idx_chat_room_id
    on chat_message (chat_room_id);

create index idx_receiver_id
    on chat_message (receiver_id);

create index idx_room_timestamp
    on chat_message (chat_room_id, timestamp);

create index idx_sender_id
    on chat_message (sender_id);

create index idx_sender_receiver
    on chat_message (sender_id, receiver_id);

create index idx_timestamp
    on chat_message (timestamp);

INSERT INTO `retrochat-database`.chat_message (id, content, sender_id, sender_name, message_type, chat_room_id, receiver_id, receiver_name, timestamp, message_format, file_url, file_size, reply_to_id, is_read, create_time, update_time, is_delete) VALUES (1991887878308982784, '111111', 2, 'Andy', 1, 349496220672290816, 1, null, '2025-11-21 23:14:12.334', 1, null, null, null, 0, '2025-11-21 23:14:12.342', '2025-11-21 23:14:12.342', 0);
INSERT INTO `retrochat-database`.chat_message (id, content, sender_id, sender_name, message_type, chat_room_id, receiver_id, receiver_name, timestamp, message_format, file_url, file_size, reply_to_id, is_read, create_time, update_time, is_delete) VALUES (1991887878984265728, '111111', 2, 'Andy', 1, 349496221343379456, 1, null, '2025-11-21 23:14:12.494', 1, null, null, null, 0, '2025-11-21 23:14:12.503', '2025-11-21 23:14:12.503', 0);
INSERT INTO `retrochat-database`.chat_message (id, content, sender_id, sender_name, message_type, chat_room_id, receiver_id, receiver_name, timestamp, message_format, file_url, file_size, reply_to_id, is_read, create_time, update_time, is_delete) VALUES (1991887879605022720, '111111', 2, 'Andy', 1, 349496221964136448, 1, null, '2025-11-21 23:14:12.643', 1, null, null, null, 0, '2025-11-21 23:14:12.652', '2025-11-21 23:14:12.652', 0);
INSERT INTO `retrochat-database`.chat_message (id, content, sender_id, sender_name, message_type, chat_room_id, receiver_id, receiver_name, timestamp, message_format, file_url, file_size, reply_to_id, is_read, create_time, update_time, is_delete) VALUES (1991887880355803136, '111111', 2, 'Andy', 1, 349496222714916864, 1, null, '2025-11-21 23:14:12.822', 1, null, null, null, 0, '2025-11-21 23:14:12.829', '2025-11-21 23:14:12.829', 0);
INSERT INTO `retrochat-database`.chat_message (id, content, sender_id, sender_name, message_type, chat_room_id, receiver_id, receiver_name, timestamp, message_format, file_url, file_size, reply_to_id, is_read, create_time, update_time, is_delete) VALUES (1991887881047863296, '111111', 2, 'Andy', 1, 349496223406977024, 1, null, '2025-11-21 23:14:12.986', 1, null, null, null, 0, '2025-11-21 23:14:12.996', '2025-11-21 23:14:12.996', 0);
INSERT INTO `retrochat-database`.chat_message (id, content, sender_id, sender_name, message_type, chat_room_id, receiver_id, receiver_name, timestamp, message_format, file_url, file_size, reply_to_id, is_read, create_time, update_time, is_delete) VALUES (1991887881752506368, '111111', 2, 'Andy', 1, 349496224111620096, 1, null, '2025-11-21 23:14:13.154', 1, null, null, null, 0, '2025-11-21 23:14:13.162', '2025-11-21 23:14:13.162', 0);
INSERT INTO `retrochat-database`.chat_message (id, content, sender_id, sender_name, message_type, chat_room_id, receiver_id, receiver_name, timestamp, message_format, file_url, file_size, reply_to_id, is_read, create_time, update_time, is_delete) VALUES (1991887882469732352, '111111', 2, 'Andy', 1, 349496224828846080, 1, null, '2025-11-21 23:14:13.325', 1, null, null, null, 0, '2025-11-21 23:14:13.334', '2025-11-21 23:14:13.334', 0);
INSERT INTO `retrochat-database`.chat_message (id, content, sender_id, sender_name, message_type, chat_room_id, receiver_id, receiver_name, timestamp, message_format, file_url, file_size, reply_to_id, is_read, create_time, update_time, is_delete) VALUES (1991887883161792512, '111111', 2, 'Andy', 1, 349496225525100544, 1, null, '2025-11-21 23:14:13.491', 1, null, null, null, 0, '2025-11-21 23:14:13.501', '2025-11-21 23:14:13.501', 0);
INSERT INTO `retrochat-database`.chat_message (id, content, sender_id, sender_name, message_type, chat_room_id, receiver_id, receiver_name, timestamp, message_format, file_url, file_size, reply_to_id, is_read, create_time, update_time, is_delete) VALUES (1991887883853852672, '111111', 2, 'Andy', 1, 349496226212966400, 1, null, '2025-11-21 23:14:13.655', 1, null, null, null, 0, '2025-11-21 23:14:13.663', '2025-11-21 23:14:13.663', 0);


create table friend
(
    id         bigint unsigned auto_increment comment '主键ID'
        primary key,
    user_id    bigint unsigned                          not null comment '用户ID（主动发起方）',
    friend_id  bigint unsigned                          not null comment '好友ID（被添加方）',
    status     tinyint     default 0                    not null comment '关系状态：0-待确认 1-已同意 2-已拒绝 3-已拉黑',
    remark     varchar(50) default ''                   null comment '好友备注名（仅对自己可见）',
    group_id   bigint unsigned                          null comment '好友分组ID（可关联 friend_group 表）',
    avatar     varchar(255)                             null comment '自定义好友头像（覆盖原用户头像）',
    top_order  int         default 0                    not null comment '置顶排序：0-不置顶 >0-置顶（数值越小越靠前）',
    mute_until datetime                                 null comment '消息免打扰截止时间',
    created_at datetime(3) default CURRENT_TIMESTAMP(3) not null comment '创建时间（申请时间）',
    updated_at datetime(3) default CURRENT_TIMESTAMP(3) not null on update CURRENT_TIMESTAMP(3) comment '最后更新时间（同意/拉黑时间）',
    constraint uk_user_friend
        unique (user_id, friend_id) comment '用户与好友唯一关系'
)
    comment '好友关系表' collate = utf8mb4_unicode_ci;

create index idx_friend_status
    on friend (friend_id, status, updated_at)
    comment '查“谁加了我”';

create index idx_user_status
    on friend (user_id, status, updated_at)
    comment '查“我的好友/待确认”';

create index idx_user_top
    on friend (user_id asc, top_order desc, updated_at desc)
    comment '查置顶好友列表';

INSERT INTO `retrochat-database`.friend (id, user_id, friend_id, status, remark, group_id, avatar, top_order, mute_until, created_at, updated_at) VALUES (1, 2, 3, 1, '后端同事-timess', null, null, 1, null, '2025-11-17 22:30:28.819', '2025-11-17 22:30:28.819');
INSERT INTO `retrochat-database`.friend (id, user_id, friend_id, status, remark, group_id, avatar, top_order, mute_until, created_at, updated_at) VALUES (2, 3, 2, 1, '前端同事-Andy', null, null, 0, null, '2025-11-17 22:30:28.819', '2025-11-17 22:30:28.819');
INSERT INTO `retrochat-database`.friend (id, user_id, friend_id, status, remark, group_id, avatar, top_order, mute_until, created_at, updated_at) VALUES (3, 2, 1, 1, '系统助手', null, null, 2, null, '2025-11-17 22:30:28.819', '2025-11-17 22:30:28.819');
INSERT INTO `retrochat-database`.friend (id, user_id, friend_id, status, remark, group_id, avatar, top_order, mute_until, created_at, updated_at) VALUES (4, 1, 2, 1, '用户-Andy', null, null, 0, null, '2025-11-17 22:30:28.819', '2025-11-17 22:30:28.819');
INSERT INTO `retrochat-database`.friend (id, user_id, friend_id, status, remark, group_id, avatar, top_order, mute_until, created_at, updated_at) VALUES (5, 3, 1, 1, '系统助手', null, null, 2, null, '2025-11-17 22:30:28.819', '2025-11-17 22:30:28.819');
INSERT INTO `retrochat-database`.friend (id, user_id, friend_id, status, remark, group_id, avatar, top_order, mute_until, created_at, updated_at) VALUES (6, 1, 3, 1, '用户-timess', null, null, 0, null, '2025-11-17 22:30:28.819', '2025-11-17 22:30:28.819');


create table login_log
(
    id           bigint auto_increment
        primary key,
    user_id      bigint                             null,
    ip           varchar(45)                        null comment 'IPv4/IPv6地址',
    device       varchar(200)                       null comment '设备信息',
    os           varchar(50)                        null comment '操作系统',
    browser      varchar(50)                        null comment '浏览器类型',
    status       tinyint                            null comment '1-成功 0-失败',
    location     varchar(100)                       null comment 'IP地理位置',
    created_time datetime default CURRENT_TIMESTAMP null
)
    collate = utf8mb4_unicode_ci;

create index idx_created_time
    on login_log (created_time);

create index idx_user_id
    on login_log (user_id);


create table user_role
(
    id          bigint auto_increment
        primary key,
    user_id     bigint                             not null,
    role_id     bigint                             not null,
    create_time datetime default CURRENT_TIMESTAMP null,
    constraint uk_user_role
        unique (user_id, role_id),
    constraint user_role_ibfk_1
        foreign key (user_id) references user (id)
            on delete cascade,
    constraint user_role_ibfk_2
        foreign key (role_id) references role (id)
            on delete cascade
)
    collate = utf8mb4_unicode_ci;

create index role_id
    on user_role (role_id);
