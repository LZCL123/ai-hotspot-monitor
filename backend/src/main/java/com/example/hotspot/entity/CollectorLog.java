package com.example.hotspot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 采集日志实体。
 * 记录每次订阅关键词采集的执行状态、来源、耗时和结果消息。
 */
@Data
@TableName("collector_log")
public class CollectorLog {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String source;
    private String keyword;
    private String status;
    private String message;
    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
