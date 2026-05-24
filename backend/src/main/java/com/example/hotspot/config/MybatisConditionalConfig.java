package com.example.hotspot.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(prefix = "spring.datasource", name = "url")
@MapperScan("com.example.hotspot.mapper")
/**
 * MyBatis 条件配置。
 * 只有配置了数据库连接地址时才扫描 Mapper，方便没有数据库的本地场景跳过持久化组件。
 */
public class MybatisConditionalConfig {
}
