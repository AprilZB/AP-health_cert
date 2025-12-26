package com.microport.healthcert.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.microport.healthcert.entity.OperationLog;
import com.microport.healthcert.mapper.OperationLogMapper;
import com.microport.healthcert.service.OperationLogService;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 操作日志服务实现类
 * 
 * @author system
 * @date 2024
 */
@Slf4j
@Service
public class OperationLogServiceImpl implements OperationLogService {

    /**
     * 文件URL过期时间（毫秒），1小时
     */
    private static final long FILE_URL_EXPIRY = 60 * 60 * 1000L;

    /**
     * 文件URL缓存（key: 文件名, value: 创建时间）
     */
    private static final Map<String, Long> FILE_URL_CACHE = new ConcurrentHashMap<>();

    @Autowired
    private OperationLogMapper operationLogMapper;

    @Value("${server.servlet.context-path:}")
    private String contextPath;

    /**
     * 保存日志
     * 
     * @param userId 用户ID
     * @param userName 用户名
     * @param userType 用户类型
     * @param operation 操作类型
     * @param description 操作描述
     */
    @Override
    public void saveLog(Long userId, String userName, String userType, String operation, String description) {
        try {
            OperationLog log = new OperationLog();
            log.setUserId(userId);
            log.setUserName(userName);
            log.setUserType(userType);
            log.setOperation(operation);
            log.setDescription(description);
            log.setResult("success");
            log.setCreatedAt(LocalDateTime.now());
            operationLogMapper.insert(log);
        } catch (Exception e) {
            log.error("保存操作日志失败", e);
        }
    }

    /**
     * 查询日志
     * 
     * @param page 页码
     * @param size 每页大小
     * @param filters 筛选条件
     * @return 分页的日志列表
     */
    @Override
    public Page<OperationLog> queryLogs(Integer page, Integer size, Map<String, Object> filters) {
        // 创建分页对象
        Page<OperationLog> pageObj = new Page<>(page != null ? page : 1, size != null ? size : 10);

        // 构建查询条件
        LambdaQueryWrapper<OperationLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(OperationLog::getCreatedAt); // 按创建时间倒序

        // 应用筛选条件
        if (filters != null) {
            if (filters.containsKey("userName")) {
                wrapper.like(OperationLog::getUserName, filters.get("userName"));
            }
            if (filters.containsKey("userType")) {
                wrapper.eq(OperationLog::getUserType, filters.get("userType"));
            }
            if (filters.containsKey("operation")) {
                wrapper.eq(OperationLog::getOperation, filters.get("operation"));
            }
            if (filters.containsKey("startTime")) {
                wrapper.ge(OperationLog::getCreatedAt, filters.get("startTime"));
            }
            if (filters.containsKey("endTime")) {
                wrapper.le(OperationLog::getCreatedAt, filters.get("endTime"));
            }
        }

        // 执行查询
        return operationLogMapper.selectPage(pageObj, wrapper);
    }

    /**
     * 导出日志
     * 
     * @param filters 筛选条件
     * @return 导出文件URL
     */
    @Override
    public String exportLogs(Map<String, Object> filters) {
        FileOutputStream fileOut = null;
        XSSFWorkbook workbook = null;
        try {
            // 查询日志数据（不分页，查询全部）
            LambdaQueryWrapper<OperationLog> wrapper = new LambdaQueryWrapper<>();
            wrapper.orderByDesc(OperationLog::getCreatedAt);

            // 应用筛选条件
            if (filters != null) {
                if (filters.containsKey("userName")) {
                    wrapper.like(OperationLog::getUserName, filters.get("userName"));
                }
                if (filters.containsKey("userType")) {
                    wrapper.eq(OperationLog::getUserType, filters.get("userType"));
                }
                if (filters.containsKey("operation")) {
                    wrapper.eq(OperationLog::getOperation, filters.get("operation"));
                }
                if (filters.containsKey("startTime")) {
                    wrapper.ge(OperationLog::getCreatedAt, filters.get("startTime"));
                }
                if (filters.containsKey("endTime")) {
                    wrapper.le(OperationLog::getCreatedAt, filters.get("endTime"));
                }
            }

            List<OperationLog> logs = operationLogMapper.selectList(wrapper);

            // 创建Excel工作簿
            workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("操作日志");

            // 创建标题行
            Row headerRow = sheet.createRow(0);
            String[] headers = {"ID", "用户ID", "用户名", "用户类型", "操作类型", "操作描述", "操作结果", "创建时间"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                CellStyle style = workbook.createCellStyle();
                org.apache.poi.ss.usermodel.Font font = workbook.createFont();
                font.setBold(true);
                style.setFont(font);
                cell.setCellStyle(style);
            }

            // 填充数据
            int rowNum = 1;
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            for (OperationLog log : logs) {
                Row row = sheet.createRow(rowNum);
                row.createCell(0).setCellValue(log.getId() != null ? log.getId().toString() : "");
                row.createCell(1).setCellValue(log.getUserId() != null ? log.getUserId().toString() : "");
                row.createCell(2).setCellValue(log.getUserName() != null ? log.getUserName() : "");
                row.createCell(3).setCellValue(log.getUserType() != null ? log.getUserType() : "");
                row.createCell(4).setCellValue(log.getOperation() != null ? log.getOperation() : "");
                row.createCell(5).setCellValue(log.getDescription() != null ? log.getDescription() : "");
                row.createCell(6).setCellValue(log.getResult() != null ? log.getResult() : "");
                row.createCell(7).setCellValue(log.getCreatedAt() != null ? log.getCreatedAt().format(formatter) : "");
                rowNum++;
            }

            // 自动调整列宽
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
                int width = sheet.getColumnWidth(i);
                if (width < 2000) {
                    sheet.setColumnWidth(i, 2000);
                }
            }

            // 生成临时文件
            String fileName = "operation_logs_" + System.currentTimeMillis() + ".xlsx";
            String filePath = getDownloadPath() + File.separator + fileName;
            File file = new File(filePath);
            file.getParentFile().mkdirs();

            fileOut = new FileOutputStream(file);
            workbook.write(fileOut);

            // 记录文件创建时间（用于URL过期控制）
            FILE_URL_CACHE.put(fileName, System.currentTimeMillis());

            log.info("操作日志导出成功，文件：{}，记录数：{}", fileName, logs.size());

            // 返回下载URL
            return getDownloadUrl(fileName);

        } catch (Exception e) {
            log.error("操作日志导出失败", e);
            throw new RuntimeException("操作日志导出失败：" + e.getMessage(), e);
        } finally {
            try {
                if (fileOut != null) {
                    fileOut.close();
                }
                if (workbook != null) {
                    workbook.close();
                }
            } catch (IOException e) {
                log.error("关闭文件流失败", e);
            }
        }
    }

    /**
     * 获取下载目录路径
     * 
     * @return 下载目录路径
     */
    private String getDownloadPath() {
        String userDir = System.getProperty("user.dir");
        String downloadDir = userDir + File.separator + "downloads";
        File dir = new File(downloadDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return downloadDir;
    }

    /**
     * 获取下载URL
     * 
     * @param fileName 文件名
     * @return 下载URL
     */
    private String getDownloadUrl(String fileName) {
        String baseUrl = contextPath.isEmpty() ? "" : contextPath;
        return baseUrl + "/api/admin/download/" + fileName;
    }

    /**
     * 检查文件URL是否过期
     * 
     * @param fileName 文件名
     * @return true表示未过期，false表示已过期
     */
    public static boolean isFileUrlValid(String fileName) {
        Long createTime = FILE_URL_CACHE.get(fileName);
        if (createTime == null) {
            return false;
        }
        return System.currentTimeMillis() - createTime < FILE_URL_EXPIRY;
    }
}

