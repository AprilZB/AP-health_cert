package com.microport.healthcert;

import com.microport.healthcert.dto.HealthCertDTO;
import com.microport.healthcert.service.impl.OcrServiceImpl;
import okhttp3.*;
import java.io.File;

/**
 * OCR功能测试类
 * 用于测试OCR识别功能
 */
public class OcrTest {
    
    public static void main(String[] args) {
        System.out.println("开始测试OCR功能...");
        
        // OCR服务地址
        String ocrUrl = "http://10.11.100.238:8081/OCR";
        
        // 测试图片列表
        String[] testImages = {"TEST1.jpg", "TEST2.jpg"};
        
        for (String imageFile : testImages) {
            File file = new File(imageFile);
            if (file.exists()) {
                System.out.println("\n========================================");
                System.out.println("测试图片: " + imageFile);
                System.out.println("========================================");
                
                try {
                    // 创建OkHttpClient
                    OkHttpClient client = new OkHttpClient();
                    
                    // 创建请求体（multipart/form-data）
                    RequestBody requestBody = new MultipartBody.Builder()
                            .setType(MultipartBody.FORM)
                            .addFormDataPart("image", file.getName(),
                                    RequestBody.create(file, MediaType.parse("image/jpeg")))
                            .build();
                    
                    // 创建请求
                    Request request = new Request.Builder()
                            .url(ocrUrl)
                            .post(requestBody)
                            .build();
                    
                    // 执行请求
                    System.out.println("正在调用OCR服务: " + ocrUrl);
                    try (Response response = client.newCall(request).execute()) {
                        System.out.println("响应状态码: " + response.code());
                        
                        if (!response.isSuccessful()) {
                            System.out.println("OCR请求失败，状态码: " + response.code());
                            if (response.body() != null) {
                                System.out.println("错误响应: " + response.body().string());
                            }
                            continue;
                        }
                        
                        // 获取响应体
                        String responseBody = response.body() != null ? response.body().string() : "";
                        if (responseBody.isEmpty()) {
                            System.out.println("OCR响应为空");
                            continue;
                        }
                        
                        // 打印OCR原始响应
                        System.out.println("\nOCR原始响应:");
                        System.out.println(responseBody);
                        System.out.println("\n");
                        
                        // 解析OCR结果
                        OcrServiceImpl ocrService = new OcrServiceImpl();
                        HealthCertDTO result = ocrService.parseOcrResult(responseBody);
                        
                        // 打印解析结果
                        System.out.println("OCR解析结果:");
                        System.out.println("  健康证编号: " + result.getCertNumber());
                        System.out.println("  姓名: " + result.getEmployeeName());
                        System.out.println("  性别: " + result.getGender());
                        System.out.println("  年龄: " + result.getAge());
                        System.out.println("  身份证号: " + result.getIdCard());
                        System.out.println("  类别: " + result.getCategory());
                        System.out.println("  发证日期: " + result.getIssueDate());
                        System.out.println("  有效期至: " + result.getExpiryDate());
                        System.out.println("  发证机构: " + result.getIssuingAuthority());
                        
                    }
                } catch (Exception e) {
                    System.err.println("OCR调用失败: " + e.getMessage());
                    e.printStackTrace();
                }
            } else {
                System.out.println("图片文件不存在: " + imageFile);
            }
        }
        
        System.out.println("\n测试完成！");
    }
}

