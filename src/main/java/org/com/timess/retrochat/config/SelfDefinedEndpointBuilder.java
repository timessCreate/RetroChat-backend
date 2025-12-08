package org.com.timess.retrochat.config;

import com.qcloud.cos.endpoint.EndpointBuilder;
import com.qcloud.cos.region.Region;

/**
 * cos存储配置类
 */

class SelfDefinedEndpointBuilder implements EndpointBuilder {
    /**
     * 存储桶地区
     */
    private final String region;

    /**
     * 地址
     */
    private final String domain;

    public SelfDefinedEndpointBuilder(String region, String domain) {
        super();
        // 格式化 Region
        this.region = Region.formatRegion(new Region(region));
        this.domain = domain;
    }

    @Override
    public String buildGeneralApiEndpoint(String bucketName) {
        // 构造 Endpoint
        String endpoint = String.format("%s.%s", this.region, this.domain);
        // 构造 Bucket 访问域名
        return String.format("%s.%s", bucketName, endpoint);
    }

    @Override
    public String buildGetServiceApiEndpoint() {
        return String.format("%s.%s", this.region, this.domain);
    }
}