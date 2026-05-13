-- 创建数据库
CREATE DATABASE IF NOT EXISTS ai_image_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE ai_image_db;

create table category
(
    id         bigint auto_increment
        primary key,
    name       varchar(100)                       not null comment '分类名称',
    code       varchar(100)                       not null comment '分类编码',
    sort       int      default 0                 null comment '排序',
    status     tinyint  default 1                 null comment '1=启用 0=禁用',
    created_at datetime default CURRENT_TIMESTAMP null comment '创建时间',
    updated_at datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    constraint code
        unique (code)
);

create index idx_sort
    on category (sort);

create index idx_status
    on category (status);

create table email_code
(
    id         bigint auto_increment
        primary key,
    email      varchar(255)                       not null comment '邮箱',
    code       varchar(10)                        not null comment '验证码',
    expire_at  datetime                           not null comment '过期时间',
    used       tinyint  default 0                 null comment '0=未用 1=已用',
    created_at datetime default CURRENT_TIMESTAMP null comment '创建时间'
);

create index idx_email
    on email_code (email);

create index idx_expire_at
    on email_code (expire_at);

create table favorite
(
    id         bigint auto_increment
        primary key,
    user_id    bigint                             not null comment '用户ID',
    prompt_id  bigint                             not null comment '提示词ID',
    created_at datetime default CURRENT_TIMESTAMP null comment '收藏时间',
    constraint uk_user_prompt
        unique (user_id, prompt_id)
);

create index idx_user_id
    on favorite (user_id);

create index prompt_id
    on favorite (prompt_id);

create table prompt
(
    id              bigint auto_increment
        primary key,
    title           varchar(255)                       not null comment '标题',
    description     text                               null comment '描述',
    content         text                               not null comment '提示词内容',
    image_urls      text                               null comment '示意图URL列表 (JSON格式)',
    params          text                               null comment '参数JSON (合参数配置)',
    life_cycle      int                                null comment '生成~完成(秒)',
    api_url         varchar(500)                       null comment '调用API地址',
    api_method      varchar(20)                        null comment 'GET/POST',
    api_params      text                               null comment '参数配置 (JSON编码器或结构)',
    sort            int      default 0                 null comment '排序',
    status          tinyint  default 1                 null comment '1=上线 0=下架',
    created_at      datetime default CURRENT_TIMESTAMP null comment '创建时间',
    updated_at      datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    cost_points     int      default 10                null,
    ref_image_count int      default 0                 null,
    category_id     bigint                             null comment '分类ID'
);

create index idx_category_id
    on prompt (category_id);

create index idx_sort
    on prompt (sort);

create index idx_status
    on prompt (status);

create index prompt_category_id_index
    on prompt (category_id);

create table sys_user
(
    id          bigint auto_increment
        primary key,
    email       varchar(255)                       not null comment '邮箱',
    password    varchar(255)                       null comment '密码 (BCrypt)',
    level       int      default 1                 null comment '用户等级',
    invite_code varchar(20)                        null comment '用户邀请码',
    status      tinyint  default 1                 null comment '1=正常 0=禁用',
    created_at  datetime default CURRENT_TIMESTAMP null comment '创建时间',
    updated_at  datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    invitee_id  bigint                             null,
    constraint invite_code
        unique (invite_code)
);

create table generation_record
(
    id                    bigint auto_increment
        primary key,
    user_id               bigint                             not null comment '用户ID',
    prompt_id             bigint                             not null comment '提示词ID',
    params                text                               null comment '用户选择的参数 (JSON)',
    ref_images            text                               null comment '参考图片URLs (JSON)',
    status                tinyint  default 0                 null comment '0=待处理 1=生成中 2=生成完成 3=失败',
    result_images         text                               null comment '生成的图片URLs (JSON)',
    external_api_response json                               null comment '外部API响应 (JSON, 保留原始响应)',
    error_msg             varchar(500)                       null comment '失败原因',
    cost_points           int                                null comment '消耗积分',
    created_at            datetime default CURRENT_TIMESTAMP null comment '创建时间',
    updated_at            datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    constraint generation_record_ibfk_1
        foreign key (user_id) references sys_user (id),
    constraint generation_record_ibfk_2
        foreign key (prompt_id) references prompt (id)
);

create index idx_created_at
    on generation_record (created_at);

create index idx_prompt_id
    on generation_record (prompt_id);

create index idx_status
    on generation_record (status);

create index idx_user_id
    on generation_record (user_id);

create table point_log
(
    id         bigint auto_increment
        primary key,
    user_id    bigint                             not null comment '用户ID',
    type       tinyint                            not null comment '1=生成消费 2=邀请奖励 3=充值 4=管理员调整',
    amount     int                                not null comment '变动积分 (正数=增加 负数=减少)',
    balance    int                                not null comment '变动后余额',
    remark     varchar(255)                       null comment '备注',
    is_settled tinyint  default 0                 null comment '0=未结算 1=已结算',
    created_at datetime default CURRENT_TIMESTAMP null comment '创建时间',
    related_id bigint                             null,
    constraint point_log_ibfk_1
        foreign key (user_id) references sys_user (id)
);

create index idx_created_at
    on point_log (created_at);

create index idx_type
    on point_log (type);

create index idx_user_id
    on point_log (user_id);

create index idx_email
    on sys_user (email);

create index idx_status
    on sys_user (status);

create table system_config
(
    id           bigint auto_increment
        primary key,
    config_key   varchar(100)                       not null comment '配置键 (一一对应)',
    config_value varchar(500)                       not null comment '配置值',
    description  varchar(255)                       null comment '配置说明',
    updated_at   datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    constraint config_key
        unique (config_key)
);

create index idx_config_key
    on system_config (config_key);

