package com.xuecheng.content.feignclient;

import com.xuecheng.content.config.MultipartSupportConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * ClassName: MediaServiceClient
 * Package: com.xuecheng.content.feignclient
 * Description:
 * 远程调用媒资服务的注解
 * @Author: XKing
 * @Create: 2023/5/17 - 17:01
 * @Version: 1.0
 */
//使用fallback定义降级类是无法拿到熔断异常的
//@FeignClient(value = "media-api",configuration = {MultipartSupportConfig.class},fallback =MediaServiceClientFallback.class )
@FeignClient(value = "media-api",configuration = {MultipartSupportConfig.class},fallbackFactory = MediaServiceClientFallbackFactory.class )
public interface MediaServiceClient {

    @RequestMapping(value = "/media/upload/coursefile",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String upload(@RequestPart("filedata") MultipartFile filedata,
                                      @RequestParam(value = "objectName",required = false)String objectName ) throws IOException;
}
