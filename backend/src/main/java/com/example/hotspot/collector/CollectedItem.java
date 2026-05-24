package com.example.hotspot.collector;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
/**
 * 采集阶段的统一数据对象。
 * 不同数据源会被转换成这个结构，方便后续去重、AI 分析和入库处理使用同一套字段。
 */
public class CollectedItem {
    private String source;
    private String title;
    private String url;
    private String summary;
    private String author;
    private LocalDateTime publishedAt;
    private String rawContent;
}
