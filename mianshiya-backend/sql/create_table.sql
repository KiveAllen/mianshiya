# 数据库初始化

-- 创建库
CREATE DATABASE IF NOT EXISTS mianshiya;

-- 切换库
USE mianshiya;

-- 用户表
CREATE TABLE IF NOT EXISTS user
(
    id              BIGINT AUTO_INCREMENT COMMENT 'ID' PRIMARY KEY,
    user_account    VARCHAR(256)                           NOT NULL COMMENT '账号',
    user_password   VARCHAR(512)                           NOT NULL COMMENT '密码',
    union_id        VARCHAR(256)                           NULL COMMENT '微信开放平台ID',
    mp_open_id      VARCHAR(256)                           NULL COMMENT '公众号openID',
    user_name       VARCHAR(256)                           NULL COMMENT '用户昵称',
    user_avatar     VARCHAR(1024)                          NULL COMMENT '用户头像',
    user_profile    VARCHAR(512)                           NULL COMMENT '用户简介',
    user_role       VARCHAR(256) DEFAULT 'user'            NOT NULL COMMENT '用户角色：user/admin/ban',
    edit_time       DATETIME     DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '编辑时间',
    create_time     DATETIME     DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
    update_time     DATETIME     DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_delete       TINYINT      DEFAULT 0                 NOT NULL COMMENT '是否删除',
    vip_expire_time DATETIME                               NULL COMMENT '会员过期时间',
    vip_code        VARCHAR(128)                           NULL COMMENT '会员兑换码',
    vip_number      BIGINT                                 NULL COMMENT '会员编号',
    share_code      VARCHAR(20)  DEFAULT NULL COMMENT '分享码',
    invite_user     BIGINT       DEFAULT NULL COMMENT '邀请用户 ID',
    INDEX idx_union_id (union_id)
) COMMENT '用户' COLLATE = utf8mb4_unicode_ci;

-- 题库表
CREATE TABLE IF NOT EXISTS question_bank
(
    id             BIGINT AUTO_INCREMENT COMMENT 'ID' PRIMARY KEY,
    title          VARCHAR(256)                       NULL COMMENT '标题',
    description    TEXT                               NULL COMMENT '描述',
    picture        VARCHAR(2048)                      NULL COMMENT '图片',
    user_id        BIGINT                             NOT NULL COMMENT '创建用户 ID',
    edit_time      DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '编辑时间',
    create_time    DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
    update_time    DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_delete      TINYINT  DEFAULT 0                 NOT NULL COMMENT '是否删除',
    review_status  INT      DEFAULT 0                 NOT NULL COMMENT '状态：0-待审核, 1-通过, 2-拒绝',
    review_message VARCHAR(512)                       NULL COMMENT '审核信息',
    reviewer_id    BIGINT                             NULL COMMENT '审核人 ID',
    review_time    DATETIME                           NULL COMMENT '审核时间',
    priority       INT      DEFAULT 0                 NOT NULL COMMENT '优先级',
    view_num       INT      DEFAULT 0                 NOT NULL COMMENT '浏览量',
    INDEX idx_title (title)
) COMMENT '题库' COLLATE = utf8mb4_unicode_ci;

-- 题目表
CREATE TABLE IF NOT EXISTS question
(
    id             BIGINT AUTO_INCREMENT COMMENT 'ID' PRIMARY KEY,
    title          VARCHAR(256)                       NULL COMMENT '标题',
    content        TEXT                               NULL COMMENT '内容',
    tags           VARCHAR(1024)                      NULL COMMENT '标签列表(JSON 数组)',
    answer         TEXT                               NULL COMMENT '推荐答案',
    user_id        BIGINT                             NOT NULL COMMENT '创建用户 ID',
    edit_time      DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '编辑时间',
    create_time    DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
    update_time    DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_delete      TINYINT  DEFAULT 0                 NOT NULL COMMENT '是否删除',
    review_status  INT      DEFAULT 0                 NOT NULL COMMENT '状态：0-待审核, 1-通过, 2-拒绝',
    review_message VARCHAR(512)                       NULL COMMENT '审核信息',
    reviewer_id    BIGINT                             NULL COMMENT '审核人 ID',
    review_time    DATETIME                           NULL COMMENT '审核时间',
    view_num       INT      DEFAULT 0                 NOT NULL COMMENT '浏览数',
    thumb_num      INT      DEFAULT 0                 NOT NULL COMMENT '点赞数',
    favour_num     INT      DEFAULT 0                 NOT NULL COMMENT '收藏数',
    priority       INT      DEFAULT 0                 NOT NULL COMMENT '优先级',
    source         VARCHAR(512)                       NULL COMMENT '题目来源',
    need_vip       TINYINT  DEFAULT 0                 NOT NULL COMMENT '仅会员可见(1 表示仅会员可见)',
    INDEX idx_title (title),
    INDEX idx_user_id (user_id)
) COMMENT '题目' COLLATE = utf8mb4_unicode_ci;

-- 题库题目表(硬删除)
CREATE TABLE IF NOT EXISTS question_bank_question
(
    id               BIGINT AUTO_INCREMENT COMMENT 'ID' PRIMARY KEY,
    question_bank_id BIGINT                             NOT NULL COMMENT '题库 ID',
    question_id      BIGINT                             NOT NULL COMMENT '题目 ID',
    user_id          BIGINT                             NOT NULL COMMENT '创建用户 ID',
    create_time      DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
    update_time      DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    question_order   INT      DEFAULT 0                 NOT NULL COMMENT '题目顺序(题号)',
    UNIQUE (question_bank_id, question_id)
) COMMENT '题库题目' COLLATE = utf8mb4_unicode_ci;
