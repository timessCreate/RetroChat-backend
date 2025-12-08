package org.com.timess.retrochat.manager;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.exception.CosClientException;
import com.qcloud.cos.exception.CosServiceException;
import com.qcloud.cos.http.HttpMethodName;
import com.qcloud.cos.model.*;
import com.qcloud.cos.transfer.TransferManager;
import com.qcloud.cos.transfer.TransferManagerConfiguration;
import com.qcloud.cos.utils.IOUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.com.timess.retrochat.config.CosClientConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.io.*;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 腾讯云COS文件管理器
 */
@Slf4j
@Component
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class CosManager {

    @Resource
    private COSClient cosClient;

    @Resource
    CosClientConfig cosClientConfig;

    @Resource
    CosProperties cosProperties;
    
    private TransferManager transferManager;

    private ExecutorService threadPool;

    // 图片文件扩展名
    private static final Set<String> IMAGE_EXTENSIONS = new HashSet<>(Arrays.asList(
            "jpg", "jpeg", "png", "gif", "bmp", "webp", "svg", "ico"
    ));

    // 视频文件扩展名
    private static final Set<String> VIDEO_EXTENSIONS = new HashSet<>(Arrays.asList(
            "mp4", "avi", "mov", "wmv", "flv", "mkv", "webm"
    ));

    // 文档文件扩展名
    private static final Set<String> DOCUMENT_EXTENSIONS = new HashSet<>(Arrays.asList(
            "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt"
    ));

    /**
     * 初始化COS客户端
     */
    @PostConstruct
    public void init() {
        log.info("🚀 初始化COS客户端...");

        try {

            // 4. 初始化TransferManager
            threadPool = Executors.newFixedThreadPool(5);
            TransferManagerConfiguration transferManagerConfiguration = new TransferManagerConfiguration();
            transferManagerConfiguration.setMultipartUploadThreshold(5 * 1024 * 1024);  // 5MB
            transferManagerConfiguration.setMinimumUploadPartSize(1024 * 1024);  // 1MB

            transferManager = new TransferManager(cosClient, threadPool);
            transferManagerConfiguration = transferManager.getConfiguration();
            log.info("✅ COS客户端初始化成功");

        } catch (Exception e) {
            log.error("❌ COS客户端初始化失败", e);
            throw new RuntimeException("COS客户端初始化失败", e);
        }
    }

    /**
     * 销毁客户端
     */
    @PreDestroy
    public void destroy() {
        log.info("🔧 关闭COS客户端...");
        if (transferManager != null) {
            transferManager.shutdownNow();
        }
        if (cosClient != null) {
            cosClient.shutdown();
        }
        if (threadPool != null) {
            threadPool.shutdown();
        }
        log.info("✅ COS客户端已关闭");
    }

    // ==================== 上传方法 ====================

    /**
     * 上传文件到指定目录
     *
     * @param file 文件
     * @param directory 目录路径，如 "images/2023/01/"
     * @return 文件访问URL
     */
    public CosUploadResult uploadFile(MultipartFile file, String directory) throws IOException {
        return uploadFile(file, directory, generateFileName(file.getOriginalFilename()));
    }

    /**
     * 上传文件到指定目录（自定义文件名）
     *
     * @param file 文件
     * @param directory 目录路径
     * @param fileName 文件名
     * @return 上传结果
     */
    public CosUploadResult uploadFile(MultipartFile file, String directory, String fileName) throws IOException {
        log.info("📤 上传文件: {} 到目录: {}", file.getOriginalFilename(), directory);

        // 验证文件
        validateFile(file);

        // 生成存储路径
        String key = buildStoragePath(directory, fileName);

        // 获取文件类型
        String contentType = file.getContentType();
        String fileExtension = getFileExtension(file.getOriginalFilename()).toLowerCase();

        // 上传文件
        try (InputStream inputStream = file.getInputStream()) {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(file.getSize());
            metadata.setContentType(contentType);

            // 设置公共读权限
            PutObjectRequest putObjectRequest = new PutObjectRequest(
                    cosClientConfig.getBucket(),
                    key,
                    inputStream,
                    metadata
            );

            // 设置ACL
            putObjectRequest.setCannedAcl(CannedAccessControlList.PublicRead);

            // 执行上传
            cosClient.putObject(putObjectRequest);

            // 构建返回结果
            String fileUrl = getFileUrl(key);

            CosUploadResult result = CosUploadResult.builder()
                    .originalFilename(file.getOriginalFilename())
                    .storagePath(key)
                    .fileName(fileName)
                    .fileSize(file.getSize())
                    .contentType(contentType)
                    .fileExtension(fileExtension)
                    .fileUrl(fileUrl)
                    .uploadTime(new Date())
                    .isImage(IMAGE_EXTENSIONS.contains(fileExtension))
                    .build();

            log.info("✅ 文件上传成功: {}", result.getFileUrl());
            return result;

        } catch (CosServiceException e) {
            log.error("❌ COS服务异常: {}", e.getErrorMessage(), e);
            throw new RuntimeException("COS服务异常: " + e.getErrorMessage(), e);
        } catch (CosClientException e) {
            log.error("❌ COS客户端异常", e);
            throw new RuntimeException("COS客户端异常", e);
        }
    }

    /**
     * 上传图片到图片目录
     */
    public CosUploadResult uploadImage(MultipartFile imageFile) throws IOException {
        // 生成图片路径：uploads/images/年/月/
        String datePath = new SimpleDateFormat("yyyy/MM").format(new Date());
        String directory = cosProperties.getPaths().getImages() + datePath + "/";

        return uploadFile(imageFile, directory);
    }

    /**
     * 上传文档到文档目录
     */
    public CosUploadResult uploadDocument(MultipartFile documentFile) throws IOException {
        String datePath = new SimpleDateFormat("yyyy/MM").format(new Date());
        String directory = cosProperties.getPaths().getDocuments() + datePath + "/";

        return uploadFile(documentFile, directory);
    }

    /**
     * 上传临时文件
     */
    public CosUploadResult uploadTempFile(MultipartFile file) throws IOException {
        String directory = cosProperties.getPaths().getTemp();
        String fileName = "temp_" + System.currentTimeMillis() + "_" + generateFileName(file.getOriginalFilename());

        return uploadFile(file, directory, fileName);
    }

    /**
     * 上传字节数据
     */
    public CosUploadResult uploadBytes(byte[] bytes, String directory, String fileName, String contentType) {
        log.info("📤 上传字节数据: {} bytes 到目录: {}", bytes.length, directory);

        String key = buildStoragePath(directory, fileName);

        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes)) {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(bytes.length);
            metadata.setContentType(contentType);

            PutObjectRequest putObjectRequest = new PutObjectRequest(
                    cosClientConfig.getBucket(),
                    key,
                    inputStream,
                    metadata
            );

            putObjectRequest.setCannedAcl(CannedAccessControlList.PublicRead);

            cosClient.putObject(putObjectRequest);

            CosUploadResult result = CosUploadResult.builder()
                    .storagePath(key)
                    .fileName(fileName)
                    .fileSize((long) bytes.length)
                    .contentType(contentType)
                    .fileExtension(getFileExtension(fileName))
                    .fileUrl(getFileUrl(key))
                    .uploadTime(new Date())
                    .build();

            log.info("✅ 字节数据上传成功: {}", result.getFileUrl());
            return result;

        } catch (Exception e) {
            log.error("❌ 字节数据上传失败", e);
            throw new RuntimeException("字节数据上传失败", e);
        }
    }

    // ==================== 读取方法 ====================

    /**
     * 读取文件为字节数组
     */
    public byte[] getFileBytes(String fileKey) {
        log.info("📥 读取文件: {}", fileKey);

        try {
            GetObjectRequest getObjectRequest = new GetObjectRequest(cosClientConfig.getBucket(), fileKey);
            COSObject cosObject = cosClient.getObject(getObjectRequest);

            try (InputStream inputStream = cosObject.getObjectContent()) {
                return IOUtils.toByteArray(inputStream);
            }

        } catch (Exception e) {
            log.error("❌ 读取文件失败: {}", fileKey, e);
            throw new RuntimeException("读取文件失败: " + fileKey, e);
        }
    }

    /**
     * 读取文件为输入流
     */
    public InputStream getFileStream(String fileKey) {
        log.info("📥 获取文件流: {}", fileKey);

        try {
            GetObjectRequest getObjectRequest = new GetObjectRequest(cosClientConfig.getBucket(), fileKey);
            COSObject cosObject = cosClient.getObject(getObjectRequest);
            return cosObject.getObjectContent();

        } catch (Exception e) {
            log.error("❌ 获取文件流失败: {}", fileKey, e);
            throw new RuntimeException("获取文件流失败: " + fileKey, e);
        }
    }

    /**
     * 读取图片文件
     */
    public byte[] getImage(String fileKey) {
        return getFileBytes(fileKey);
    }

    /**
     * 获取文件URL
     */
    public String getFileUrl(String fileKey) {

        return String.format("https://%s.cos.%s.myqcloud.com/%s",
                cosClientConfig.getBucket(),
                cosClientConfig.getRegion(),
                fileKey
        );
    }

    /**
     * 获取带签名的临时URL（过期时间）
     */
    public String getSignedUrl(String fileKey, int expireMinutes) {
        log.info("🔐 生成签名URL: {}, 过期时间: {}分钟", fileKey, expireMinutes);

        try {
            Date expiration = new Date(System.currentTimeMillis() + expireMinutes * 60 * 1000L);
            GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(
                    cosClientConfig.getBucket(),
                    fileKey,
                    HttpMethodName.GET
            );
            request.setExpiration(expiration);

            URL url = cosClient.generatePresignedUrl(request);
            return url.toString();

        } catch (Exception e) {
            log.error("❌ 生成签名URL失败: {}", fileKey, e);
            return getFileUrl(fileKey);
        }
    }

    // ==================== 管理方法 ====================

    /**
     * 检查文件是否存在
     */
    public boolean fileExists(String fileKey) {
        try {
            cosClient.getObjectMetadata(cosClientConfig.getBucket(), fileKey);
            return true;
        } catch (CosClientException e) {
            throw e;
        }
    }

    /**
     * 删除文件
     */
    public boolean deleteFile(String fileKey) {
        log.info("🗑️ 删除文件: {}", fileKey);

        try {
            cosClient.deleteObject(cosClientConfig.getBucket(), fileKey);
            log.info("✅ 文件删除成功: {}", fileKey);
            return true;
        } catch (Exception e) {
            log.error("❌ 文件删除失败: {}", fileKey, e);
            return false;
        }
    }

    /**
     * 批量删除文件
     */
    public int deleteFiles(List<String> fileKeys) {
        if (fileKeys == null || fileKeys.isEmpty()) {
            return 0;
        }

        int successCount = 0;
        List<DeleteObjectsRequest.KeyVersion> deleteKeys = new ArrayList<>();

        for (String key : fileKeys) {
            deleteKeys.add(new DeleteObjectsRequest.KeyVersion(key));
        }

        try {
            DeleteObjectsRequest deleteObjectsRequest = new DeleteObjectsRequest(cosClientConfig.getBucket());
            deleteObjectsRequest.setKeys(deleteKeys);
            DeleteObjectsResult deleteObjectsResult = cosClient.deleteObjects(deleteObjectsRequest);

            successCount = deleteObjectsResult.getDeletedObjects().size();
            log.info("✅ 批量删除成功: {}/{} 个文件", successCount, fileKeys.size());

        } catch (Exception e) {
            log.error("❌ 批量删除失败", e);
        }

        return successCount;
    }

    /**
     * 获取文件信息
     */
    public CosFileInfo getFileInfo(String fileKey) {
        try {
            ObjectMetadata metadata = cosClient.getObjectMetadata(cosClientConfig.getBucket(), fileKey);

            return CosFileInfo.builder()
                    .key(fileKey)
                    .fileName(getFileNameFromPath(fileKey))
                    .fileSize(metadata.getContentLength())
                    .contentType(metadata.getContentType())
                    .lastModified(metadata.getLastModified())
                    .eTag(metadata.getETag())
                    .url(getFileUrl(fileKey))
                    .build();

        } catch (Exception e) {
            log.error("❌ 获取文件信息失败: {}", fileKey, e);
            return null;
        }
    }

    /**
     * 复制文件
     */
    public boolean copyFile(String sourceKey, String destinationKey) {
        log.info("📋 复制文件: {} -> {}", sourceKey, destinationKey);

        try {
            CopyObjectRequest copyObjectRequest = new CopyObjectRequest(
                    cosClientConfig.getBucket(),
                    sourceKey,
                    cosClientConfig.getBucket(),
                    destinationKey
            );

            cosClient.copyObject(copyObjectRequest);
            log.info("✅ 文件复制成功");
            return true;

        } catch (Exception e) {
            log.error("❌ 文件复制失败", e);
            return false;
        }
    }

    /**
     * 移动文件（复制+删除）
     */
    public boolean moveFile(String sourceKey, String destinationKey) {
        if (copyFile(sourceKey, destinationKey)) {
            return deleteFile(sourceKey);
        }
        return false;
    }

    // ==================== 工具方法 ====================

    /**
     * 生成随机文件名
     */
    private String generateFileName(String originalFilename) {
        String extension = getFileExtension(originalFilename);
        String uuid = UUID.randomUUID().toString().replace("-", "");
        return uuid + "." + extension;
    }

    /**
     * 生成日期路径
     */
    private String generateDatePath() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
        return sdf.format(new Date()) + "/";
    }

    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String filename) {
        if (StringUtils.isBlank(filename)) {
            return "";
        }
        int dotIndex = filename.lastIndexOf(".");
        if (dotIndex > 0) {
            return filename.substring(dotIndex + 1);
        }
        return "";
    }

    /**
     * 从路径中获取文件名
     */
    private String getFileNameFromPath(String path) {
        if (StringUtils.isBlank(path)) {
            return "";
        }
        int slashIndex = path.lastIndexOf("/");
        if (slashIndex >= 0) {
            return path.substring(slashIndex + 1);
        }
        return path;
    }

    /**
     * 构建存储路径
     */
    private String buildStoragePath(String directory, String fileName) {
        // 确保目录以 / 结尾
        String dir = directory;
        if (StringUtils.isNotBlank(dir) && !dir.endsWith("/")) {
            dir = dir + "/";
        }

        // 生成完整路径
        return (dir == null ? "" : dir) + fileName;
    }


    /**
     * 验证文件
     */
    private void validateFile(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }

        // 检查文件大小
        long maxSize = parseSize(cosProperties.getUpload().getMaxSize());
        if (file.getSize() > maxSize) {
            throw new IllegalArgumentException("文件大小不能超过 " + cosProperties.getUpload().getMaxSize());
        }

        //TODO: bug:不支持svg
//        // 检查文件扩展名
//        String extension = getFileExtension(file.getOriginalFilename()).toLowerCase();
//        Set<String> allowedExtensions = cosProperties.getUpload().getAllowedExtensionSet();
//
//        if (!allowedExtensions.isEmpty() && !allowedExtensions.contains(extension)) {
//            throw new IllegalArgumentException("不支持的文件类型: " + extension);
//        }
    }

    /**
     * 解析文件大小字符串
     */
    private long parseSize(String sizeStr) {
        if (StringUtils.isBlank(sizeStr)) {
            return 10 * 1024 * 1024; // 默认10MB
        }

        sizeStr = sizeStr.trim().toUpperCase();
        if (sizeStr.endsWith("B")) {
            sizeStr = sizeStr.substring(0, sizeStr.length() - 1);
        }

        if (sizeStr.endsWith("K")) {
            return (long) (Double.parseDouble(sizeStr.substring(0, sizeStr.length() - 1)) * 1024);
        } else if (sizeStr.endsWith("M")) {
            return (long) (Double.parseDouble(sizeStr.substring(0, sizeStr.length() - 1)) * 1024 * 1024);
        } else if (sizeStr.endsWith("G")) {
            return (long) (Double.parseDouble(sizeStr.substring(0, sizeStr.length() - 1)) * 1024 * 1024 * 1024);
        } else {
            return Long.parseLong(sizeStr);
        }
    }

    /**
     * 判断是否为图片文件
     */
    public boolean isImageFile(String filename) {
        String extension = getFileExtension(filename).toLowerCase();
        return IMAGE_EXTENSIONS.contains(extension);
    }

    /**
     * 判断是否为视频文件
     */
    public boolean isVideoFile(String filename) {
        String extension = getFileExtension(filename).toLowerCase();
        return VIDEO_EXTENSIONS.contains(extension);
    }

    /**
     * 判断是否为文档文件
     */
    public boolean isDocumentFile(String filename) {
        String extension = getFileExtension(filename).toLowerCase();
        return DOCUMENT_EXTENSIONS.contains(extension);
    }
}