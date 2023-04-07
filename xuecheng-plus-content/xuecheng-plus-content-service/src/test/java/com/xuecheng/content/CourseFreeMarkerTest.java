package com.xuecheng.content;


import com.xuecheng.content.model.dto.CoursePreviewDto;
import com.xuecheng.content.service.CoursePublishService;
import freemarker.template.Configuration;
import freemarker.template.Template;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;


@Slf4j
@SpringBootTest
public class CourseFreeMarkerTest {
    @Autowired
    CoursePublishService coursePublishService;
    @Test
    public void testCourseFreeMarkerTest() {
        Configuration configuration = new Configuration(Configuration.getVersion());

        String classPath = this.getClass().getResource("/").getPath();
        System.out.println("classPath:"+ classPath);
        File htmlFile = null;
        try {
            configuration.setDirectoryForTemplateLoading(new File(classPath + "/templates"));
            configuration.setDefaultEncoding("utf-8");
            Template template = configuration.getTemplate("course_template.ftl");
            CoursePreviewDto coursePreviewDto = coursePublishService.getCoursePreviewInfo(118L);
            Map<String,Object> map = new HashMap<>();
            map.put("model",coursePreviewDto);

            String html = FreeMarkerTemplateUtils.processTemplateIntoString(template,map);
            InputStream inputStream = IOUtils.toInputStream(html,"utf-8");

            FileOutputStream fileOutputStream = new FileOutputStream("D:/soft/upload/118.html");
            IOUtils.copy(inputStream,fileOutputStream);

        } catch (Exception e) {

            e.printStackTrace();
        }

    }
}
