package com.xuecheng.media.config;


import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

@Configuration
public class MinioConfig {

    @Value("${minio.endpoint}")
    private String endPoint;

    @Value("${minio.accessKey}")
    private String accessKey;

    @Value("${minio.secretKey}")
    private String secretKey;



    @Bean
    public MinioClient minioClient(){
        MinioClient minioClient = MinioClient.builder().
                endpoint(endPoint).
                credentials(accessKey,secretKey).
                build();
        return minioClient;
    }
}
