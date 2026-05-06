package com.aiimage.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {
    
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("AI 图片生成平台 API")
                        .version("1.0.0")
                        .description("AI 图片生成平台后端 API 文档")
                        .contact(new Contact()
                                .name("技术支持")
                                .email("support@aiimage.com")));
    }
}
