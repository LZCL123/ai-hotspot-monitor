package com.example.hotspot.vo;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;

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
