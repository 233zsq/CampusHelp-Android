-- ========================================
-- CampusHelp MySQL 建表脚本
-- 对应 Android 端 Room 的 5 张表
-- ========================================

CREATE DATABASE IF NOT EXISTS campushelp
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE campushelp;

-- 用户表（对应 Room users）
CREATE TABLE IF NOT EXISTS `user` (
    `id`           BIGINT       NOT NULL AUTO_INCREMENT,
    `student_id`   VARCHAR(32)  NOT NULL COMMENT '学号',
    `password`     VARCHAR(128) NOT NULL DEFAULT '' COMMENT '密码(BCrypt)',
    `name`         VARCHAR(64)  NOT NULL DEFAULT '' COMMENT '昵称',
    `avatar`       VARCHAR(256) NOT NULL DEFAULT '' COMMENT '头像 URL',
    `credit_score` INT          NOT NULL DEFAULT 600 COMMENT '信用分 0~1000',
    `phone`        VARCHAR(20)  NOT NULL DEFAULT '' COMMENT '手机号',
    `created_at`   BIGINT       NOT NULL DEFAULT 0 COMMENT '创建时间戳(ms)',
    `deleted`      TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_student_id` (`student_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 任务表（对应 Room tasks）
CREATE TABLE IF NOT EXISTS `task` (
    `id`           BIGINT       NOT NULL AUTO_INCREMENT,
    `publisher_id` BIGINT       NOT NULL COMMENT '发布者 user.id',
    `type`         TINYINT      NOT NULL DEFAULT 0 COMMENT '0跑腿 1拼单 2二手',
    `title`        VARCHAR(128) NOT NULL DEFAULT '' COMMENT '标题',
    `content`      TEXT         COMMENT '内容描述',
    `reward`       DOUBLE       NOT NULL DEFAULT 0 COMMENT '报酬/单价',
    `location`     VARCHAR(128) NOT NULL DEFAULT '' COMMENT '地点文案',
    `latitude`     DOUBLE       NOT NULL DEFAULT 0 COMMENT '纬度',
    `longitude`    DOUBLE       NOT NULL DEFAULT 0 COMMENT '经度',
    `status`       TINYINT      NOT NULL DEFAULT 0 COMMENT '0待接单 1已接单 2已完成 3已取消',
    `deadline`     BIGINT       NOT NULL DEFAULT 0 COMMENT '截止时间戳(ms)',
    `created_at`   BIGINT       NOT NULL DEFAULT 0 COMMENT '创建时间戳(ms)',
    `deleted`      TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    KEY `idx_publisher_id` (`publisher_id`),
    KEY `idx_type` (`type`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务表';

-- 接单表（对应 Room orders）
CREATE TABLE IF NOT EXISTS `order` (
    `id`           BIGINT       NOT NULL AUTO_INCREMENT,
    `task_id`      BIGINT       NOT NULL COMMENT '关联 task.id',
    `taker_id`     BIGINT       NOT NULL COMMENT '接单人 user.id',
    `accepted_at`  BIGINT       NOT NULL DEFAULT 0 COMMENT '接单时间戳(ms)',
    `deadline`     BIGINT       NOT NULL DEFAULT 0 COMMENT '接单截止时间戳(ms)',
    `status`       TINYINT      NOT NULL DEFAULT 0 COMMENT '0进行中 1已完成 2超时',
    `completed_at` BIGINT       NOT NULL DEFAULT 0 COMMENT '完成时间戳(ms)',
    `deleted`      TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    KEY `idx_task_id` (`task_id`),
    KEY `idx_taker_id` (`taker_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='接单表';

-- 聊天消息表（对应 Room messages）
CREATE TABLE IF NOT EXISTS `chat_message` (
    `id`              BIGINT       NOT NULL AUTO_INCREMENT,
    `conversation_id` VARCHAR(128) NOT NULL COMMENT '会话ID',
    `sender_id`       BIGINT       NOT NULL COMMENT '发送者 user.id',
    `receiver_id`     BIGINT       NOT NULL COMMENT '接收者 user.id',
    `content`         TEXT         COMMENT '消息内容',
    `type`            TINYINT      NOT NULL DEFAULT 0 COMMENT '0文本 1图片 2订单卡片',
    `timestamp`       BIGINT       NOT NULL DEFAULT 0 COMMENT '发送时间戳(ms)',
    `read`            TINYINT      NOT NULL DEFAULT 0 COMMENT '是否已读 0否 1是',
    `deleted`         TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    KEY `idx_conversation_id` (`conversation_id`),
    KEY `idx_receiver_id` (`receiver_id`),
    KEY `idx_receiver_read` (`receiver_id`, `read`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='聊天消息表';

-- 信用分变动记录表（对应 Room credit_records）
CREATE TABLE IF NOT EXISTS `credit_record` (
    `id`         BIGINT       NOT NULL AUTO_INCREMENT,
    `user_id`    BIGINT       NOT NULL COMMENT '关联 user.id',
    `delta`      INT          NOT NULL DEFAULT 0 COMMENT '信用分变动(+/-)',
    `reason`     VARCHAR(256) NOT NULL DEFAULT '' COMMENT '变动原因',
    `timestamp`  BIGINT       NOT NULL DEFAULT 0 COMMENT '变动时间戳(ms)',
    `deleted`    TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='信用分变动记录表';
