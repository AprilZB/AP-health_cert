package com.microport.healthcert.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.microport.healthcert.entity.Employee;
import com.microport.healthcert.entity.HealthCertificate;
import com.microport.healthcert.mapper.EmployeeMapper;
import com.microport.healthcert.mapper.HealthCertificateMapper;
import com.microport.healthcert.service.DashboardService;
import com.microport.healthcert.vo.DashboardOverviewVO;
import com.microport.healthcert.vo.EmployeeListVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 数据看板服务实现类
 * 
 * @author system
 * @date 2024
 */
@Slf4j
@Service
public class DashboardServiceImpl implements DashboardService {

    @Autowired
    private EmployeeMapper employeeMapper;

    @Autowired
    private HealthCertificateMapper healthCertificateMapper;

    /**
     * 获取概览统计
     * 
     * @return 概览统计数据
     */
    @Override
    public DashboardOverviewVO getOverviewStatistics() {
        DashboardOverviewVO overview = new DashboardOverviewVO();

        try {
            // 1. 总员工数
            Long totalEmployees = employeeMapper.selectCount(null);
            overview.setTotalEmployees(totalEmployees.intValue());

            // 2. 在职员工数（is_active=1）
            LambdaQueryWrapper<Employee> activeWrapper = new LambdaQueryWrapper<>();
            activeWrapper.eq(Employee::getIsActive, 1);
            Long activeEmployees = employeeMapper.selectCount(activeWrapper);
            overview.setActiveEmployees(activeEmployees.intValue());

            // 3. 已提交健康证数（status不为draft）
            LambdaQueryWrapper<HealthCertificate> submittedWrapper = new LambdaQueryWrapper<>();
            submittedWrapper.ne(HealthCertificate::getStatus, "draft");
            Long submittedCount = healthCertificateMapper.selectCount(submittedWrapper);
            overview.setSubmittedCount(submittedCount.intValue());

            // 4. 待审核健康证数（status=pending）
            LambdaQueryWrapper<HealthCertificate> pendingWrapper = new LambdaQueryWrapper<>();
            pendingWrapper.eq(HealthCertificate::getStatus, "pending");
            Long pendingCount = healthCertificateMapper.selectCount(pendingWrapper);
            overview.setPendingCount(pendingCount.intValue());

            // 5. 已通过健康证数（status=approved且is_current=1）
            LambdaQueryWrapper<HealthCertificate> approvedWrapper = new LambdaQueryWrapper<>();
            approvedWrapper.eq(HealthCertificate::getStatus, "approved")
                          .eq(HealthCertificate::getIsCurrent, 1);
            Long approvedCount = healthCertificateMapper.selectCount(approvedWrapper);
            overview.setApprovedCount(approvedCount.intValue());

            // 6. 即将到期数（30/15/7天）
            LocalDate today = LocalDate.now();
            
            // 30天后到期
            LocalDate date30Days = today.plusDays(30);
            LambdaQueryWrapper<HealthCertificate> expiring30Wrapper = new LambdaQueryWrapper<>();
            expiring30Wrapper.eq(HealthCertificate::getExpiryDate, date30Days)
                            .eq(HealthCertificate::getStatus, "approved")
                            .eq(HealthCertificate::getIsCurrent, 1);
            Long expiring30Days = healthCertificateMapper.selectCount(expiring30Wrapper);
            overview.setExpiring30Days(expiring30Days.intValue());

            // 15天后到期
            LocalDate date15Days = today.plusDays(15);
            LambdaQueryWrapper<HealthCertificate> expiring15Wrapper = new LambdaQueryWrapper<>();
            expiring15Wrapper.eq(HealthCertificate::getExpiryDate, date15Days)
                            .eq(HealthCertificate::getStatus, "approved")
                            .eq(HealthCertificate::getIsCurrent, 1);
            Long expiring15Days = healthCertificateMapper.selectCount(expiring15Wrapper);
            overview.setExpiring15Days(expiring15Days.intValue());

            // 7天后到期
            LocalDate date7Days = today.plusDays(7);
            LambdaQueryWrapper<HealthCertificate> expiring7Wrapper = new LambdaQueryWrapper<>();
            expiring7Wrapper.eq(HealthCertificate::getExpiryDate, date7Days)
                           .eq(HealthCertificate::getStatus, "approved")
                           .eq(HealthCertificate::getIsCurrent, 1);
            Long expiring7Days = healthCertificateMapper.selectCount(expiring7Wrapper);
            overview.setExpiring7Days(expiring7Days.intValue());

            // 7. 已过期数（expiry_date < today 且 status=approved 且 is_current=1）
            LambdaQueryWrapper<HealthCertificate> expiredWrapper = new LambdaQueryWrapper<>();
            expiredWrapper.lt(HealthCertificate::getExpiryDate, today)
                         .eq(HealthCertificate::getStatus, "approved")
                         .eq(HealthCertificate::getIsCurrent, 1);
            Long expiredCount = healthCertificateMapper.selectCount(expiredWrapper);
            overview.setExpiredCount(expiredCount.intValue());

            // 8. 覆盖率（已通过健康证数 / 在职员工数 * 100）
            if (activeEmployees > 0) {
                double coverageRate = (approvedCount.doubleValue() / activeEmployees.doubleValue()) * 100;
                overview.setCoverageRate(Math.round(coverageRate * 100.0) / 100.0); // 保留两位小数
            } else {
                overview.setCoverageRate(0.0);
            }

            // 9. 没有健康证信息的员工数量（在职员工中没有有效健康证的）
            // 查询所有在职员工
            List<Employee> activeEmployeeList = employeeMapper.selectList(activeWrapper);
            // 查询所有有效的健康证（status=approved且is_current=1）
            LambdaQueryWrapper<HealthCertificate> validCertWrapper = new LambdaQueryWrapper<>();
            validCertWrapper.eq(HealthCertificate::getStatus, "approved")
                           .eq(HealthCertificate::getIsCurrent, 1);
            List<HealthCertificate> validCerts = healthCertificateMapper.selectList(validCertWrapper);
            // 提取有健康证的员工ID集合
            java.util.Set<Long> employeesWithCert = new java.util.HashSet<>();
            for (HealthCertificate cert : validCerts) {
                if (cert.getEmployeeId() != null) {
                    employeesWithCert.add(cert.getEmployeeId());
                }
            }
            // 统计没有健康证的员工数量
            int noCertCount = 0;
            for (Employee emp : activeEmployeeList) {
                if (!employeesWithCert.contains(emp.getId())) {
                    noCertCount++;
                }
            }
            overview.setNoCertCount(noCertCount);

        } catch (Exception e) {
            // 统计失败，返回空数据
            log.error("获取概览统计失败", e);
        }

        return overview;
    }

    /**
     * 获取图表数据
     * 
     * @param chartType 图表类型（status/department/frontline）
     * @return 图表数据
     */
    @Override
    public Map<String, Object> getChartData(String chartType) {
        Map<String, Object> result = new HashMap<>();

        try {
            if ("status".equals(chartType)) {
                // 状态分布饼图数据
                result = getStatusChartData();
            } else if ("department".equals(chartType)) {
                // 部门统计柱状图数据
                result = getDepartmentChartData();
            } else if ("frontline".equals(chartType)) {
                // 一线/非一线统计
                result = getFrontlineChartData();
            } else {
                throw new IllegalArgumentException("不支持的图表类型：" + chartType);
            }
        } catch (Exception e) {
            log.error("获取图表数据失败，chartType：{}", chartType, e);
            result.put("error", "获取图表数据失败");
        }

        return result;
    }

    /**
     * 获取状态分布饼图数据
     * 已提交和待审核合并为一个蓝色项
     * 
     * @return 图表数据
     */
    private Map<String, Object> getStatusChartData() {
        Map<String, Object> result = new HashMap<>();

        // 查询各状态的数量
        // 1. 已提交（status不为draft的所有健康证，包括pending、approved、rejected、expired）
        LambdaQueryWrapper<HealthCertificate> submittedWrapper = new LambdaQueryWrapper<>();
        submittedWrapper.ne(HealthCertificate::getStatus, "draft");
        Long submittedCount = healthCertificateMapper.selectCount(submittedWrapper);

        // 2. 已通过（status=approved且is_current=1）
        LambdaQueryWrapper<HealthCertificate> approvedWrapper = new LambdaQueryWrapper<>();
        approvedWrapper.eq(HealthCertificate::getStatus, "approved")
                      .eq(HealthCertificate::getIsCurrent, 1);
        Long approvedCount = healthCertificateMapper.selectCount(approvedWrapper);

        // 3. 即将到期（status=approved且is_current=1，且expiry_date在7-30天内）
        LocalDate today = LocalDate.now();
        LocalDate date7Days = today.plusDays(7);
        LocalDate date30Days = today.plusDays(30);
        LambdaQueryWrapper<HealthCertificate> expiringWrapper = new LambdaQueryWrapper<>();
        expiringWrapper.eq(HealthCertificate::getStatus, "approved")
                      .eq(HealthCertificate::getIsCurrent, 1)
                      .ge(HealthCertificate::getExpiryDate, date7Days)
                      .le(HealthCertificate::getExpiryDate, date30Days);
        Long expiringCount = healthCertificateMapper.selectCount(expiringWrapper);

        // 4. 已过期（status=approved且is_current=1，且expiry_date < today）
        LambdaQueryWrapper<HealthCertificate> expiredWrapper = new LambdaQueryWrapper<>();
        expiredWrapper.eq(HealthCertificate::getStatus, "approved")
                     .eq(HealthCertificate::getIsCurrent, 1)
                     .lt(HealthCertificate::getExpiryDate, today);
        Long expiredCount = healthCertificateMapper.selectCount(expiredWrapper);

        // 5. 没有健康证（在职员工中没有有效健康证的）
        LambdaQueryWrapper<Employee> activeWrapper = new LambdaQueryWrapper<>();
        activeWrapper.eq(Employee::getIsActive, 1);
        List<Employee> activeEmployeeList = employeeMapper.selectList(activeWrapper);
        LambdaQueryWrapper<HealthCertificate> validCertWrapper = new LambdaQueryWrapper<>();
        validCertWrapper.eq(HealthCertificate::getStatus, "approved")
                       .eq(HealthCertificate::getIsCurrent, 1);
        List<HealthCertificate> validCerts = healthCertificateMapper.selectList(validCertWrapper);
        Set<Long> employeesWithCert = new HashSet<>();
        for (HealthCertificate cert : validCerts) {
            if (cert.getEmployeeId() != null) {
                employeesWithCert.add(cert.getEmployeeId());
            }
        }
        int noCertCount = 0;
        for (Employee emp : activeEmployeeList) {
            if (!employeesWithCert.contains(emp.getId())) {
                noCertCount++;
            }
        }

        // 构建图表数据
        List<Map<String, Object>> data = new ArrayList<>();

        // 已提交（蓝色）- 合并了已提交和待审核
        Map<String, Object> submittedData = new HashMap<>();
        submittedData.put("label", "已提交");
        submittedData.put("value", submittedCount.intValue());
        data.add(submittedData);

        // 已通过（绿色）
        Map<String, Object> approvedData = new HashMap<>();
        approvedData.put("label", "已通过");
        approvedData.put("value", approvedCount.intValue());
        data.add(approvedData);

        // 即将到期（黄色）
        Map<String, Object> expiringData = new HashMap<>();
        expiringData.put("label", "即将到期");
        expiringData.put("value", expiringCount.intValue());
        data.add(expiringData);

        // 已过期（红色）
        Map<String, Object> expiredData = new HashMap<>();
        expiredData.put("label", "已过期");
        expiredData.put("value", expiredCount.intValue());
        data.add(expiredData);

        // 没有健康证（紫色）
        Map<String, Object> noCertData = new HashMap<>();
        noCertData.put("label", "没有健康证");
        noCertData.put("value", noCertCount);
        data.add(noCertData);

        result.put("labels", new String[]{"已提交", "已通过", "即将到期", "已过期", "没有健康证"});
        result.put("data", data);

        return result;
    }

    /**
     * 获取部门统计柱状图数据
     * 
     * @return 图表数据
     */
    private Map<String, Object> getDepartmentChartData() {
        Map<String, Object> result = new HashMap<>();

        // 查询所有在职员工
        LambdaQueryWrapper<Employee> employeeWrapper = new LambdaQueryWrapper<>();
        employeeWrapper.eq(Employee::getIsActive, 1);
        List<Employee> employees = employeeMapper.selectList(employeeWrapper);

        // 按部门统计
        Map<String, Integer> deptCountMap = new HashMap<>();
        for (Employee employee : employees) {
            String deptName = employee.getDepartNameCn();
            if (deptName == null || deptName.trim().isEmpty()) {
                deptName = "未分配部门";
            }
            deptCountMap.put(deptName, deptCountMap.getOrDefault(deptName, 0) + 1);
        }

        // 构建图表数据
        List<String> labels = new ArrayList<>();
        List<Integer> values = new ArrayList<>();

        for (Map.Entry<String, Integer> entry : deptCountMap.entrySet()) {
            labels.add(entry.getKey());
            values.add(entry.getValue());
        }

        result.put("labels", labels);
        result.put("data", values);

        return result;
    }

    /**
     * 获取一线/非一线统计
     * 
     * @return 图表数据
     */
    private Map<String, Object> getFrontlineChartData() {
        Map<String, Object> result = new HashMap<>();

        // 查询在职员工中一线员工数
        LambdaQueryWrapper<Employee> frontlineWrapper = new LambdaQueryWrapper<>();
        frontlineWrapper.eq(Employee::getIsActive, 1)
                       .eq(Employee::getIsFrontlineWorker, 1);
        Long frontlineCount = employeeMapper.selectCount(frontlineWrapper);

        // 查询在职员工中非一线员工数
        LambdaQueryWrapper<Employee> nonFrontlineWrapper = new LambdaQueryWrapper<>();
        nonFrontlineWrapper.eq(Employee::getIsActive, 1)
                          .eq(Employee::getIsFrontlineWorker, 0);
        Long nonFrontlineCount = employeeMapper.selectCount(nonFrontlineWrapper);

        // 构建图表数据
        List<Map<String, Object>> data = new ArrayList<>();

        Map<String, Object> frontlineData = new HashMap<>();
        frontlineData.put("label", "一线员工");
        frontlineData.put("value", frontlineCount.intValue());
        data.add(frontlineData);

        Map<String, Object> nonFrontlineData = new HashMap<>();
        nonFrontlineData.put("label", "非一线员工");
        nonFrontlineData.put("value", nonFrontlineCount.intValue());
        data.add(nonFrontlineData);

        result.put("labels", new String[]{"一线员工", "非一线员工"});
        result.put("data", data);

        return result;
    }

    /**
     * 获取下钻员工列表
     * 根据状态类型返回对应的员工列表，按部门排序
     * 
     * @param statusType 状态类型：submitted(已提交)、approved(已通过)、expiring(即将到期)、expired(已过期)、noCert(没有健康证)
     * @return 员工列表，按部门排序
     */
    @Override
    public List<EmployeeListVO> getEmployeeListByStatus(String statusType) {
        List<EmployeeListVO> result = new ArrayList<>();
        
        try {
            LocalDate today = LocalDate.now();
            LocalDate date7Days = today.plusDays(7);
            LocalDate date30Days = today.plusDays(30);
            
            if ("submitted".equals(statusType)) {
                // 已提交：查询所有status不为draft的健康证
                LambdaQueryWrapper<HealthCertificate> wrapper = new LambdaQueryWrapper<>();
                wrapper.ne(HealthCertificate::getStatus, "draft")
                       .orderByAsc(HealthCertificate::getEmployeeName);
                List<HealthCertificate> certs = healthCertificateMapper.selectList(wrapper);
                
                for (HealthCertificate cert : certs) {
                    EmployeeListVO vo = new EmployeeListVO();
                    vo.setEmployeeId(cert.getEmployeeId());
                    vo.setEmployeeName(cert.getEmployeeName());
                    vo.setCertNumber(cert.getCertNumber());
                    vo.setExpiryDate(cert.getExpiryDate());
                    vo.setCertStatus(cert.getStatus());
                    
                    // 查询员工信息获取部门
                    if (cert.getEmployeeId() != null) {
                        Employee emp = employeeMapper.selectById(cert.getEmployeeId());
                        if (emp != null) {
                            vo.setDepartmentName(emp.getDepartNameCn() != null ? emp.getDepartNameCn() : "未分配部门");
                            vo.setMpNumber(emp.getMpNumber());
                            vo.setJobName(emp.getJobNameCn());
                        } else {
                            vo.setDepartmentName("未知部门");
                        }
                    } else {
                        vo.setDepartmentName("未知部门");
                    }
                    
                    result.add(vo);
                }
                
            } else if ("approved".equals(statusType)) {
                // 已通过：查询status=approved且is_current=1的健康证
                LambdaQueryWrapper<HealthCertificate> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(HealthCertificate::getStatus, "approved")
                       .eq(HealthCertificate::getIsCurrent, 1)
                       .orderByAsc(HealthCertificate::getEmployeeName);
                List<HealthCertificate> certs = healthCertificateMapper.selectList(wrapper);
                
                for (HealthCertificate cert : certs) {
                    EmployeeListVO vo = new EmployeeListVO();
                    vo.setEmployeeId(cert.getEmployeeId());
                    vo.setEmployeeName(cert.getEmployeeName());
                    vo.setCertNumber(cert.getCertNumber());
                    vo.setExpiryDate(cert.getExpiryDate());
                    vo.setCertStatus(cert.getStatus());
                    
                    if (cert.getEmployeeId() != null) {
                        Employee emp = employeeMapper.selectById(cert.getEmployeeId());
                        if (emp != null) {
                            vo.setDepartmentName(emp.getDepartNameCn() != null ? emp.getDepartNameCn() : "未分配部门");
                            vo.setMpNumber(emp.getMpNumber());
                            vo.setJobName(emp.getJobNameCn());
                        } else {
                            vo.setDepartmentName("未知部门");
                        }
                    } else {
                        vo.setDepartmentName("未知部门");
                    }
                    
                    result.add(vo);
                }
                
            } else if ("expiring".equals(statusType)) {
                // 即将到期：查询status=approved且is_current=1，且expiry_date在7-30天内的健康证
                // 注意：date7Days是今天+7天，date30Days是今天+30天
                // 所以需要查询expiry_date >= date7Days 且 expiry_date <= date30Days
                LambdaQueryWrapper<HealthCertificate> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(HealthCertificate::getStatus, "approved")
                       .eq(HealthCertificate::getIsCurrent, 1)
                       .ge(HealthCertificate::getExpiryDate, date7Days)
                       .le(HealthCertificate::getExpiryDate, date30Days)
                       .orderByAsc(HealthCertificate::getExpiryDate)
                       .orderByAsc(HealthCertificate::getEmployeeName);
                List<HealthCertificate> certs = healthCertificateMapper.selectList(wrapper);
                
                for (HealthCertificate cert : certs) {
                    EmployeeListVO vo = new EmployeeListVO();
                    vo.setEmployeeId(cert.getEmployeeId());
                    vo.setEmployeeName(cert.getEmployeeName());
                    vo.setCertNumber(cert.getCertNumber());
                    vo.setExpiryDate(cert.getExpiryDate());
                    vo.setCertStatus(cert.getStatus());
                    
                    if (cert.getEmployeeId() != null) {
                        Employee emp = employeeMapper.selectById(cert.getEmployeeId());
                        if (emp != null) {
                            vo.setDepartmentName(emp.getDepartNameCn() != null ? emp.getDepartNameCn() : "未分配部门");
                            vo.setMpNumber(emp.getMpNumber());
                            vo.setJobName(emp.getJobNameCn());
                        } else {
                            vo.setDepartmentName("未知部门");
                        }
                    } else {
                        vo.setDepartmentName("未知部门");
                    }
                    
                    result.add(vo);
                }
                
            } else if ("expired".equals(statusType)) {
                // 已过期：查询status=approved且is_current=1，且expiry_date < today的健康证
                LambdaQueryWrapper<HealthCertificate> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(HealthCertificate::getStatus, "approved")
                       .eq(HealthCertificate::getIsCurrent, 1)
                       .lt(HealthCertificate::getExpiryDate, today)
                       .orderByAsc(HealthCertificate::getEmployeeName);
                List<HealthCertificate> certs = healthCertificateMapper.selectList(wrapper);
                
                for (HealthCertificate cert : certs) {
                    EmployeeListVO vo = new EmployeeListVO();
                    vo.setEmployeeId(cert.getEmployeeId());
                    vo.setEmployeeName(cert.getEmployeeName());
                    vo.setCertNumber(cert.getCertNumber());
                    vo.setExpiryDate(cert.getExpiryDate());
                    vo.setCertStatus(cert.getStatus());
                    
                    if (cert.getEmployeeId() != null) {
                        Employee emp = employeeMapper.selectById(cert.getEmployeeId());
                        if (emp != null) {
                            vo.setDepartmentName(emp.getDepartNameCn() != null ? emp.getDepartNameCn() : "未分配部门");
                            vo.setMpNumber(emp.getMpNumber());
                            vo.setJobName(emp.getJobNameCn());
                        } else {
                            vo.setDepartmentName("未知部门");
                        }
                    } else {
                        vo.setDepartmentName("未知部门");
                    }
                    
                    result.add(vo);
                }
                
            } else if ("noCert".equals(statusType)) {
                // 没有健康证：查询在职员工中没有有效健康证的
                LambdaQueryWrapper<Employee> activeWrapper = new LambdaQueryWrapper<>();
                activeWrapper.eq(Employee::getIsActive, 1);
                List<Employee> activeEmployees = employeeMapper.selectList(activeWrapper);
                
                // 查询所有有效的健康证
                LambdaQueryWrapper<HealthCertificate> validCertWrapper = new LambdaQueryWrapper<>();
                validCertWrapper.eq(HealthCertificate::getStatus, "approved")
                               .eq(HealthCertificate::getIsCurrent, 1);
                List<HealthCertificate> validCerts = healthCertificateMapper.selectList(validCertWrapper);
                
                // 提取有健康证的员工ID集合
                Set<Long> employeesWithCert = new HashSet<>();
                for (HealthCertificate cert : validCerts) {
                    if (cert.getEmployeeId() != null) {
                        employeesWithCert.add(cert.getEmployeeId());
                    }
                }
                
                // 找出没有健康证的员工
                for (Employee emp : activeEmployees) {
                    if (!employeesWithCert.contains(emp.getId())) {
                        EmployeeListVO vo = new EmployeeListVO();
                        vo.setEmployeeId(emp.getId());
                        vo.setEmployeeName(emp.getName());
                        vo.setDepartmentName(emp.getDepartNameCn() != null ? emp.getDepartNameCn() : "未分配部门");
                        vo.setMpNumber(emp.getMpNumber());
                        vo.setJobName(emp.getJobNameCn());
                        vo.setCertStatus("无健康证");
                        result.add(vo);
                    }
                }
            }
            
            // 按部门名称排序
            result.sort((a, b) -> {
                String deptA = a.getDepartmentName() != null ? a.getDepartmentName() : "";
                String deptB = b.getDepartmentName() != null ? b.getDepartmentName() : "";
                int deptCompare = deptA.compareTo(deptB);
                if (deptCompare != 0) {
                    return deptCompare;
                }
                // 部门相同，按姓名排序
                String nameA = a.getEmployeeName() != null ? a.getEmployeeName() : "";
                String nameB = b.getEmployeeName() != null ? b.getEmployeeName() : "";
                return nameA.compareTo(nameB);
            });
            
        } catch (Exception e) {
            log.error("获取下钻员工列表失败，statusType：{}", statusType, e);
        }
        
        return result;
    }
}

