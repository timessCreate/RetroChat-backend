package org.com.timess.retrochat.utils;

import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

/**
 * 图片URL验证、下载和转换工具类
 */
public class ImageDownloadValidator {

    // 支持的图片扩展名
    private static final Set<String> SUPPORTED_EXTENSIONS = new HashSet<>(
            Arrays.asList("jpg", "jpeg", "png", "gif", "bmp", "webp", "ico", "tiff", "svg")
    );

    // 支持的MIME类型
    private static final Set<String> SUPPORTED_MIME_TYPES = new HashSet<>(
            Arrays.asList("image/jpeg", "image/png", "image/gif", "image/bmp",
                    "image/webp", "image/x-icon", "image/tiff", "image/svg+xml")
    );

    // 用户代理
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36";

    // 连接超时（毫秒）
    private static final int CONNECT_TIMEOUT = 10000;
    // 读取超时（毫秒）
    private static final int READ_TIMEOUT = 30000;

    /**
     * 验证URL是否为有效的图片URL
     */
    public static ValidationResult validateImageUrl(String imageUrl) {
        ValidationResult result = new ValidationResult();
        result.setUrl(imageUrl);

        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            result.setValid(false);
            result.setMessage("URL不能为空");
            return result;
        }

        try {
            URL url = new URL(imageUrl);

            // 验证协议
            String protocol = url.getProtocol();
            if (!"http".equalsIgnoreCase(protocol) && !"https".equalsIgnoreCase(protocol)) {
                result.setValid(false);
                result.setMessage("不支持的协议: " + protocol);
                return result;
            }

            // 验证扩展名
            String extension = getFileExtensionFromUrl(imageUrl);
            if (extension == null || !SUPPORTED_EXTENSIONS.contains(extension.toLowerCase())) {
                result.setValid(false);
                result.setMessage("不支持的图片格式");
                return result;
            }

            result.setExtension(extension);

        } catch (Exception e) {
            result.setValid(false);
            result.setMessage("URL格式错误: " + e.getMessage());
            return result;
        }

        result.setValid(true);
        result.setMessage("URL验证通过");
        return result;
    }

    /**
     * 验证并下载图片，转换为MultipartFile
     * @param imageUrl 图片URL
     * @param saveToFile 是否保存为文件（false时只保存在内存）
     * @return 下载结果
     */
    public static DownloadResult downloadImage(String imageUrl, boolean saveToFile) {
        DownloadResult result = new DownloadResult();
        result.setUrl(imageUrl);

        // 1. 验证URL
        ValidationResult validationResult = validateImageUrl(imageUrl);
        if (!validationResult.isValid()) {
            result.setSuccess(false);
            result.setMessage("URL验证失败: " + validationResult.getMessage());
            return result;
        }

        HttpURLConnection connection = null;
        InputStream inputStream = null;
        ByteArrayOutputStream baos = null;
        File tempFile = null;

        try {
            URL url = new URL(imageUrl);

            // 2. 建立连接
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", USER_AGENT);
            connection.setConnectTimeout(CONNECT_TIMEOUT);
            connection.setReadTimeout(READ_TIMEOUT);
            connection.setInstanceFollowRedirects(true);

            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                result.setSuccess(false);
                result.setMessage("HTTP错误: " + responseCode);
                return result;
            }

            // 3. 验证Content-Type
            String contentType = connection.getContentType();
            if (contentType == null || !isImageContentType(contentType)) {
                result.setSuccess(false);
                result.setMessage("不是图片文件: " + contentType);
                return result;
            }

            // 4. 获取文件大小
            long fileSize = connection.getContentLengthLong();
            if (fileSize <= 0) {
                fileSize = 0;
            }

            // 5. 获取文件名
            String extension = validationResult.getExtension();
            String originalFilename = getOriginalFilename(connection, url, extension);

            // 6. 下载文件数据
            inputStream = connection.getInputStream();

            if (saveToFile) {
                // 保存到临时文件
                tempFile = saveToTempFile(inputStream, originalFilename, extension);
                if (tempFile == null) {
                    result.setSuccess(false);
                    result.setMessage("文件保存失败");
                    return result;
                }
                result.setDownloadedFile(tempFile);
            } else {
                // 保存到内存
                baos = new ByteArrayOutputStream();
                byte[] buffer = new byte[8192];
                int bytesRead;
                long totalBytesRead = 0;

                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    baos.write(buffer, 0, bytesRead);
                    totalBytesRead += bytesRead;

                    // 检查文件大小限制（10MB）
                    if (totalBytesRead > 10 * 1024 * 1024) { // 10MB限制
                        result.setSuccess(false);
                        result.setMessage("文件大小超过10MB限制");
                        return result;
                    }
                }

                byte[] fileData = baos.toByteArray();
                result.setFileData(fileData);
            }

            // 7. 验证是否为有效图片
            boolean isImageValid = saveToFile ?
                    ImageValidatorUtils.isImage(tempFile) :
                    isValidImageData(result.getFileData());

            if (!isImageValid) {
                if (tempFile != null && tempFile.exists()) {
                    tempFile.delete();
                }
                result.setSuccess(false);
                result.setMessage("下载的文件不是有效的图片");
                return result;
            }

            // 8. 转换为MultipartFile
            String finalFilename = originalFilename;
            byte[] finalFileData = saveToFile ? null : result.getFileData();

            MultipartFile multipartFile = new CustomMultipartFile(
                    finalFilename,
                    originalFilename,
                    contentType,
                    saveToFile ? null : finalFileData,
                    tempFile
            );

            result.setMultipartFile(multipartFile);

            // 9. 返回成功结果
            result.setSuccess(true);
            result.setMessage("图片下载成功");
            result.setFileSize(fileSize);
            result.setContentType(contentType);
            result.setFilename(originalFilename);

        } catch (Exception e) {
            // 清理
            if (tempFile != null && tempFile.exists()) {
                tempFile.delete();
            }

            result.setSuccess(false);
            result.setMessage("下载失败: " + e.getMessage());

        } finally {
            // 关闭资源
            if (inputStream != null) {
                try { inputStream.close(); } catch (IOException e) {}
            }
            if (baos != null) {
                try { baos.close(); } catch (IOException e) {}
            }
            if (connection != null) {
                connection.disconnect();
            }
        }
        return result;
    }

    /**
     * 下载图片并返回MultipartFile（默认保存到内存）
     */
    public static DownloadResult downloadImage(String imageUrl) {
        return downloadImage(imageUrl, false);
    }

    /**
     * 保存到临时文件
     */
    private static File saveToTempFile(InputStream inputStream, String originalFilename, String extension) throws IOException {
        // 生成临时文件名
        String tempFileName = "temp_" + System.currentTimeMillis() + "_" +
                UUID.randomUUID().toString().substring(0, 8) + "." + extension;
        File tempFile = new File(System.getProperty("java.io.tmpdir"), tempFileName);

        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            byte[] buffer = new byte[8192];
            int bytesRead;

            while ((bytesRead = inputStream.read(buffer)) != -1) {
                fos.write(buffer, 0, bytesRead);
            }
        }

        return tempFile;
    }

    /**
     * 获取原始文件名
     */
    private static String getOriginalFilename(HttpURLConnection connection, URL url, String extension) {
        // 1. 尝试从Content-Disposition头部获取
        String contentDisposition = connection.getHeaderField("Content-Disposition");
        if (contentDisposition != null) {
            String filename = extractFilenameFromContentDisposition(contentDisposition);
            if (filename != null && !filename.trim().isEmpty()) {
                return filename;
            }
        }

        // 2. 从URL路径获取
        String path = url.getPath();
        if (path != null && !path.isEmpty()) {
            int lastSlashIndex = path.lastIndexOf('/');
            if (lastSlashIndex != -1 && lastSlashIndex < path.length() - 1) {
                String nameFromPath = path.substring(lastSlashIndex + 1);
                if (nameFromPath.contains(".")) {
                    return nameFromPath;
                }
            }
        }

        // 3. 生成默认文件名
        return "image_" + System.currentTimeMillis() + "." + extension;
    }

    /**
     * 从Content-Disposition提取文件名
     */
    private static String extractFilenameFromContentDisposition(String contentDisposition) {
        if (contentDisposition == null) {
            return null;
        }

        // 查找filename=或filename*=部分
        String[] parts = contentDisposition.split(";");
        for (String part : parts) {
            part = part.trim();
            if (part.startsWith("filename=")) {
                String filename = part.substring("filename=".length());
                // 移除引号
                if (filename.startsWith("\"") && filename.endsWith("\"")) {
                    filename = filename.substring(1, filename.length() - 1);
                }
                return filename;
            } else if (part.startsWith("filename*=")) {
                // 处理编码的文件名
                String encodedFilename = part.substring("filename*=".length());
                if (encodedFilename.startsWith("UTF-8''")) {
                    return encodedFilename.substring("UTF-8''".length());
                }
            }
        }

        return null;
    }

    /**
     * 获取文件扩展名
     */
    private static String getFileExtensionFromUrl(String url) {
        try {
            URL urlObj = new URL(url);
            String path = urlObj.getPath();

            if (path == null || path.isEmpty()) {
                return null;
            }

            int lastDotIndex = path.lastIndexOf('.');
            if (lastDotIndex == -1 || lastDotIndex == path.length() - 1) {
                return null;
            }

            return path.substring(lastDotIndex + 1).toLowerCase();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 检查是否为图片Content-Type
     */
    private static boolean isImageContentType(String contentType) {
        if (contentType == null) {
            return false;
        }

        contentType = contentType.toLowerCase().split(";")[0].trim();

        // 检查完整MIME类型
        if (SUPPORTED_MIME_TYPES.contains(contentType)) {
            return true;
        }

        // 检查是否为image/开头
        return contentType.startsWith("image/");
    }

    /**
     * 验证图片数据
     */
    private static boolean isValidImageData(byte[] data) {
        if (data == null || data.length == 0) {
            return false;
        }

        try (ByteArrayInputStream bais = new ByteArrayInputStream(data)) {
            BufferedImage image = ImageIO.read(bais);
            return image != null;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 自定义MultipartFile实现
     */
    public static class CustomMultipartFile implements MultipartFile {
        private final String name;
        private final String originalFilename;
        private final String contentType;
        private final byte[] fileData;
        private final File tempFile;

        public CustomMultipartFile(String name, String originalFilename, String contentType,
                                   byte[] fileData, File tempFile) {
            this.name = name;
            this.originalFilename = originalFilename;
            this.contentType = contentType;
            this.fileData = fileData;
            this.tempFile = tempFile;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getOriginalFilename() {
            return originalFilename;
        }

        @Override
        public String getContentType() {
            return contentType;
        }

        @Override
        public boolean isEmpty() {
            if (fileData != null) {
                return fileData.length == 0;
            } else if (tempFile != null) {
                return tempFile.length() == 0;
            }
            return true;
        }

        @Override
        public long getSize() {
            if (fileData != null) {
                return fileData.length;
            } else if (tempFile != null) {
                return tempFile.length();
            }
            return 0;
        }

        @Override
        public byte[] getBytes() throws IOException {
            if (fileData != null) {
                return fileData;
            } else if (tempFile != null) {
                return readFileToBytes(tempFile);
            }
            return new byte[0];
        }

        @Override
        public InputStream getInputStream() throws IOException {
            if (fileData != null) {
                return new ByteArrayInputStream(fileData);
            } else if (tempFile != null) {
                return new FileInputStream(tempFile);
            }
            return new ByteArrayInputStream(new byte[0]);
        }

        @Override
        public void transferTo(File dest) throws IOException, IllegalStateException {
            if (fileData != null) {
                try (FileOutputStream fos = new FileOutputStream(dest)) {
                    fos.write(fileData);
                }
            } else if (tempFile != null) {
                // 复制文件
                try (InputStream is = new FileInputStream(tempFile);
                     OutputStream os = new FileOutputStream(dest)) {
                    byte[] buffer = new byte[8192];
                    int bytesRead;
                    while ((bytesRead = is.read(buffer)) != -1) {
                        os.write(buffer, 0, bytesRead);
                    }
                }
            }
        }

        /**
         * 清理临时文件
         */
        public void cleanup() {
            if (tempFile != null && tempFile.exists()) {
                tempFile.delete();
            }
        }

        private byte[] readFileToBytes(File file) throws IOException {
            try (FileInputStream fis = new FileInputStream(file);
                 ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = fis.read(buffer)) != -1) {
                    baos.write(buffer, 0, bytesRead);
                }
                return baos.toByteArray();
            }
        }
    }

    /**
     * 验证结果类
     */
    public static class ValidationResult {
        private boolean valid;
        private String message;
        private String url;
        private String extension;

        public boolean isValid() { return valid; }
        public void setValid(boolean valid) { this.valid = valid; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }

        public String getUrl() { return url; }
        public void setUrl(String url) { this.url = url; }

        public String getExtension() { return extension; }
        public void setExtension(String extension) { this.extension = extension; }
    }

    /**
     * 下载结果类
     */
    public static class DownloadResult {
        private boolean success;
        private String message;
        private String url;
        private MultipartFile multipartFile;
        private File downloadedFile;
        private byte[] fileData;
        private long fileSize;
        private String contentType;
        private String filename;

        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }

        public String getUrl() { return url; }
        public void setUrl(String url) { this.url = url; }

        public MultipartFile getMultipartFile() { return multipartFile; }
        public void setMultipartFile(MultipartFile multipartFile) { this.multipartFile = multipartFile; }

        public File getDownloadedFile() { return downloadedFile; }
        public void setDownloadedFile(File downloadedFile) { this.downloadedFile = downloadedFile; }

        public byte[] getFileData() { return fileData; }
        public void setFileData(byte[] fileData) { this.fileData = fileData; }

        public long getFileSize() { return fileSize; }
        public void setFileSize(long fileSize) { this.fileSize = fileSize; }

        public String getContentType() { return contentType; }
        public void setContentType(String contentType) { this.contentType = contentType; }

        public String getFilename() { return filename; }
        public void setFilename(String filename) { this.filename = filename; }

        /**
         * 清理资源
         */
        public void cleanup() {
            if (multipartFile instanceof CustomMultipartFile) {
                ((CustomMultipartFile) multipartFile).cleanup();
            }
            if (downloadedFile != null && downloadedFile.exists()) {
                downloadedFile.delete();
            }
        }

        @Override
        public String toString() {
            return "DownloadResult{" +
                    "success=" + success +
                    ", message='" + message + '\'' +
                    ", url='" + url + '\'' +
                    ", filename='" + filename + '\'' +
                    ", fileSize=" + fileSize +
                    ", contentType='" + contentType + '\'' +
                    ", hasMultipartFile=" + (multipartFile != null) +
                    ", hasFileData=" + (fileData != null) +
                    '}';
        }
    }
}