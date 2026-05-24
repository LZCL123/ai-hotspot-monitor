package com.example.hotspot.collector;

import java.util.List;

/**
 * 热点采集器接口。
 * 每个数据源实现都需要声明自己的来源标识，并按关键词返回候选热点列表。
 */
public interface HotspotCollector {
    String source();

    List<CollectedItem> collect(String keyword);
}
