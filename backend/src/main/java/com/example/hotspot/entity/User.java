package com.example.hotspot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 用户实体。
 * 保存系统用户信息，支持多用户登录和权限管理。
 */
@Data
@TableName("user")
public class User {
    /** 用户ID，自增主键 */
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 用户名，唯一 */
    private String username;
    /** 密码哈希值（BCrypt） */
    @TableField("password_hash")
    private String passwordHash;
    /** 用户角色：ADMIN/USER */
    private String role;
    /** 创建时间 */
    @TableField("created_at")
    private LocalDateTime createdAt;
    /** 更新时间 */
    @TableField("updated_at")
    private LocalDateTime updatedAt;
}
