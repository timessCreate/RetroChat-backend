package org.com.timess.retrochat.config;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.region.Region;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@ConfigurationProperties(prefix = "cos.client")
@Data
public class CosClientConfig {

    /**
     * 存储桶名称
     */
    private String bucket;

    /**
     * host域名（不要包含https://前缀）
     */
    private String host;

    /**
     * secretId
     */
    private String secretId;

    /**
     * 区域
     */
    private String region;

    /**
     * 密钥
     */
    private String secretKey;

    @Bean
    public COSClient cosClient() {
        log.info("🔧 初始化COS客户端配置...");
        log.info("   - Bucket: {}", bucket);
        log.info("   - Host: {}", host);
        log.info("   - Region: {}", region);
        log.info("   - SecretId: {}", secretId != null ? secretId.substring(0, Math.min(8, secretId.length())) + "..." : "未设置");

        // 1. 处理host，移除可能存在的协议前缀
        String cleanedHost = cleanHost(host);
        log.info("   - 清理后Host: {}", cleanedHost);

        // 2. 初始化身份信息
        COSCredentials cred = new BasicCOSCredentials(secretId, secretKey);

        // 3. 设置客户端配置
        ClientConfig clientConfig = new ClientConfig(new Region(region));

        // 4. 设置自定义端点构建器
        SelfDefinedEndpointBuilder endpointBuilder = new SelfDefinedEndpointBuilder(region, cleanedHost);
        clientConfig.setEndpointBuilder(endpointBuilder);

        // 5. 生成COS客户端
        COSClient cosClient = new COSClient(cred, clientConfig);

        log.info("✅ COS客户端初始化完成");
        return cosClient;
    }

    private String cleanHost(String host) {
        if (host == null) {
            return null;
        }

        // 移除可能存在的协议前缀
        String cleaned = host.replaceAll("^https?://", "");

        // 移除末尾的斜杠
        cleaned = cleaned.replaceAll("/$", "");

        return cleaned;
    }
}