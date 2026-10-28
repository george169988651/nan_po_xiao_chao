-- 菜单表
CREATE TABLE IF NOT EXISTS menu (
                                    id          BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '菜单id',
                                    name        VARCHAR(100)  NOT NULL COMMENT '菜品名称',
                                    price       NUMERIC(10,2) NOT NULL COMMENT '菜品价格',
                                    image       text          COMMENT '菜品图片',
                                    create_time TIMESTAMP     NOT NULL default current_timestamp COMMENT '创建时间',
                                    update_time TIMESTAMP     NOT NULL default current_timestamp on update current_timestamp COMMENT '更新时间',
    unique key `uk_name`(`name`)
)ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- 订单表
-- order_list 必须是 varchar/text:存的是 JSON 文本(如 [3,5])。
-- 别改回 json 类型——H2 会把 Java 字符串当成 JSON 字符串值写入,变成 "[3,5]"(多一层引号),
-- 回读时 Jackson3TypeHandler 反序列化会抛 MismatchedInputException。
-- user_id 不加唯一约束:一个用户可以下多单,唯一约束会让第二单直接 500
create table if not exists dish_order(
    `id` bigint not null primary key auto_increment comment '订单id',
    `user_id` bigint not null comment '用户id',
    `user_name` varchar(50) not null comment '用户姓名',
    `order_list` varchar(255) not null comment '对应菜单表的id,JSON 数组文本',
    `message` varchar(255) comment '其他消息',
    `create_time` datetime  default current_timestamp comment '创建时间',
    `update_time` datetime  default current_timestamp on update current_timestamp comment '更新时间',
    key `idx_userId_userName`(`user_id`, `user_name`)
)ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;


-- 聊天记忆表
CREATE TABLE IF NOT EXISTS SPRING_AI_CHAT_MEMORY (
    `id`              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键id',
    `conversation_id` VARCHAR(36)  NOT NULL COMMENT '会话标识',
    `content`         TEXT         NOT NULL COMMENT '消息内容',
    `type`            VARCHAR(10)  NOT NULL COMMENT '消息类型: USER/ASSISTANT/SYSTEM/TOOL',
    `timestamp`       TIMESTAMP    NOT NULL COMMENT '消息时间戳',
    PRIMARY KEY (`id`),
    INDEX `idx_conv_ts` (`conversation_id`, `timestamp`)
);

-- 用户表(H2 中 USER 是保留字,故命名为 sys_user)
CREATE TABLE IF NOT EXISTS sys_user (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '用户id',
    `username`    VARCHAR(50)  NOT NULL COMMENT '登录用户名',
    `password`    VARCHAR(100) NOT NULL COMMENT 'BCrypt 密码哈希',
    `nickname`    VARCHAR(50)  COMMENT '昵称',
    `create_time` DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;