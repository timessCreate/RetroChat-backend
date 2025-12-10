package org.com.timess.retrochat.utils;

import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * 文件处理工具类
 */
public class FileUtils {

    /**
     * 将MultipartFile转换为File对象
     * @param multipartFile
     * @return
     * @throws IOException
     */
    public static File convertToFile(MultipartFile multipartFile) throws IOException {
        // 创建临时文件路径
        Path tempFile = Files.createTempFile("temp-", multipartFile.getOriginalFilename());

        // 复制文件内容
        try (var inputStream = multipartFile.getInputStream()) {
            Files.copy(inputStream, tempFile, StandardCopyOption.REPLACE_EXISTING);
        }

        return tempFile.toFile();
    }
}
