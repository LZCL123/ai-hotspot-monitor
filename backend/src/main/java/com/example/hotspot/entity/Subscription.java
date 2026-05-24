package com.example.hotspot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 订阅实体。
 * 映射用户配置的订阅关键词，包括扩展关键词、数据源、最低相关性阈值和启用状态。
 */
@Data
@TableName("subscription")
public class Subscription {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String keyword;
    private String expandedKeywords;
    private String sourceTypes;
    private Integer minRelevanceScore;
    private Integer intervalMinutes;
    private Boolean enabled;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
