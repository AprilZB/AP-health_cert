package com.microport.healthcert.service;

import com.microport.healthcert.vo.DashboardOverviewVO;
import com.microport.healthcert.vo.EmployeeListVO;

import java.util.List;
import java.util.Map;

/**
 * 数据看板服务接口
 * 提供数据统计和图表数据功能
 * 
 * @author system
 * @date 2024
 */
public interface DashboardService {

    /**
     * 获取概览统计
     * 包含总员工数、在职数、已提交、待审核、已通过数、即将到期数、已过期数、覆盖率、没有健康证数量
     * 
     * @return 概览统计数据
     */
    DashboardOverviewVO getOverviewStatistics();

    /**
     * 获取图表数据
     * 
     * @param chartType 图表类型（status/department/frontline）
     * @return 图表数据（Map格式，前端可直接使用）
     */
    Map<String, Object> getChartData(String chartType);

    /**
     * 获取下钻员工列表
     * 根据状态类型返回对应的员工列表，按部门排序
     * 
     * @param statusType 状态类型：submitted(已提交)、approved(已通过)、expiring(即将到期)、expired(已过期)、noCert(没有健康证)
     * @return 员工列表，按部门排序
     */
    List<EmployeeListVO> getEmployeeListByStatus(String statusType);
}

