package com.example.hotspot.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.hotspot.common.BizException;
import com.example.hotspot.entity.Hotspot;
import com.example.hotspot.mapper.HotspotMapper;
import com.example.hotspot.vo.DashboardStats;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class HotspotService {
    private final HotspotMapper mapper;
    private final SubscriptionService subscriptionService;

    public Page<Hotspot> page(String keyword, String importance, String source, int page, int size) {
        LambdaQueryWrapper<Hotspot> wrapper = new LambdaQueryWrapper<Hotspot>()
                .like(StringUtils.hasText(keyword), Hotspot::getKeyword, keyword)
                .eq(StringUtils.hasText(importance), Hotspot::getImportance, importance)
                .eq(StringUtils.hasText(source), Hotspot::getSource, source)
                .orderByDesc(Hotspot::getCreatedAt);
        return mapper.selectPage(Page.of(page, size), wrapper);
    }

    public Hotspot get(Long id) {
        Hotspot hotspot = mapper.selectById(id);
        if (hotspot == null) {
            throw new BizException("热点不存在");
        }
        return hotspot;
    }

    public boolean existsByUrl(String url) {
        return mapper.exists(new LambdaQueryWrapper<Hotspot>().eq(Hotspot::getUrl, url));
    }

    public boolean existsSimilarTitle(String keyword, String title) {
        List<Hotspot> recent = mapper.selectList(new LambdaQueryWrapper<Hotspot>()
                .eq(Hotspot::getKeyword, keyword)
                .ge(Hotspot::getCreatedAt, LocalDateTime.now().minusDays(7))
                .last("LIMIT 50"));
        return recent.stream().anyMatch(item -> similarity(item.getTitle(), title) >= 0.82);
    }

    public void insert(Hotspot hotspot) {
        mapper.insert(hotspot);
    }

    public DashboardStats stats() {
        LocalDateTime today = LocalDate.now().atStartOfDay();
        long todayCount = mapper.selectCount(new LambdaQueryWrapper<Hotspot>().ge(Hotspot::getCreatedAt, today));
        long highCount = mapper.selectCount(new LambdaQueryWrapper<Hotspot>().in(Hotspot::getImportance, List.of("HIGH", "CRITICAL")));
        long activeCount = subscriptionService.enabledList().size();
        List<DashboardStats.TrendPoint> trend = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            LocalDateTime start = date.atStartOfDay();
            LocalDateTime end = date.plusDays(1).atStartOfDay();
            long count = mapper.selectCount(new LambdaQueryWrapper<Hotspot>()
                    .ge(Hotspot::getCreatedAt, start)
                    .lt(Hotspot::getCreatedAt, end));
            trend.add(new DashboardStats.TrendPoint(date.toString(), count));
        }
        return new DashboardStats(todayCount, highCount, activeCount, trend);
    }

    private double similarity(String left, String right) {
        String a = normalize(left);
        String b = normalize(right);
        if (a.equals(b)) {
            return 1.0;
        }
        int distance = levenshtein(a, b);
        return 1.0 - (double) distance / Math.max(a.length(), b.length());
    }

    private String normalize(String value) {
        return value == null ? "" : value.toLowerCase().replaceAll("[^a-z0-9\\u4e00-\\u9fa5]", "");
    }

    private int levenshtein(String a, String b) {
        int[][] dp = new int[a.length() + 1][b.length() + 1];
        for (int i = 0; i <= a.length(); i++) dp[i][0] = i;
        for (int j = 0; j <= b.length(); j++) dp[0][j] = j;
        for (int i = 1; i <= a.length(); i++) {
            for (int j = 1; j <= b.length(); j++) {
                int cost = a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1;
                dp[i][j] = Math.min(Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1), dp[i - 1][j - 1] + cost);
            }
        }
        return dp[a.length()][b.length()];
    }
}
