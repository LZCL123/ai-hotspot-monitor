package com.example.hotspot.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@ConditionalOnProperty(prefix = "app", name = "db.enabled", havingValue = "true", matchIfMissing = false)
@EnableScheduling
/**
 * 定时任务条件配置。
 * 当数据库功能开启时启用调度能力，避免无数据库启动模式下触发采集任务。
 */
public class SchedulingConditionalConfig {
}
