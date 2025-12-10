package org.com.timess.retrochat.utils;

import org.apache.commons.io.FilenameUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * 图片验证工具类
 * 提供多种方式验证文件是否为有效的图片
 */
public class ImageValidatorUtils {

    // 支持的图片格式扩展名
    private static final Set<String> SUPPORTED_EXTENSIONS = new HashSet<>(
            Arrays.asList("jpg", "jpeg", "png", "gif", "bmp", "webp", "ico", "tiff", "svg")
    );

    // 支持的MIME类型前缀
    private static final Set<String> IMAGE_MIME_PREFIXES = new HashSet<>(
            Arrays.asList("image/jpeg", "image/png", "image/gif", "image/bmp",
                    "image/webp", "image/x-icon", "image/tiff", "image/svg+xml")
    );

    // 图片文件魔数签名
    private static final byte[][] IMAGE_MAGIC_NUMBERS = {
            { (byte)0xFF, (byte)0xD8, (byte)0xFF },                     // JPEG/JPG
            { (byte)0x89, (byte)0x50, (byte)0x4E, (byte)0x47 },         // PNG
            { (byte)0x47, (byte)0x49, (byte)0x46, (byte)0x38 },         // GIF
            { (byte)0x42, (byte)0x4D },                                 // BMP
            { (byte)0x52, (byte)0x49, (byte)0x46, (byte)0x46 },         // WebP
            { (byte)0x00, (byte)0x00, (byte)0x01, (byte)0x00 },         // ICO
            { (byte)0x49, (byte)0x49, (byte)0x2A, (byte)0x00 },         // TIFF (little endian)
            { (byte)0x4D, (byte)0x4D, (byte)0x00, (byte)0x2A }          // TIFF (big endian)
    };

    /**
     * 验证是否为有效的图片文件
     * @param file File文件对象
     * @return 验证结果对象
     */
    public static ValidationResult validateImage(File file) {
        return validateImage(file, 0, 0, 0, 0, 0);
    }

    /**
     * 验证是否为有效的图片文件
     * @param file File文件对象
     * @return 验证结果对象
     */
    public static ValidationResult validateImage(MultipartFile file) {
        try {
            File file1 = FileUtils.convertToFile(file);
            return validateImage(file1, 0, 0, 0, 0, 0);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * 验证是否为有效的图片文件（带尺寸限制）
     * @param file File文件对象
     * @param maxSizeKB 最大文件大小（KB，0表示不限制）
     * @param minWidth 最小宽度（0表示不限制）
     * @param minHeight 最小高度（0表示不限制）
     * @param maxWidth 最大宽度（0表示不限制）
     * @param maxHeight 最大高度（0表示不限制）
     * @return 验证结果对象
     */
    public static ValidationResult validateImage(File file, int maxSizeKB,
                                                 int minWidth, int minHeight,
                                                 int maxWidth, int maxHeight) {

        ValidationResult result = new ValidationResult();

        // 1. 基本检查
        if (file == null || !file.exists()) {
            result.setValid(false);
            result.setMessage("文件不存在");
            return result;
        }

        if (!file.isFile()) {
            result.setValid(false);
            result.setMessage("不是有效的文件");
            return result;
        }

        if (file.length() == 0) {
            result.setValid(false);
            result.setMessage("文件为空");
            return result;
        }

        // 2. 文件大小检查
        if (maxSizeKB > 0) {
            long maxSizeBytes = maxSizeKB * 1024L;
            if (file.length() > maxSizeBytes) {
                result.setValid(false);
                result.setMessage(String.format("文件大小不能超过 %dKB", maxSizeKB));
                return result;
            }
        }

        // 3. 扩展名检查
        String filename = file.getName();
        if (filename == null || filename.trim().isEmpty()) {
            result.setValid(false);
            result.setMessage("文件名不能为空");
            return result;
        }

        String extension = FilenameUtils.getExtension(filename).toLowerCase();
        if (!isValidExtension(extension)) {
            result.setValid(false);
            result.setMessage("不支持的文件格式，请上传图片文件");
            return result;
        }

        // 4. 魔数检查（防止文件伪造）
        try {
            if (!isValidByMagicNumber(file)) {
                result.setValid(false);
                result.setMessage("文件内容不是有效的图片格式");
                return result;
            }
        } catch (IOException e) {
            result.setValid(false);
            result.setMessage("文件读取失败：" + e.getMessage());
            return result;
        }

        // 5. 图片尺寸检查
        if (minWidth > 0 || minHeight > 0 || maxWidth > 0 || maxHeight > 0) {
            try {
                ImageDimensions dimensions = getImageDimensions(file);
                if (dimensions == null) {
                    result.setValid(false);
                    result.setMessage("无法获取图片尺寸信息");
                    return result;
                }

                if ((minWidth > 0 && dimensions.width < minWidth) ||
                        (minHeight > 0 && dimensions.height < minHeight)) {
                    result.setValid(false);
                    result.setMessage(String.format("图片尺寸过小，最小尺寸：%dx%d", minWidth, minHeight));
                    return result;
                }

                if ((maxWidth > 0 && dimensions.width > maxWidth) ||
                        (maxHeight > 0 && dimensions.height > maxHeight)) {
                    result.setValid(false);
                    result.setMessage(String.format("图片尺寸过大，最大尺寸：%dx%d", maxWidth, maxHeight));
                    return result;
                }

                result.setDimensions(dimensions);
            } catch (IOException e) {
                result.setValid(false);
                result.setMessage("图片尺寸检测失败：" + e.getMessage());
                return result;
            }
        }

        // 6. 通过Java ImageIO验证
        try {
            if (!isValidByImageIO(file)) {
                result.setValid(false);
                result.setMessage("图片文件已损坏或格式不支持");
                return result;
            }
        } catch (IOException e) {
            result.setValid(false);
            result.setMessage("图片验证失败：" + e.getMessage());
            return result;
        }

        result.setValid(true);
        result.setMessage("验证通过");
        result.setExtension(extension);
        result.setFileSize(file.length());
        result.setFilename(filename);

        // 设置MIME类型
        result.setMimeType(detectMimeType(file, extension));

        return result;
    }

    /**
     * 检查扩展名是否有效
     */
    private static boolean isValidExtension(String extension) {
        return SUPPORTED_EXTENSIONS.contains(extension.toLowerCase());
    }

    /**
     * 检测MIME类型
     */
    private static String detectMimeType(File file, String extension) {
        // 根据扩展名猜测MIME类型
        switch (extension.toLowerCase()) {
            case "jpg":
            case "jpeg":
                return "image/jpeg";
            case "png":
                return "image/png";
            case "gif":
                return "image/gif";
            case "bmp":
                return "image/bmp";
            case "webp":
                return "image/webp";
            case "ico":
                return "image/x-icon";
            case "tiff":
                return "image/tiff";
            case "svg":
                return "image/svg+xml";
            default:
                return "application/octet-stream";
        }
    }

    /**
     * 通过魔数验证图片
     */
    private static boolean isValidByMagicNumber(File file) throws IOException {
        byte[] fileHeader = new byte[8]; // 读取前8个字节足够判断大部分图片格式
        try (InputStream inputStream = new FileInputStream(file)) {
            int read = inputStream.read(fileHeader, 0, 8);
            if (read < 2) return false; // 至少需要2个字节判断BMP

            // 检查所有支持的图片格式魔数
            for (byte[] magicNumber : IMAGE_MAGIC_NUMBERS) {
                if (magicNumber.length <= read) {
                    boolean match = true;
                    for (int i = 0; i < magicNumber.length; i++) {
                        if (fileHeader[i] != magicNumber[i]) {
                            match = false;
                            break;
                        }
                    }
                    if (match) return true;
                }
            }
        }
        return false;
    }

    /**
     * 通过ImageIO验证图片
     */
    private static boolean isValidByImageIO(File file) throws IOException {
        try (InputStream inputStream = new FileInputStream(file)) {
            BufferedImage image = ImageIO.read(inputStream);
            return image != null;
        }
    }

    /**
     * 获取图片尺寸
     */
    private static ImageDimensions getImageDimensions(File file) throws IOException {
        try (InputStream inputStream = new FileInputStream(file)) {
            BufferedImage image = ImageIO.read(inputStream);
            if (image != null) {
                return new ImageDimensions(image.getWidth(), image.getHeight());
            }
        }
        return null;
    }

    /**
     * 简化的验证方法 - 只验证是否为图片
     */
    public static boolean isImage(File file) {
        if (file == null || !file.exists() || !file.isFile()) {
            return false;
        }

        // 检查扩展名
        String filename = file.getName();
        String extension = FilenameUtils.getExtension(filename).toLowerCase();
        if (!isValidExtension(extension)) {
            return false;
        }

        // 通过ImageIO验证
        try (InputStream inputStream = new FileInputStream(file)) {
            BufferedImage image = ImageIO.read(inputStream);
            return image != null;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * 获取图片详细信息
     */
    public static ImageInfo getImageInfo(File file) throws IOException {
        if (file == null || !file.exists() || !file.isFile()) {
            throw new IllegalArgumentException("文件不存在或无效");
        }

        ImageInfo info = new ImageInfo();
        info.setFilename(file.getName());
        info.setFileSize(file.length());
        info.setExtension(FilenameUtils.getExtension(file.getName()).toLowerCase());
        info.setFilePath(file.getAbsolutePath());

        try (InputStream inputStream = new FileInputStream(file)) {
            BufferedImage image = ImageIO.read(inputStream);
            if (image != null) {
                info.setWidth(image.getWidth());
                info.setHeight(image.getHeight());
                info.setValid(true);
            } else {
                info.setValid(false);
            }
        }

        return info;
    }

    /**
     * 验证结果类
     */
    public static class ValidationResult {
        private boolean valid;
        private String message;
        private String filename;
        private String extension;
        private String mimeType;
        private long fileSize;
        private ImageDimensions dimensions;

        // 构造方法、getter、setter
        public ValidationResult() {}

        public boolean isValid() { return valid; }
        public void setValid(boolean valid) { this.valid = valid; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }

        public String getFilename() { return filename; }
        public void setFilename(String filename) { this.filename = filename; }

        public String getExtension() { return extension; }
        public void setExtension(String extension) { this.extension = extension; }

        public String getMimeType() { return mimeType; }
        public void setMimeType(String mimeType) { this.mimeType = mimeType; }

        public long getFileSize() { return fileSize; }
        public void setFileSize(long fileSize) { this.fileSize = fileSize; }

        public ImageDimensions getDimensions() { return dimensions; }
        public void setDimensions(ImageDimensions dimensions) { this.dimensions = dimensions; }

        @Override
        public String toString() {
            return "ValidationResult{" +
                    "valid=" + valid +
                    ", message='" + message + '\'' +
                    ", filename='" + filename + '\'' +
                    ", extension='" + extension + '\'' +
                    ", mimeType='" + mimeType + '\'' +
                    ", fileSize=" + fileSize +
                    ", dimensions=" + (dimensions != null ? dimensions.toString() : "null") +
                    '}';
        }
    }

    /**
     * 图片尺寸类
     */
    public static class ImageDimensions {
        private final int width;
        private final int height;

        public ImageDimensions(int width, int height) {
            this.width = width;
            this.height = height;
        }

        public int getWidth() { return width; }
        public int getHeight() { return height; }

        @Override
        public String toString() {
            return width + "x" + height;
        }
    }

    /**
     * 图片信息类
     */
    public static class ImageInfo {
        private boolean valid;
        private String filename;
        private String filePath;
        private String extension;
        private long fileSize;
        private int width;
        private int height;

        // getter、setter
        public boolean isValid() { return valid; }
        public void setValid(boolean valid) { this.valid = valid; }

        public String getFilename() { return filename; }
        public void setFilename(String filename) { this.filename = filename; }

        public String getFilePath() { return filePath; }
        public void setFilePath(String filePath) { this.filePath = filePath; }

        public String getExtension() { return extension; }
        public void setExtension(String extension) { this.extension = extension; }

        public long getFileSize() { return fileSize; }
        public void setFileSize(long fileSize) { this.fileSize = fileSize; }

        public int getWidth() { return width; }
        public void setWidth(int width) { this.width = width; }

        public int getHeight() { return height; }
        public void setHeight(int height) { this.height = height; }

        @Override
        public String toString() {
            return "ImageInfo{" +
                    "valid=" + valid +
                    ", filename='" + filename + '\'' +
                    ", filePath='" + filePath + '\'' +
                    ", extension='" + extension + '\'' +
                    ", fileSize=" + fileSize +
                    ", width=" + width +
                    ", height=" + height +
                    '}';
        }
    }
}