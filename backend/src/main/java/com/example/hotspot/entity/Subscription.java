package com.example.hotspot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

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
