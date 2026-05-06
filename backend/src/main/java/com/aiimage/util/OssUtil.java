package com.aiimage.util;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.PutObjectRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class OssUtil {
    
    @Value("${app.oss.endpoint}")
    private String endpoint;
    
    @Value("${app.oss.access-key-id}")
    private String accessKeyId;
    
    @Value("${app.oss.access-key-secret}")
    private String accessKeySecret;
    
    @Value("${app.oss.bucket-name}")
    private String bucketName;
    
    @Value("${app.oss.region}")
    private String region;
    
    /**
     * 上传文件到 OSS
     */
    public String uploadFile(InputStream inputStream, String fileName) {
        try {
            OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
            
            // 生成唯一的对象键
            String objectKey = "images/" + UUID.randomUUID() + fileName;
            
            // 上传文件
            ossClient.putObject(new PutObjectRequest(bucketName, objectKey, inputStream));
            
            // 关闭 OSS 客户端
            ossClient.shutdown();
            
            // 返回文件 URL
            String fileUrl = "https://zzlpic.cc/" + objectKey;
            log.info("文件上传成功: {}", fileUrl);
            return fileUrl;
        } catch (Exception e) {
            log.error("文件上传失败: {}", e.getMessage());
            throw new RuntimeException("文件上传失败");
        }
    }
    
    /**
     * 删除 OSS 中的文件
     */
    public void deleteFile(String objectKey) {
        try {
            OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
            ossClient.deleteObject(bucketName, objectKey);
            ossClient.shutdown();
            log.info("文件删除成功: {}", objectKey);
        } catch (Exception e) {
            log.error("文件删除失败: {}", e.getMessage());
        }
    }
}
