package com.example.hotspot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * AI 分析日志实体。
 * 记录每次 AI 分析操作的提供商、模型、输入哈希和输出结果。
 */
@Data
@TableName("ai_analysis_log")
public class AiAnalysisLog {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String provider;
    private String model;
    private String inputHash;
    private String outputJson;
    private Integer costTokens;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
