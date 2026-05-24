package com.example.hotspot.vo;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 仪表盘统计数据。
 * 包含今日热点数、高重要性热点数、活跃订阅数和近 7 天趋势点集合。
 */
@Data
@AllArgsConstructor
public class DashboardStats {
    private long todayHotspots;
    private long highImportanceHotspots;
    private long activeSubscriptions;
    private List<TrendPoint> trend;

    @Data
    @AllArgsConstructor
    public static class TrendPoint {
        private String date;
        private long count;
    }
}
