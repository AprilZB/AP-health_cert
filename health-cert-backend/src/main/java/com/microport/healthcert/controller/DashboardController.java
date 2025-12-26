package com.microport.healthcert.controller;

import com.microport.healthcert.common.Result;
import com.microport.healthcert.service.DashboardService;
import com.microport.healthcert.vo.DashboardOverviewVO;
import com.microport.healthcert.vo.EmployeeListVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 数据看板控制器
 * 提供数据统计和图表数据接口
 * 
 * @author system
 * @date 2024
 */
@RestController
@RequestMapping("/api/admin/dashboard")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    /**
     * 获取概览统计
     * 
     * @return 概览统计数据
     */
    @GetMapping("/overview")
    public Result<DashboardOverviewVO> getOverview() {
        try {
            DashboardOverviewVO overview = dashboardService.getOverviewStatistics();
            return Result.success(overview);
        } catch (Exception e) {
            return Result.error(500, "获取概览统计失败：" + e.getMessage());
        }
    }

    /**
     * 获取图表数据
     * 
     * @param chartType 图表类型（status/department/frontline）
     * @return 图表数据
     */
    @GetMapping("/charts")
    public Result<Map<String, Object>> getCharts(@RequestParam("chartType") String chartType) {
        try {
            Map<String, Object> chartData = dashboardService.getChartData(chartType);
            return Result.success(chartData);
        } catch (IllegalArgumentException e) {
            return Result.error(400, e.getMessage());
        } catch (Exception e) {
            return Result.error(500, "获取图表数据失败：" + e.getMessage());
        }
    }

    /**
     * 获取下钻员工列表
     * 根据状态类型返回对应的员工列表，按部门排序
     * 
     * @param statusType 状态类型：submitted(已提交)、approved(已通过)、expiring(即将到期)、expired(已过期)、noCert(没有健康证)
     * @return 员工列表
     */
    @GetMapping("/employee-list")
    public Result<List<EmployeeListVO>> getEmployeeList(@RequestParam("statusType") String statusType) {
        try {
            List<EmployeeListVO> employeeList = dashboardService.getEmployeeListByStatus(statusType);
            return Result.success(employeeList);
        } catch (IllegalArgumentException e) {
            return Result.error(400, e.getMessage());
        } catch (Exception e) {
            return Result.error(500, "获取员工列表失败：" + e.getMessage());
        }
    }
}

