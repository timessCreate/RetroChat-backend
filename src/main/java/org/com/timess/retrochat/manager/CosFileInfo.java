package org.com.timess.retrochat.manager;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 文件信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CosFileInfo {
    
    /**
     * COS中的Key
     */
    private String key;
    
    /**
     * 文件名
     */
    private String fileName;
    
    /**
     * 文件大小
     */
    private Long fileSize;
    
    /**
     * 文件类型
     */
    private String contentType;
    
    /**
     * 最后修改时间
     */
    private Date lastModified;
    
    /**
     * ETag
     */
    private String eTag;
    
//    /**
//     * 存储类型
//     */
//    private String storageClass;
    
    /**
     * 访问URL
     */
    private String url;
}