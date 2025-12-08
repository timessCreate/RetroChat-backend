package org.com.timess.retrochat.manager;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import javax.annotation.PostConstruct;
import java.util.HashSet;
import java.util.Set;

@Data
@Component
@ConfigurationProperties(prefix = "cos")
@Validated
public class CosProperties {

    private Upload upload = new Upload();
    private Paths paths = new Paths();

    @PostConstruct
    public void init() {
        // 确保不为空
        if (upload == null) upload = new Upload();
        if (paths == null) paths = new Paths();

        // 输出配置信息
        System.out.println("COS配置初始化完成");
        System.out.println("最大文件大小: " + upload.getMaxSize());
    }

    @Data
    public static class Upload {
        @JsonProperty("max-size")
        private String maxSize = "10MB";

        @JsonProperty("allowed-extensions")
        private String allowedExtensions = "jpg,jpeg,png,gif,bmp,webp,pdf,doc,docx,xls,xlsx,txt";

        @JsonProperty("image-extensions")
        private String imageExtensions = "jpg,jpeg,png,gif,bmp,webp";

        @JsonProperty("temp-dir")
        private String tempDir = "/tmp/cos-uploads";

        public Set<String> getAllowedExtensionSet() {
            return splitToSet(allowedExtensions);
        }

        public Set<String> getImageExtensionSet() {
            return splitToSet(imageExtensions);
        }

        public Set<String> splitToSet(String str) {
            Set<String> extensions = new HashSet<>();
            if (str != null && !str.trim().isEmpty()) {
                for (String ext : str.split(",")) {
                    extensions.add(ext.trim().toLowerCase());
                }
            }
            return extensions;
        }
    }

    @Data
    public static class Paths {
        private String images = "uploads/images/";
        private String documents = "uploads/docs/";
        private String temp = "uploads/temp/";
    }
}