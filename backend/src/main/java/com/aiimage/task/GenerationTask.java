package com.aiimage.task;

import com.aiimage.entity.GenerationRecord;
import com.aiimage.entity.Prompt;
import com.aiimage.service.GenerationRecordService;
import com.aiimage.service.PromptService;
import com.aiimage.service.SystemConfigService;
import com.aiimage.util.OssUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;

@Component
@Slf4j
@RequiredArgsConstructor
public class GenerationTask {

    private final GenerationRecordService generationRecordService;
    private final PromptService promptService;
    private final SystemConfigService systemConfigService;
    private final OssUtil ossUtil;
    private final RestTemplate restTemplate;

    @Value("${app.ai-api.base-url:https://api.marswave.ai}")
    private String aiApiBaseUrl;

    @Value("${app.ai-api.api-key:}")
    private String aiApiKey;

    @Async
    public void processGeneration(Long recordId) {
        GenerationRecord record = generationRecordService.getById(recordId);
        if (record == null) {
            log.error("生成记录不存在: {}", recordId);
            return;
        }

        try {
            // 1. Update status to generating (1)
            record.setStatus(1);
            generationRecordService.updateById(record);

            // 2. Get prompt info
            Prompt prompt = promptService.getById(record.getPromptId());
            if (prompt == null) {
                failRecord(record, "提示词不存在");
                return;
            }

            // 3. Build API request
            String promptText = prompt.getContent();
            String paramsJson = record.getParams();
            if (paramsJson != null && !paramsJson.isBlank()) {
                try {
                    JSONObject paramsObj = JSON.parseObject(paramsJson);
                    String promptContent = paramsObj.getString("promptContent");
                    if (promptContent != null && !promptContent.isBlank()) {
                        promptText = promptContent;
                    }
                } catch (Exception e) {
                    log.warn("解析生成参数失败: {}", e.getMessage());
                }
            }

            Map<String, Object> requestBody = new LinkedHashMap<>();
            requestBody.put("provider", "openai");
            requestBody.put("model", "gpt-image-2");
            requestBody.put("prompt", promptText);

            // Add reference images if present
            String refImages = record.getRefImages();
            if (refImages != null && !refImages.isEmpty()) {
                try {
                    List<String> imageUrls = JSON.parseArray(refImages, String.class);
                    List<Map<String, Object>> refImageList = new ArrayList<>();
                    for (String imageUrl : imageUrls) {
                        log.info("参考图URL: {}", imageUrl);
                        Map<String, Object> fileData = new LinkedHashMap<>();
                        String encodedUri = imageUrl.replace(" ", "%20");
                        fileData.put("fileUri", encodedUri);
                        fileData.put("mimeType", "image/jpeg");
                        Map<String, Object> refImage = new LinkedHashMap<>();
                        refImage.put("fileData", fileData);
                        refImageList.add(refImage);
                    }
                    if (!refImageList.isEmpty()) {
                        requestBody.put("referenceImages", refImageList);
                    }
                } catch (Exception e) {
                    log.warn("解析参考图URL失败: {}", e.getMessage());
                }
            }

            // Add image config
            Map<String, Object> imageConfig = new LinkedHashMap<>();
            imageConfig.put("imageSize", "2K");
            requestBody.put("imageConfig", imageConfig);

            // 4. Call external AI API
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Accept", "application/json");
            String apiKey = systemConfigService.getValue("api_key");
            if (apiKey != null) {
                apiKey = apiKey.trim();
            }
            if (apiKey == null || apiKey.isBlank()) {
                apiKey = aiApiKey != null ? aiApiKey.trim() : "";
            }
            if (apiKey.isBlank()) {
                failRecord(record, "生成失败: 未配置 API Key");
                return;
            }
            headers.set("Authorization", "Bearer " + apiKey);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            String generationUrl = resolveGenerationUrl();
            log.info("generationUrl:{}",generationUrl);
            log.info("generationUrl entity:{}",JSON.toJSONString(entity.getBody()));
            ResponseEntity<String> response = restTemplate.exchange(
                    generationUrl, HttpMethod.POST, entity, String.class
            );

            String responseBody = response.getBody();
            log.info("AI API响应: {}", responseBody);

            // 5. Parse response and extract base64 image
            String imageBase64 = extractImageBase64(responseBody);
            if (imageBase64 == null) {
                failRecord(record, "AI API返回格式错误");
                return;
            }

            // 6. Save external API response
            record.setExternalApiResponse(responseBody);

            // 7. Upload to OSS
            String ossUrl = uploadBase64ToOss(imageBase64, "png");
            if (ossUrl == null) {
                failRecord(record, "图片上传OSS失败");
                return;
            }

            // 8. Update record as success
            record.setStatus(2);
            record.setResultImages(JSON.toJSONString(List.of(ossUrl)));
            record.setUpdatedAt(LocalDateTime.now());
            generationRecordService.updateById(record);
            log.info("生成成功，图片URL: {}", ossUrl);

        } catch (Exception e) {
            log.error("生成失败: {}", e.getMessage(), e);
            failRecord(record, "生成失败: " + e.getMessage());
        }
    }

    private String resolveGenerationUrl() {
        String base = aiApiBaseUrl != null ? aiApiBaseUrl.trim() : "";
        if (base.isBlank()) {
            base = "https://api.marswave.ai";
        }
        if (base.contains("/v1/images/generation")) {
            return base;
        }
        if (base.endsWith("/")) {
            base = base.substring(0, base.length() - 1);
        }
        return base + "/openapi/v1/images/generation";
    }

    private String extractImageBase64(String responseJson) {
        try {
            // Try the candidates.content.parts.inlineData path first (Google style)
            JSONObject root = JSON.parseObject(responseJson);
            JSONArray candidates = root.getJSONArray("candidates");
            if (candidates != null && !candidates.isEmpty()) {
                JSONObject firstCandidate = candidates.getJSONObject(0);
                JSONObject content = firstCandidate.getJSONObject("content");
                if (content != null) {
                    JSONArray parts = content.getJSONArray("parts");
                    if (parts != null && !parts.isEmpty()) {
                        JSONObject firstPart = parts.getJSONObject(0);
                        JSONObject inlineData = firstPart.getJSONObject("inlineData");
                        if (inlineData != null) {
                            return inlineData.getString("data");
                        }
                    }
                }
            }
            // Fallback: search anywhere for inlineData.data (Marswave style)
            return findInlineData(root);
        } catch (Exception e) {
            log.error("解析AI响应失败: {}", e.getMessage());
            return null;
        }
    }

    private String findInlineData(JSONObject obj) {
        for (String key : obj.keySet()) {
            Object value = obj.get(key);
            if ("inlineData".equals(key) && value instanceof JSONObject) {
                JSONObject inline = (JSONObject) value;
                String data = inline.getString("data");
                if (data != null && !data.isBlank()) {
                    return data;
                }
            }
            if (value instanceof JSONObject) {
                String found = findInlineData((JSONObject) value);
                if (found != null) return found;
            } else if (value instanceof JSONArray) {
                String found = findInlineData((JSONArray) value);
                if (found != null) return found;
            }
        }
        return null;
    }

    private String findInlineData(JSONArray arr) {
        for (int i = 0; i < arr.size(); i++) {
            Object item = arr.get(i);
            if (item instanceof JSONObject) {
                String found = findInlineData((JSONObject) item);
                if (found != null) return found;
            }
        }
        return null;
    }

    private String uploadBase64ToOss(String base64Data, String extension) {
        try {
            byte[] imageBytes = Base64.getDecoder().decode(base64Data);
            Path tempFile = Files.createTempFile("gen-", "." + extension);
            Files.write(tempFile, imageBytes);
            String url = ossUtil.uploadFile(Files.newInputStream(tempFile), "generated." + extension);
            Files.deleteIfExists(tempFile);
            return url;
        } catch (Exception e) {
            log.error("上传OSS失败: {}", e.getMessage());
            return null;
        }
    }

    private void failRecord(GenerationRecord record, String errorMsg) {
        record.setStatus(3);
        record.setErrorMsg(errorMsg);
        record.setUpdatedAt(LocalDateTime.now());
        generationRecordService.updateById(record);
    }
}
