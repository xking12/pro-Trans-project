package com.xuecheng.content;

import com.xuecheng.content.model.dto.CoursePreviewDto;
import com.xuecheng.content.service.CoursePublishService;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.HashMap;

/**
 * ClassName: FreeMarkerTest
 * Package: com.xuecheng.content
 * Description:
 *
 * @Author: XKing
 * @Create: 2023/5/17 - 16:21
 * @Version: 1.0
 */
@SpringBootTest
public class FreeMarkerTest {
    @Autowired
    CoursePublishService coursePublishService;
    @Test
    public void testGenerateHtmlByTemplate() throws Exception {

        Configuration configuration = new Configuration(Configuration.getVersion());
        //拿到classpath路径
        String classPath = this.getClass().getResource("/").getPath();
        //指定模板的目录
        configuration.setDirectoryForTemplateLoading(new File(classPath+"/templates/"));
        //指定编码
        configuration.setDefaultEncoding("utf-8");
        //得到模板
        Template template = configuration.getTemplate("course_template.ftl");
        //得到数据
        CoursePreviewDto coursePreviewInfo =coursePublishService.getCoursePreviewInfo(1L);
        HashMap<String,Object> map = new HashMap();
        map.put("model",coursePreviewInfo);

        //Template template, Object model
        String html = FreeMarkerTemplateUtils.processTemplateIntoString(template, map);

        //将静态化内容输出到文件中
        InputStream inputStream = IOUtils.toInputStream(html);
        //输出流
        FileOutputStream outputStream = new FileOutputStream("D:\\xuecheng-media\\testHtml\\test.html");
        IOUtils.copy(inputStream, outputStream);


    }
}
