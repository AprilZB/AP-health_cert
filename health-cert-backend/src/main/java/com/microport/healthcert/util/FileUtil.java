package com.microport.healthcert.util;

import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.UUID;

/**
 * 文件工具类
 * 提供文件保存和验证功能
 * 
 * @author system
 * @date 2024
 */
public class FileUtil {

    /**
     * 保存文件
     * 生成UUID文件名，创建日期目录，返回相对路径
     * 
     * @param file 上传的文件
     * @param uploadDir 上传目录（相对于项目根目录）
     * @return 文件的相对路径
     * @throws IOException IO异常
     */
    public static String saveFile(MultipartFile file, String uploadDir) throws IOException {
        // 获取原始文件名
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            throw new IllegalArgumentException("文件名不能为空");
        }

        // 获取文件扩展名
        String extension = "";
        int lastDotIndex = originalFilename.lastIndexOf(".");
        if (lastDotIndex > 0) {
            extension = originalFilename.substring(lastDotIndex);
        }

        // 生成UUID文件名
        String uuidFilename = UUID.randomUUID().toString() + extension;

        // 创建日期目录（格式：uploads/2024/12/）
        LocalDate now = LocalDate.now();
        String year = String.valueOf(now.getYear());
        String month = String.format("%02d", now.getMonthValue());
        String dateDir = uploadDir + File.separator + year + File.separator + month;

        // 创建目录（如果不存在）
        Path datePath = Paths.get(dateDir);
        if (!Files.exists(datePath)) {
            Files.createDirectories(datePath);
        }

        // 保存文件
        Path filePath = datePath.resolve(uuidFilename);
        Files.write(filePath, file.getBytes());

        // 返回相对路径（相对于uploadDir）
        return year + File.separator + month + File.separator + uuidFilename;
    }

    /**
     * 验证图片
     * 检查文件类型和文件大小
     * 
     * @param file 上传的文件
     * @throws IllegalArgumentException 如果文件不符合要求
     */
    public static void validateImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }

        // 检查文件类型（jpg/png/bmp）
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            throw new IllegalArgumentException("文件名不能为空");
        }

        String extension = "";
        int lastDotIndex = originalFilename.lastIndexOf(".");
        if (lastDotIndex > 0) {
            extension = originalFilename.substring(lastDotIndex + 1).toLowerCase();
        }

        if (!extension.equals("jpg") && !extension.equals("jpeg") && 
            !extension.equals("png") && !extension.equals("bmp")) {
            throw new IllegalArgumentException("只支持jpg、png、bmp格式的图片");
        }

        // 检查文件大小（< 10MB）
        long maxSize = 10 * 1024 * 1024; // 10MB
        if (file.getSize() > maxSize) {
            throw new IllegalArgumentException("文件大小不能超过10MB");
        }
    }
}

