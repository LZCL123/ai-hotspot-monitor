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

@RestController
@RequestMapping("/api/hotspots")
@RequiredArgsConstructor
public class HotspotController {
    private final HotspotService hotspotService;
    private final CollectorOrchestrator collectorOrchestrator;

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

    @GetMapping("/{id}")
    public ApiResponse<Hotspot> get(@PathVariable Long id) {
        return ApiResponse.ok(hotspotService.get(id));
    }

    @GetMapping("/trending")
    public ApiResponse<DashboardStats> trending() {
        return ApiResponse.ok(hotspotService.stats());
    }

    @PostMapping("/refresh")
    public ApiResponse<Integer> refresh() {
        // Manual collection entry point. It collects, analyzes, saves, then pushes WebSocket messages.
        return ApiResponse.ok(collectorOrchestrator.refreshAll());
    }
}
