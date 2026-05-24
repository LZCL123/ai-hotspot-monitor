package com.example.hotspot.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Data
@Configuration
@ConfigurationProperties(prefix = "app")
/**
 * 应用配置映射类。
 * 负责把 application.yml 和环境变量里的 app 配置绑定成 Java 对象，供认证、AI、采集和跨域配置使用。
 */
public class AppProperties {
    // 对应 application.yml 中的 app.auth，用来配置后台登录账号和密码。
    private Auth auth = new Auth();
    // 主 AI 服务配置，当前默认使用阿里百炼兼容接口。
    private Ai ai = new Ai();
    // 兼容旧版 OpenRouter 配置，避免已有部署升级后立即失效。
    private OpenRouter openrouter = new OpenRouter();
    // 采集器的默认间隔、超时时间和最大重试次数。
    private Collector collector = new Collector();
    // 允许调用后端接口的前端来源地址。
    private Cors cors = new Cors();

    @Data
    /**
     * 登录认证配置。
     * 保存后台管理账号和密码，AuthService 会用它校验登录请求。
     */
    public static class Auth {
        private String username;
        private String password;
    }

    @Data
    /**
     * AI 服务配置。
     * 保存服务商、接口密钥、模型名称和兼容 OpenAI 格式的请求地址。
     */
    public static class Ai {
        private String provider = "bailian";
        // 从 DASHSCOPE_API_KEY 或 BAILIAN_API_KEY 环境变量读取。
        private String apiKey;
        private String model = "qwen-plus";
        // 百炼兼容 OpenAI Chat Completions 的接口地址。
        private String baseUrl = "https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions";
    }

    @Data
    /**
     * OpenRouter 兼容配置。
     * 继承通用 AI 配置结构，主要用于保留旧部署里的配置项。
     */
    public static class OpenRouter extends Ai {
    }

    @Data
    /**
     * 采集器配置。
     * 控制自动采集间隔、单次请求超时和失败重试次数。
     */
    public static class Collector {
        private int defaultIntervalMinutes = 30;
        private int timeoutSeconds = 8;
        private int maxRetries = 2;
    }

    @Data
    /**
     * 跨域配置。
     * 列出允许访问后端接口的前端地址，供 CORS 配置类读取。
     */
    public static class Cors {
        private List<String> allowedOrigins = List.of("http://localhost:5173");
    }
}
