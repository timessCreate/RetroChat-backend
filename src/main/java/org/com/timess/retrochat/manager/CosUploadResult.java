package org.com.timess.retrochat.manager;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 文件上传结果
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CosUploadResult {
    
    /**
     * 原始文件名
     */
    private String originalFilename;
    
    /**
     * 存储路径（COS中的Key）
     */
    private String storagePath;
    
    /**
     * 存储文件名
     */
    private String fileName;
    
    /**
     * 文件大小（字节）
     */
    private Long fileSize;
    
    /**
     * 文件类型
     */
    private String contentType;
    
    /**
     * 文件扩展名
     */
    private String fileExtension;
    
    /**
     * 文件URL
     */
    private String fileUrl;
    
    /**
     * CDN URL
     */
    private String cdnUrl;
    
    /**
     * 上传时间
     */
    private Date uploadTime;
    
    /**
     * 是否图片
     */
    private Boolean isImage;
}

