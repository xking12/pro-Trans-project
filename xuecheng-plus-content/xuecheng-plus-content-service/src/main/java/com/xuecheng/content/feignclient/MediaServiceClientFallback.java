package com.xuecheng.content.feignclient;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * ClassName: MediaServiceClientFallback
 * Package: com.xuecheng.content.feignclient
 * Description:
 * 降级方法
 * @Author: XKing
 * @Create: 2023/5/17 - 17:40
 * @Version: 1.0
 */
public class MediaServiceClientFallback implements MediaServiceClient {
    @Override
    public String upload(MultipartFile filedata, String objectName) throws IOException {
        return null;
    }
}
