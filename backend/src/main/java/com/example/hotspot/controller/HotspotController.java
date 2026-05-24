package com.example.hotspot.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.hotspot.common.ApiResponse;
import com.example.hotspot.entity.Hotspot;
import com.example.hotspot.service.CollectorOrchestrator;
import com.example.hotspot.service.HotspotService;
import com.example.hotspot.vo.DashboardStats;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 热点控制器。
 * 提供热点数据的分页查询、详情查看、趋势统计和手动触发采集刷新功能。
 */
@RestController
@RequestMapping("/api/hotspots")
@RequiredArgsConstructor
public class HotspotController {
    private final HotspotService hotspotService;
    private final CollectorOrchestrator collectorOrchestrator;

    /**
     * 分页查询热点列表
     *
     * @param keyword   关键词筛选（可选）
     * @param importance 重要性筛选（可选）
     * @param source    来源筛选（可选）
     * @param page      页码（默认 1）
     * @param size      每页条数（默认 20）
     * @return 分页热点数据
     */
    @GetMapping
    public ApiResponse<Page<Hotspot>> page(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String importance,
            @RequestParam(required = false) String source,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        // Frontend hotspot list reads paged data from the MySQL hotspot table.
        return ApiResponse.ok(hotspotService.page(keyword, importance, source, page, size));
    }

    /**
     * 获取热点详情
     *
     * @param id 热点ID
     * @return 热点详情
     */
    @GetMapping("/{id}")
    public ApiResponse<Hotspot> get(@PathVariable Long id) {
        return ApiResponse.ok(hotspotService.get(id));
    }

    /**
     * 获取仪表盘趋势统计
     *
     * @return 今日热点数、高重要性热点数、活跃订阅数和近 7 天趋势数据
     */
    @GetMapping("/trending")
    public ApiResponse<DashboardStats> trending() {
        return ApiResponse.ok(hotspotService.stats());
    }

    /**
     * 手动刷新采集
     * 触发所有订阅关键词的采集、分析和推送流程。
     *
     * @return 本次新增的热点数量
     */
    @PostMapping("/refresh")
    public ApiResponse<Integer> refresh() {
        // Manual collection entry point. It collects, analyzes, saves, then pushes WebSocket messages.
        return ApiResponse.ok(collectorOrchestrator.refreshAll());
    }
}
