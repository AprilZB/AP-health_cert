package com.microport.healthcert.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microport.healthcert.dto.HealthCertDTO;
import com.microport.healthcert.service.OcrService;
import okhttp3.*;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * OCR服务实现类
 * 调用PaddleOCR服务进行健康证识别
 * 
 * @author system
 * @date 2024
 */
@Service
public class OcrServiceImpl implements OcrService {

    /**
     * OCR服务地址（PaddleOCR）
     * 使用JSON格式，Base64编码图片数据
     */
    private static final String OCR_URL = "http://10.11.100.238:8081/ocr";

    /**
     * 日期格式解析器（支持多种格式）
     */
    private static final DateTimeFormatter[] DATE_FORMATTERS = {
        DateTimeFormatter.ofPattern("yyyy-MM-dd"),
        DateTimeFormatter.ofPattern("yyyy/MM/dd"),
        DateTimeFormatter.ofPattern("yyyy年MM月dd日"),
        DateTimeFormatter.ofPattern("yyyy.MM.dd")
    };

    /**
     * 调用OCR API识别健康证
     * 使用Base64编码方式（JSON格式）调用PaddleOCR服务
     * 
     * @param imageFile 图片文件
     * @return OCR识别结果（HealthCertDTO），如果调用失败返回空对象
     */
    @Override
    public HealthCertDTO callOcrApi(File imageFile) {
        try {
            OkHttpClient client = new OkHttpClient();
            
            // 读取图片文件并转换为Base64编码
            byte[] imageBytes = Files.readAllBytes(imageFile.toPath());
            String base64Image = Base64.getEncoder().encodeToString(imageBytes);
            
            // 构建JSON请求体（根据PaddleOCR实际API格式）
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> requestData = new HashMap<>();
            requestData.put("file", base64Image);  // 使用file字段，不是image
            requestData.put("fileType", 1);       // 文件类型：1表示图片
            
            String jsonBody = objectMapper.writeValueAsString(requestData);
            
            // 创建请求体
            RequestBody requestBody = RequestBody.create(
                jsonBody, 
                MediaType.parse("application/json; charset=utf-8")
            );
            
            // 创建请求
            Request request = new Request.Builder()
                    .url(OCR_URL)
                    .post(requestBody)
                    .addHeader("Content-Type", "application/json")
                    .build();
            
            System.out.println("调用OCR服务: " + OCR_URL);
            System.out.println("图片大小: " + imageBytes.length + " bytes");
            
            // 执行请求
            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    System.err.println("OCR请求失败，状态码: " + response.code());
                    String errorBody = response.body() != null ? response.body().string() : "";
                    System.err.println("错误响应: " + errorBody);
                    return new HealthCertDTO();
                }
                
                // 获取响应体
                String responseBody = response.body() != null ? response.body().string() : "";
                if (responseBody.isEmpty()) {
                    System.err.println("OCR响应为空");
                    return new HealthCertDTO();
                }
                
                // 打印OCR原始响应（用于调试）
                System.out.println("OCR原始响应: " + responseBody.substring(0, Math.min(500, responseBody.length())));
                
                // 解析OCR结果
                HealthCertDTO result = parseOcrResult(responseBody);
                System.out.println("OCR解析结果: " + result);
                return result;
            }
        } catch (Exception e) {
            // OCR调用失败，记录日志并返回空结果，允许手动填写
            System.err.println("OCR调用失败: " + e.getMessage());
            e.printStackTrace();
            return new HealthCertDTO();
        }
    }

    /**
     * 解析OCR结果为HealthCertDTO
     * 从识别文本中提取健康证字段
     * 
     * @param ocrJson OCR返回的JSON字符串
     * @return 解析后的HealthCertDTO
     */
    @Override
    public HealthCertDTO parseOcrResult(String ocrJson) {
        HealthCertDTO dto = new HealthCertDTO();

        try {
            // 解析JSON
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(ocrJson);

            // PaddleOCR响应结构：result -> ocrResults -> prunedResult -> rec_texts
            JsonNode resultNode = jsonNode.get("result");
            if (resultNode == null || !resultNode.isObject()) {
                System.err.println("OCR响应中没有result字段，响应结构: " + jsonNode.toString());
                return dto;
            }
            
            JsonNode ocrResultsNode = resultNode.get("ocrResults");
            if (ocrResultsNode == null || !ocrResultsNode.isArray() || ocrResultsNode.size() == 0) {
                System.err.println("OCR响应中没有ocrResults字段或为空");
                return dto;
            }
            
            // 取第一个OCR结果
            JsonNode firstResult = ocrResultsNode.get(0);
            if (firstResult == null || !firstResult.isObject()) {
                System.err.println("OCR结果格式错误");
                return dto;
            }
            
            JsonNode prunedResultNode = firstResult.get("prunedResult");
            if (prunedResultNode == null || !prunedResultNode.isObject()) {
                System.err.println("OCR响应中没有prunedResult字段");
                return dto;
            }
            
            // 提取rec_texts字段（字符串数组）
            JsonNode recTextsNode = prunedResultNode.get("rec_texts");
            if (recTextsNode == null || !recTextsNode.isArray()) {
                System.err.println("OCR响应中没有rec_texts字段");
                return dto;
            }

            // 将所有识别文本合并
            // PaddleOCR返回的rec_texts是字符串数组: ["文本1", "文本2", ...]
            StringBuilder allText = new StringBuilder();
            for (JsonNode textNode : recTextsNode) {
                if (textNode.isTextual()) {
                    allText.append(textNode.asText()).append(" ");
                }
            }

            String ocrText = allText.toString();
            
            // 打印OCR识别文本（用于调试）
            System.out.println("OCR识别文本: " + ocrText);

            // 从识别文本中提取健康证字段
            // 提取编号
            dto.setCertNumber(extractCertNumber(ocrText));
            // 提取姓名
            dto.setEmployeeName(extractName(ocrText));
            // 提取性别
            dto.setGender(extractGender(ocrText));
            // 提取年龄
            dto.setAge(extractAge(ocrText));
            // 提取身份证号
            dto.setIdCard(extractIdCard(ocrText));
            // 提取健康证类别
            dto.setCategory(extractCategory(ocrText));
            // 提取发证日期
            dto.setIssueDate(extractIssueDate(ocrText));
            // 提取有效期至
            dto.setExpiryDate(extractExpiryDate(ocrText));
            // 提取发证机构
            dto.setIssuingAuthority(extractIssuingAuthority(ocrText));

        } catch (Exception e) {
            // 解析失败，记录日志并返回空对象
            System.err.println("OCR解析失败: " + e.getMessage());
            e.printStackTrace();
            return new HealthCertDTO();
        }

        return dto;
    }

    /**
     * 提取健康证编号
     */
    private String extractCertNumber(String text) {
        // 匹配编号模式（如：编号：XXXXX 或 证号：XXXXX）
        Pattern pattern = Pattern.compile("(?:编号|证号)[：:：]?\\s*([A-Z0-9]{6,20})", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    /**
     * 提取姓名
     */
    private String extractName(String text) {
        // 匹配姓名模式（如：姓名：张勃 或 姓名 张勃）
        Pattern pattern = Pattern.compile("(?:姓名|名字)[：:：\\s]+([\\u4e00-\\u9fa5]{2,4})", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            String name = matcher.group(1);
            System.out.println("提取到姓名: " + name);
            return name;
        }
        return null;
    }

    /**
     * 提取性别
     */
    private String extractGender(String text) {
        // 匹配性别模式
        if (text.contains("男") || text.contains("M") || text.contains("male")) {
            return "男";
        } else if (text.contains("女") || text.contains("F") || text.contains("female")) {
            return "女";
        }
        return null;
    }

    /**
     * 提取年龄
     */
    private Integer extractAge(String text) {
        // 匹配年龄模式（如：年龄：40 或 年龄 40）
        Pattern pattern = Pattern.compile("(?:年龄|岁)[：:：\\s]+(\\d{1,3})(?:岁)?");
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            try {
                Integer age = Integer.parseInt(matcher.group(1));
                System.out.println("提取到年龄: " + age);
                return age;
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    /**
     * 提取身份证号
     */
    private String extractIdCard(String text) {
        // 匹配身份证号模式（18位），支持多种格式
        Pattern pattern1 = Pattern.compile("(?:身份证|身份证号|证件号)[：:：\\s]+(\\d{17}[\\dXx])");
        Matcher matcher1 = pattern1.matcher(text);
        if (matcher1.find()) {
            String idCard = matcher1.group(1).toUpperCase();
            System.out.println("提取到身份证号: " + idCard);
            return idCard;
        }
        // 如果没有标签，直接匹配18位数字（可能是身份证号）
        Pattern pattern2 = Pattern.compile("\\b(\\d{17}[\\dXx])\\b");
        Matcher matcher2 = pattern2.matcher(text);
        if (matcher2.find()) {
            String idCard = matcher2.group(1).toUpperCase();
            // 验证是否是有效的身份证号格式
            if (idCard.matches("\\d{17}[\\dXx]")) {
                System.out.println("提取到身份证号(无标签): " + idCard);
                return idCard;
            }
        }
        return null;
    }

    /**
     * 提取健康证类别
     */
    private String extractCategory(String text) {
        // 匹配类别模式（如：类别：食品生产经营 或 类别 食品生产经营）
        // 支持多种分隔符：中文冒号、英文冒号、空格等
        Pattern pattern = Pattern.compile("(?:类别|类型)[：:：\\s]+([\\u4e00-\\u9fa5]{2,30})");
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            String category = matcher.group(1);
            System.out.println("提取到类别: " + category);
            return category;
        }
        return null;
    }

    /**
     * 提取发证日期
     */
    private LocalDate extractIssueDate(String text) {
        // 匹配发证日期模式（支持多种格式）
        Pattern pattern = Pattern.compile("(?:发证日期|发证时间|签发日期)[：:：\\s]+(\\d{4}[-/年.]\\d{1,2}[-/月.]\\d{1,2}[日]?)");
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            String dateStr = matcher.group(1).replace("年", "-").replace("月", "-").replace("日", "").replace(".", "-");
            LocalDate date = parseDate(dateStr);
            if (date != null) {
                System.out.println("提取到发证日期: " + date);
            }
            return date;
        }
        return null;
    }

    /**
     * 提取有效期至
     */
    private LocalDate extractExpiryDate(String text) {
        // 匹配有效期模式（支持多种格式）
        Pattern pattern = Pattern.compile("(?:有效期|有效期至|到期日期|到期时间)[：:：\\s]+(\\d{4}[-/年.]\\d{1,2}[-/月.]\\d{1,2}[日]?)");
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            String dateStr = matcher.group(1).replace("年", "-").replace("月", "-").replace("日", "").replace(".", "-");
            LocalDate date = parseDate(dateStr);
            if (date != null) {
                System.out.println("提取到有效期: " + date);
            }
            return date;
        }
        return null;
    }

    /**
     * 提取发证机构
     */
    private String extractIssuingAuthority(String text) {
        // 匹配发证机构模式（如：发证机构：嘉兴市南湖区大桥镇卫生院）
        Pattern pattern = Pattern.compile("(?:发证机构|签发机构)[：:：\\s]+([\\u4e00-\\u9fa5]{5,50})");
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            String authority = matcher.group(1);
            System.out.println("提取到发证机构: " + authority);
            return authority;
        }
        return null;
    }

    /**
     * 解析日期字符串
     */
    private LocalDate parseDate(String dateStr) {
        for (DateTimeFormatter formatter : DATE_FORMATTERS) {
            try {
                return LocalDate.parse(dateStr, formatter);
            } catch (DateTimeParseException e) {
                // 继续尝试下一个格式
            }
        }
        return null;
    }
}

