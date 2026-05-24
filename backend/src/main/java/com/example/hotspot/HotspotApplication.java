package com.example.hotspot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
/**
 * 后端应用启动类。
 * 负责启动 Spring Boot 容器，并开启定时任务能力，采集调度会依赖这里的配置生效。
 */
public class HotspotApplication {
    public static void main(String[] args) {
        SpringApplication.run(HotspotApplication.class, args);
    }
}
