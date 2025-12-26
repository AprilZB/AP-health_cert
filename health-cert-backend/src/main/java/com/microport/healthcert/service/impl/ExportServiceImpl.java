package com.microport.healthcert.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.microport.healthcert.entity.HealthCertificate;
import com.microport.healthcert.mapper.HealthCertificateMapper;
import com.microport.healthcert.service.ExportService;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFClientAnchor;
import org.apache.poi.xssf.usermodel.XSSFDrawing;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 导出服务实现类
 * 
 * @author system
 * @date 2024
 */
@Slf4j
@Service
public class ExportServiceImpl implements ExportService {

    /**
     * 文件URL过期时间（毫秒），1小时
     */
    private static final long FILE_URL_EXPIRY = 60 * 60 * 1000L;

    /**
     * 文件URL缓存（key: 文件名, value: 创建时间）
     */
    private static final Map<String, Long> FILE_URL_CACHE = new ConcurrentHashMap<>();

    @Autowired
    private HealthCertificateMapper healthCertificateMapper;

    @Value("${server.servlet.context-path:}")
    private String contextPath;

    /**
     * 导出Excel
     * 
     * @param filters 筛选条件
     * @param includeImages 是否包含图片
     * @return 下载URL
     */
    @Override
    public String exportExcel(Map<String, Object> filters, Boolean includeImages) {
        FileOutputStream fileOut = null;
        XSSFWorkbook workbook = null;
        try {
            // 查询健康证数据（分批查询，考虑性能）
            List<HealthCertificate> certificates = queryCertificates(filters);

            // 创建Excel工作簿
            workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("健康证列表");

            // 创建标题行
            createExcelHeader(sheet);

            // 填充数据
            int rowNum = 1;
            for (HealthCertificate cert : certificates) {
                Row row = sheet.createRow(rowNum);
                createExcelRow(workbook, sheet, row, cert, includeImages);
                rowNum++;

                // 如果包含图片，需要调整行高
                if (includeImages && cert.getImagePath() != null) {
                    row.setHeightInPoints(80);
                }
            }

            // 自动调整列宽
            autoSizeColumns(sheet, includeImages);

            // 生成临时文件
            String fileName = "health_cert_" + System.currentTimeMillis() + ".xlsx";
            String filePath = getDownloadPath() + File.separator + fileName;
            File file = new File(filePath);
            file.getParentFile().mkdirs();

            fileOut = new FileOutputStream(file);
            workbook.write(fileOut);

            // 记录文件创建时间（用于URL过期控制）
            FILE_URL_CACHE.put(fileName, System.currentTimeMillis());

            log.info("Excel导出成功，文件：{}，记录数：{}", fileName, certificates.size());

            // 返回下载URL
            return getDownloadUrl(fileName);

        } catch (Exception e) {
            log.error("Excel导出失败", e);
            throw new RuntimeException("Excel导出失败：" + e.getMessage(), e);
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
     * 导出PDF
     * 
     * @param filters 筛选条件
     * @param includeImages 是否包含图片
     * @return 下载URL
     */
    @Override
    public String exportPdf(Map<String, Object> filters, Boolean includeImages) {
        Document document = null;
        PdfWriter writer = null;
        try {
            // 查询健康证数据（分批查询，考虑性能）
            List<HealthCertificate> certificates = queryCertificates(filters);

            // 生成临时文件
            String fileName = "health_cert_" + System.currentTimeMillis() + ".pdf";
            String filePath = getDownloadPath() + File.separator + fileName;
            File file = new File(filePath);
            file.getParentFile().mkdirs();

            // 创建PDF文档
            document = new Document(PageSize.A4.rotate()); // 横向
            writer = PdfWriter.getInstance(document, new FileOutputStream(file));
            document.open();

            // 创建表格（7列，如果包含图片则8列）
            int columnCount = includeImages ? 8 : 7;
            PdfPTable table = new PdfPTable(columnCount);
            table.setWidthPercentage(100);

            // 创建表头
            createPdfHeader(table, includeImages);

            // 填充数据
            for (HealthCertificate cert : certificates) {
                createPdfRow(table, cert, includeImages, document, writer);
            }

            document.add(table);
            document.close();

            // 记录文件创建时间（用于URL过期控制）
            FILE_URL_CACHE.put(fileName, System.currentTimeMillis());

            log.info("PDF导出成功，文件：{}，记录数：{}", fileName, certificates.size());

            // 返回下载URL
            return getDownloadUrl(fileName);

        } catch (Exception e) {
            log.error("PDF导出失败", e);
            throw new RuntimeException("PDF导出失败：" + e.getMessage(), e);
        } finally {
            if (document != null && document.isOpen()) {
                document.close();
            }
        }
    }

    /**
     * 查询健康证数据（考虑性能，使用索引字段查询）
     * 
     * @param filters 筛选条件
     * @return 健康证列表
     */
    private List<HealthCertificate> queryCertificates(Map<String, Object> filters) {
        LambdaQueryWrapper<HealthCertificate> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(HealthCertificate::getCreatedAt); // 按创建时间倒序

        // 应用筛选条件
        if (filters != null) {
            if (filters.containsKey("status")) {
                wrapper.eq(HealthCertificate::getStatus, filters.get("status"));
            }
            if (filters.containsKey("employeeName")) {
                wrapper.like(HealthCertificate::getEmployeeName, filters.get("employeeName"));
            }
            if (filters.containsKey("certNumber")) {
                wrapper.like(HealthCertificate::getCertNumber, filters.get("certNumber"));
            }
            if (filters.containsKey("sfUserId")) {
                wrapper.eq(HealthCertificate::getSfUserId, filters.get("sfUserId"));
            }
        }

        // 分批查询，避免一次性加载过多数据
        // 先获取总数，如果超过1000条，需要分批处理
        long total = healthCertificateMapper.selectCount(wrapper);
        if (total > 1000) {
            log.warn("导出数据量较大：{}条，可能影响性能", total);
        }

        return healthCertificateMapper.selectList(wrapper);
    }

    /**
     * 创建Excel表头
     * 
     * @param sheet Excel工作表
     */
    private void createExcelHeader(Sheet sheet) {
        Row headerRow = sheet.createRow(0);
        String[] headers = {"编号", "员工姓名", "员工域账号", "性别", "年龄", "健康证编号", "发证日期", "有效期至", "发证机构", "状态"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            CellStyle style = sheet.getWorkbook().createCellStyle();
            org.apache.poi.ss.usermodel.Font font = sheet.getWorkbook().createFont();
            font.setBold(true);
            style.setFont(font);
            cell.setCellStyle(style);
        }
        // 如果包含图片，添加图片列
        if (sheet.getRow(0).getPhysicalNumberOfCells() == headers.length) {
            Cell imageCell = headerRow.createCell(headers.length);
            imageCell.setCellValue("健康证图片");
            CellStyle style = sheet.getWorkbook().createCellStyle();
            org.apache.poi.ss.usermodel.Font font = sheet.getWorkbook().createFont();
            font.setBold(true);
            style.setFont(font);
            imageCell.setCellStyle(style);
        }
    }

    /**
     * 创建Excel数据行
     * 
     * @param workbook Excel工作簿
     * @param sheet Excel工作表
     * @param row Excel行
     * @param cert 健康证数据
     * @param includeImages 是否包含图片
     */
    private void createExcelRow(XSSFWorkbook workbook, Sheet sheet, Row row, HealthCertificate cert, Boolean includeImages) {
        int colNum = 0;
        row.createCell(colNum++).setCellValue(cert.getId() != null ? cert.getId().toString() : "");
        row.createCell(colNum++).setCellValue(cert.getEmployeeName() != null ? cert.getEmployeeName() : "");
        row.createCell(colNum++).setCellValue(cert.getSfUserId() != null ? cert.getSfUserId() : "");
        row.createCell(colNum++).setCellValue(cert.getGender() != null ? cert.getGender() : "");
        row.createCell(colNum++).setCellValue(cert.getAge() != null ? cert.getAge() : 0);
        row.createCell(colNum++).setCellValue(cert.getCertNumber() != null ? cert.getCertNumber() : "");
        row.createCell(colNum++).setCellValue(cert.getIssueDate() != null ? cert.getIssueDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) : "");
        row.createCell(colNum++).setCellValue(cert.getExpiryDate() != null ? cert.getExpiryDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) : "");
        row.createCell(colNum++).setCellValue(cert.getIssuingAuthority() != null ? cert.getIssuingAuthority() : "");
        row.createCell(colNum++).setCellValue(getStatusText(cert.getStatus()));

        // 如果包含图片，插入图片
        if (includeImages && cert.getImagePath() != null) {
            try {
                insertExcelImage(workbook, sheet, row, cert.getImagePath(), colNum);
            } catch (Exception e) {
                log.warn("插入图片失败，健康证编号：{}，图片路径：{}", cert.getCertNumber(), cert.getImagePath(), e);
            }
        }
    }

    /**
     * 插入Excel图片
     * 
     * @param workbook Excel工作簿
     * @param sheet Excel工作表
     * @param row Excel行
     * @param imagePath 图片路径
     * @param colNum 列号
     */
    private void insertExcelImage(XSSFWorkbook workbook, Sheet sheet, Row row, String imagePath, int colNum) throws IOException {
        String fullImagePath = getImageFullPath(imagePath);
        File imageFile = new File(fullImagePath);
        if (!imageFile.exists()) {
            log.warn("图片文件不存在：{}", fullImagePath);
            return;
        }

        // 读取图片
        byte[] imageBytes = Files.readAllBytes(imageFile.toPath());
        int pictureIdx = workbook.addPicture(imageBytes, Workbook.PICTURE_TYPE_JPEG);

        // 创建绘图对象
        XSSFDrawing drawing = (XSSFDrawing) sheet.createDrawingPatriarch();
        XSSFClientAnchor anchor = new XSSFClientAnchor(0, 0, 0, 0, colNum, row.getRowNum(), colNum + 1, row.getRowNum() + 1);
        anchor.setAnchorType(ClientAnchor.AnchorType.MOVE_AND_RESIZE);

        // 插入图片
        drawing.createPicture(anchor, pictureIdx);
    }

    /**
     * 自动调整列宽
     * 
     * @param sheet Excel工作表
     * @param includeImages 是否包含图片
     */
    private void autoSizeColumns(Sheet sheet, Boolean includeImages) {
        int columnCount = includeImages ? 11 : 10;
        for (int i = 0; i < columnCount; i++) {
            sheet.autoSizeColumn(i);
            // 设置最小列宽
            int width = sheet.getColumnWidth(i);
            if (width < 2000) {
                sheet.setColumnWidth(i, 2000);
            }
        }
        // 图片列设置固定宽度
        if (includeImages) {
            sheet.setColumnWidth(10, 5000);
        }
    }

    /**
     * 创建PDF表头
     * 
     * @param table PDF表格
     * @param includeImages 是否包含图片
     */
    private void createPdfHeader(PdfPTable table, Boolean includeImages) throws DocumentException {
        String[] headers = {"编号", "员工姓名", "员工域账号", "性别", "年龄", "健康证编号", "发证日期", "有效期至", "发证机构", "状态"};
        if (includeImages) {
            headers = new String[]{"编号", "员工姓名", "员工域账号", "性别", "年龄", "健康证编号", "发证日期", "有效期至", "发证机构", "状态", "图片"};
        }

        com.itextpdf.text.Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPadding(5);
            table.addCell(cell);
        }
    }

    /**
     * 创建PDF数据行
     * 
     * @param table PDF表格
     * @param cert 健康证数据
     * @param includeImages 是否包含图片
     * @param document PDF文档
     * @param writer PDF写入器
     */
    private void createPdfRow(PdfPTable table, HealthCertificate cert, Boolean includeImages, Document document, PdfWriter writer) {
        com.itextpdf.text.Font cellFont = FontFactory.getFont(FontFactory.HELVETICA, 9);

        table.addCell(createPdfCell(cert.getId() != null ? cert.getId().toString() : "", cellFont));
        table.addCell(createPdfCell(cert.getEmployeeName() != null ? cert.getEmployeeName() : "", cellFont));
        table.addCell(createPdfCell(cert.getSfUserId() != null ? cert.getSfUserId() : "", cellFont));
        table.addCell(createPdfCell(cert.getGender() != null ? cert.getGender() : "", cellFont));
        table.addCell(createPdfCell(cert.getAge() != null ? cert.getAge().toString() : "", cellFont));
        table.addCell(createPdfCell(cert.getCertNumber() != null ? cert.getCertNumber() : "", cellFont));
        table.addCell(createPdfCell(cert.getIssueDate() != null ? cert.getIssueDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) : "", cellFont));
        table.addCell(createPdfCell(cert.getExpiryDate() != null ? cert.getExpiryDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) : "", cellFont));
        table.addCell(createPdfCell(cert.getIssuingAuthority() != null ? cert.getIssuingAuthority() : "", cellFont));
        table.addCell(createPdfCell(getStatusText(cert.getStatus()), cellFont));

        // 如果包含图片，插入图片
        if (includeImages) {
            try {
                insertPdfImage(table, cert.getImagePath());
            } catch (Exception e) {
                log.warn("插入PDF图片失败，健康证编号：{}，图片路径：{}", cert.getCertNumber(), cert.getImagePath(), e);
                table.addCell(createPdfCell("", cellFont));
            }
        }
    }

    /**
     * 创建PDF单元格
     * 
     * @param text 文本内容
     * @param font 字体
     * @return PDF单元格
     */
    private PdfPCell createPdfCell(String text, com.itextpdf.text.Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setPadding(5);
        return cell;
    }

    /**
     * 插入PDF图片
     * 
     * @param table PDF表格
     * @param imagePath 图片路径
     */
    private void insertPdfImage(PdfPTable table, String imagePath) throws Exception {
        String fullImagePath = getImageFullPath(imagePath);
        File imageFile = new File(fullImagePath);
        if (!imageFile.exists()) {
            log.warn("图片文件不存在：{}", fullImagePath);
            table.addCell(new PdfPCell());
            return;
        }

        Image image = Image.getInstance(fullImagePath);
        image.scaleToFit(50, 50); // 缩放图片大小
        PdfPCell cell = new PdfPCell(image, true);
        cell.setPadding(2);
        table.addCell(cell);
    }

    /**
     * 获取状态文本
     * 
     * @param status 状态值
     * @return 状态文本
     */
    private String getStatusText(String status) {
        if (status == null) {
            return "";
        }
        switch (status) {
            case "draft":
                return "草稿";
            case "pending":
                return "待审核";
            case "approved":
                return "已通过";
            case "rejected":
                return "已拒绝";
            case "expired":
                return "已过期";
            default:
                return status;
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
     * 获取图片完整路径
     * 
     * @param imagePath 相对路径
     * @return 完整路径
     */
    private String getImageFullPath(String imagePath) {
        if (imagePath == null || imagePath.trim().isEmpty()) {
            return "";
        }
        String userDir = System.getProperty("user.dir");
        if (imagePath.startsWith("/") || imagePath.startsWith("\\")) {
            return userDir + imagePath;
        } else {
            return userDir + File.separator + imagePath;
        }
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

