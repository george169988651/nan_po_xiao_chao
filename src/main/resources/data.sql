-- 菜单测试数据
INSERT INTO menu (name, price, create_time, update_time) VALUES
                                                             ('宫保鸡丁', 28.00, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                                                             ('麻婆豆腐', 18.00, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                                                             ('鱼香肉丝', 26.00, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                                                             ('水煮肉片', 38.00, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                                                             ('回锅肉',   32.00, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
-- 测试用户(密码统一为 123456，与 george 复用同一 BCrypt 哈希)
INSERT INTO sys_user (username, password, nickname, create_time, update_time) VALUES
    ('george',   '$2a$10$iy3SeIlRXNIZHYlzbpNbp.6IUlZSYEE0USRtnl1PRj3EpHQ6nhvm2', '乔治', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('zhangwei', '$2a$10$iy3SeIlRXNIZHYlzbpNbp.6IUlZSYEE0USRtnl1PRj3EpHQ6nhvm2', '张伟', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('wangfang', '$2a$10$iy3SeIlRXNIZHYlzbpNbp.6IUlZSYEE0USRtnl1PRj3EpHQ6nhvm2', '王芳', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('liqiang',  '$2a$10$iy3SeIlRXNIZHYlzbpNbp.6IUlZSYEE0USRtnl1PRj3EpHQ6nhvm2', '李强', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('liumin',   '$2a$10$iy3SeIlRXNIZHYlzbpNbp.6IUlZSYEE0USRtnl1PRj3EpHQ6nhvm2', '刘敏', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('chenjie',  '$2a$10$iy3SeIlRXNIZHYlzbpNbp.6IUlZSYEE0USRtnl1PRj3EpHQ6nhvm2', '陈杰', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('yangli',   '$2a$10$iy3SeIlRXNIZHYlzbpNbp.6IUlZSYEE0USRtnl1PRj3EpHQ6nhvm2', '杨丽', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('zhaolei',  '$2a$10$iy3SeIlRXNIZHYlzbpNbp.6IUlZSYEE0USRtnl1PRj3EpHQ6nhvm2', '赵磊', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('huangyong','$2a$10$iy3SeIlRXNIZHYlzbpNbp.6IUlZSYEE0USRtnl1PRj3EpHQ6nhvm2', '黄勇', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('zhoujing', '$2a$10$iy3SeIlRXNIZHYlzbpNbp.6IUlZSYEE0USRtnl1PRj3EpHQ6nhvm2', '周静', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 订单测试数据(order_list 为菜单 id 数组，对应 menu 表 1~5)
INSERT INTO dish_order (user_id, user_name, order_list, message, create_time, update_time) VALUES
    (1,  '乔治', json_array(1,2,5),       '少放辣，再加一碗米饭',       CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (2,  '张伟', json_array(3,4),         '堂食，麻烦尽快上菜',         CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (3,  '王芳', json_array(2,5),         '打包，不要葱蒜',             CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (4,  '李强', json_array(1,3,4),       '三人份，多加两碗米饭',       CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (5,  '刘敏', json_array(4,5),         '微辣即可，少油',             CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (6,  '陈杰', json_array(1,2),         '宫保鸡丁不要花生，过敏',     CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (7,  '杨丽', json_array(2,3,5),       '口味清淡，谢谢',             CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (8,  '赵磊', json_array(4),           '加辣加麻，越辣越好',         CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (9,  '黄勇', json_array(1,5),         '再来两瓶冰啤酒',             CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (10, '周静', json_array(2,3),         '打包带走，无需一次性餐具',   CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
