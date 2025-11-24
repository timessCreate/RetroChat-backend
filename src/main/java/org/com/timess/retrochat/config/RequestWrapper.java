package org.com.timess.retrochat.config;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * HttpServletRequest的body值是流，仅支持读取一次
 * 为了能够多次读取，对请求类进行包装
 * 包装请求，使 InputStream 可以重复读取
 *
 * @author timess
 */
@Slf4j
public class RequestWrapper extends HttpServletRequestWrapper {

    private final String body;
    private final byte[] bodyBytes;

    public RequestWrapper(HttpServletRequest request) {
        super(request);
        StringBuilder stringBuilder = new StringBuilder();
        try (InputStream inputStream = request.getInputStream();
             BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, getCharset(request)))) {
            char[] charBuffer = new char[128];
            int bytesRead = -1;
            while ((bytesRead = bufferedReader.read(charBuffer)) > 0) {
                stringBuilder.append(charBuffer, 0, bytesRead);
            }
        } catch (IOException ignored) {
        }
        body = stringBuilder.toString();
        bodyBytes = body.getBytes(StandardCharsets.UTF_8);
    }

    /**
     * 获取字符编码
     */
    private String getCharset(HttpServletRequest request) {
        String charset = request.getCharacterEncoding();
        return charset != null ? charset : StandardCharsets.UTF_8.name();
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bodyBytes);

        return new ServletInputStream() {
            private boolean finished = false;

            @Override
            public boolean isFinished() {
                return finished;
            }

            @Override
            public boolean isReady() {
                return true;  // 数据已经缓存，总是准备就绪
            }

            @Override
            public void setReadListener(ReadListener readListener) {
                // 对于同步处理，不需要实现异步监听器
                // 但必须提供空实现来满足接口要求
                if (readListener == null) {
                    throw new IllegalArgumentException("ReadListener cannot be null");
                }
                // 由于是同步处理，这里不需要设置监听器
                // 但可以立即通知数据可用，因为数据已经全部在内存中
                try {
                    readListener.onDataAvailable();
                    // 由于数据已经全部读取完成，立即通知读取完成
                    readListener.onAllDataRead();
                } catch (IOException e) {
                    readListener.onError(e);
                }
            }

            @Override
            public int read() throws IOException {
                int data = byteArrayInputStream.read();
                if (data == -1) {
                    finished = true;
                }
                return data;
            }
        };
    }

    @Override
    public BufferedReader getReader() throws IOException {
        return new BufferedReader(new InputStreamReader(this.getInputStream(), StandardCharsets.UTF_8));
    }

    public String getBody() {
        return this.body;
    }
}