package com.example.hotspot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("hotspot")
public class Hotspot {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String title;
    private String url;
    private String source;
    private String keyword;
    private String summary;
    private String rawSummary;
    private String author;
    private Integer relevanceScore;
    private String importance;
    private String eventType;
    private String tags;
    private LocalDateTime publishedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
