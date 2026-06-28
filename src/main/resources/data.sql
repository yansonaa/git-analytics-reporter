INSERT INTO team_member (email, name, team_id, is_active, role) VALUES
('zhangsan@example.com', '张三', 'team-a', true, 'Developer'),
('lisi@example.com', '李四', 'team-a', true, 'Senior Developer'),
('wangwu@example.com', '王五', 'team-a', true, 'Tech Lead'),
('zhaoliu@example.com', '赵六', 'team-b', true, 'Developer'),
('sunqi@example.com', '孙七', 'team-b', true, 'Junior Developer');

INSERT INTO commit_record (commit_id, project_id, author_email, author_name, commit_time, add_lines, delete_lines, net_lines, file_count, message, is_merge_commit, is_automated, is_anomaly, created_at) VALUES
('a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6q7r8s9t0', 'project-1', 'zhangsan@example.com', '张三', '2026-06-25 09:30:00', 120, 20, 100, 5, '新增用户登录模块', false, false, false, '2026-06-25 09:30:00'),
('b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6q7r8s9t0u1', 'project-1', 'lisi@example.com', '李四', '2026-06-25 10:15:00', 80, 10, 70, 3, '修复订单查询bug', false, false, false, '2026-06-25 10:15:00'),
('c3d4e5f6g7h8i9j0k1l2m3n4o5p6q7r8s9t0u1v2', 'project-1', 'wangwu@example.com', '王五', '2026-06-25 14:00:00', 200, 50, 150, 8, '重构支付接口', false, false, false, '2026-06-25 14:00:00'),
('d4e5f6g7h8i9j0k1l2m3n4o5p6q7r8s9t0u1v2w3', 'project-1', 'zhangsan@example.com', '张三', '2026-06-26 09:00:00', 50, 5, 45, 2, '更新配置文件', false, false, false, '2026-06-26 09:00:00'),
('e5f6g7h8i9j0k1l2m3n4o5p6q7r8s9t0u1v2w3x4', 'project-1', 'lisi@example.com', '李四', '2026-06-26 11:30:00', 150, 30, 120, 6, '新增报表导出功能', false, false, false, '2026-06-26 11:30:00'),
('f6g7h8i9j0k1l2m3n4o5p6q7r8s9t0u1v2w3x4y5', 'project-1', 'wangwu@example.com', '王五', '2026-06-26 16:45:00', 300, 100, 200, 10, '合并feature分支', true, false, false, '2026-06-26 16:45:00'),
('g7h8i9j0k1l2m3n4o5p6q7r8s9t0u1v2w3x4y5z6', 'project-2', 'zhaoliu@example.com', '赵六', '2026-06-25 10:00:00', 90, 15, 75, 4, '初始化项目结构', false, false, false, '2026-06-25 10:00:00'),
('h8i9j0k1l2m3n4o5p6q7r8s9t0u1v2w3x4y5z6a7', 'project-2', 'sunqi@example.com', '孙七', '2026-06-25 15:20:00', 60, 8, 52, 3, '添加单元测试', false, false, false, '2026-06-25 15:20:00'),
('i9j0k1l2m3n4o5p6q7r8s9t0u1v2w3x4y5z6a7b8', 'project-2', 'zhaoliu@example.com', '赵六', '2026-06-26 09:45:00', 110, 25, 85, 5, '优化数据库查询', false, false, false, '2026-06-26 09:45:00'),
('j0k1l2m3n4o5p6q7r8s9t0u1v2w3x4y5z6a7b8c9', 'project-2', 'sunqi@example.com', '孙七', '2026-06-26 13:00:00', 40, 60, -20, 2, '回滚错误代码', false, false, true, '2026-06-26 13:00:00'),
('k1l2m3n4o5p6q7r8s9t0u1v2w3x4y5z6a7b8c9d0', 'project-1', 'zhangsan@example.com', '张三', '2026-06-27 10:00:00', 180, 40, 140, 7, '新增缓存策略', false, false, false, '2026-06-27 10:00:00'),
('l2m3n4o5p6q7r8s9t0u1v2w3x4y5z6a7b8c9d0e1', 'project-1', 'lisi@example.com', '李四', '2026-06-27 14:30:00', 220, 30, 190, 9, '集成第三方登录', false, false, false, '2026-06-27 14:30:00'),
('m3n4o5p6q7r8s9t0u1v2w3x4y5z6a7b8c9d0e1f2', 'project-1', 'wangwu@example.com', '王五', '2026-06-27 17:00:00', 500, 200, 300, 15, '大规模重构核心模块', false, false, true, '2026-06-27 17:00:00'),
('n4o5p6q7r8s9t0u1v2w3x4y5z6a7b8c9d0e1f2g3', 'project-2', 'zhaoliu@example.com', '赵六', '2026-06-27 11:00:00', 75, 10, 65, 3, '更新API文档', false, false, false, '2026-06-27 11:00:00'),
('o5p6q7r8s9t0u1v2w3x4y5z6a7b8c9d0e1f2g3h4', 'project-2', 'sunqi@example.com', '孙七', '2026-06-27 16:00:00', 55, 5, 50, 2, '修复前端兼容问题', false, false, false, '2026-06-27 16:00:00');

INSERT INTO review_record (mr_id, reviewer_email, review_time, comment_count, response_seconds, project_id, created_at) VALUES
('MR-101', 'wangwu@example.com', '2026-06-25 11:00:00', 3, 1800, 'project-1', '2026-06-25 11:00:00'),
('MR-101', 'lisi@example.com', '2026-06-25 11:30:00', 2, 3600, 'project-1', '2026-06-25 11:30:00'),
('MR-102', 'zhangsan@example.com', '2026-06-26 10:00:00', 5, 900, 'project-1', '2026-06-26 10:00:00'),
('MR-103', 'wangwu@example.com', '2026-06-26 17:00:00', 1, 7200, 'project-1', '2026-06-26 17:00:00'),
('MR-201', 'zhaoliu@example.com', '2026-06-25 16:00:00', 4, 1200, 'project-2', '2026-06-25 16:00:00'),
('MR-202', 'sunqi@example.com', '2026-06-26 14:00:00', 2, 2400, 'project-2', '2026-06-26 14:00:00'),
('MR-203', 'zhaoliu@example.com', '2026-06-27 12:00:00', 6, 600, 'project-2', '2026-06-27 12:00:00');
