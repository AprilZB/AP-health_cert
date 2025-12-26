package com.microport.healthcert.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.microport.healthcert.entity.Department;
import com.microport.healthcert.entity.Employee;
import com.microport.healthcert.mapper.DepartmentMapper;
import com.microport.healthcert.mapper.EmployeeMapper;
import com.microport.healthcert.service.DepartmentService;
import com.microport.healthcert.vo.DepartmentTreeVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 部门管理服务实现类
 * 
 * @author system
 * @date 2024
 */
@Slf4j
@Service
public class DepartmentServiceImpl implements DepartmentService {

    @Autowired
    private DepartmentMapper departmentMapper;

    @Autowired
    private EmployeeMapper employeeMapper;

    /**
     * 查询部门列表（支持分页和筛选）
     * 
     * @param page 页码
     * @param size 每页大小
     * @param filters 筛选条件
     * @return 分页的部门列表
     */
    @Override
    public Page<Department> getDepartmentList(Integer page, Integer size, Map<String, Object> filters) {
        // 创建分页对象
        Page<Department> pageObj = new Page<>(page != null ? page : 1, size != null ? size : 10);

        // 构建查询条件
        LambdaQueryWrapper<Department> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(Department::getDeptLevel)
               .orderByAsc(Department::getSortOrder)
               .orderByAsc(Department::getDeptName);

        // 应用筛选条件
        if (filters != null) {
            if (filters.containsKey("deptName")) {
                wrapper.like(Department::getDeptName, filters.get("deptName"));
            }
            if (filters.containsKey("parentDeptName")) {
                wrapper.eq(Department::getParentDeptName, filters.get("parentDeptName"));
            }
            if (filters.containsKey("isActive")) {
                wrapper.eq(Department::getIsActive, filters.get("isActive"));
            }
            if (filters.containsKey("deptLevel")) {
                wrapper.eq(Department::getDeptLevel, filters.get("deptLevel"));
            }
        }

        // 执行查询
        return departmentMapper.selectPage(pageObj, wrapper);
    }

    /**
     * 查询所有部门（树形结构）
     * 
     * @return 部门树形列表
     */
    @Override
    public List<Department> getDepartmentTree() {
        // 查询所有部门
        List<Department> allDepartments = departmentMapper.selectList(null);

        // 按层级和排序号排序
        allDepartments.sort((d1, d2) -> {
            int levelCompare = Integer.compare(d1.getDeptLevel(), d2.getDeptLevel());
            if (levelCompare != 0) {
                return levelCompare;
            }
            int sortCompare = Integer.compare(
                d1.getSortOrder() != null ? d1.getSortOrder() : 0,
                d2.getSortOrder() != null ? d2.getSortOrder() : 0
            );
            if (sortCompare != 0) {
                return sortCompare;
            }
            return d1.getDeptName().compareTo(d2.getDeptName());
        });

        return allDepartments;
    }

    /**
     * 根据ID查询部门详情
     * 
     * @param id 部门ID
     * @return 部门信息
     */
    @Override
    public Department getDepartmentById(Long id) {
        return departmentMapper.selectById(id);
    }

    /**
     * 更新部门信息
     * 
     * @param department 部门信息
     * @return 更新后的部门信息
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Department updateDepartment(Department department) {
        if (department.getId() == null) {
            throw new IllegalArgumentException("部门ID不能为空");
        }

        // 查询原部门信息
        Department existing = departmentMapper.selectById(department.getId());
        if (existing == null) {
            throw new IllegalArgumentException("部门不存在");
        }

        // 更新部门信息
        department.setUpdatedAt(LocalDateTime.now());
        departmentMapper.updateById(department);

        // 返回更新后的部门信息
        return departmentMapper.selectById(department.getId());
    }

    /**
     * 更新部门人数统计
     * 根据employees表统计每个部门的人数
     * 
     * @return 更新的部门数量
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateEmployeeCount() {
        // 查询所有部门
        List<Department> departments = departmentMapper.selectList(null);
        int updateCount = 0;

        for (Department dept : departments) {
            // 统计该部门的在职员工数
            LambdaQueryWrapper<Employee> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Employee::getDepartNameCn, dept.getDeptName())
                   .eq(Employee::getIsActive, 1);
            Long count = employeeMapper.selectCount(wrapper);

            // 更新部门人数
            dept.setEmployeeCount(count != null ? count.intValue() : 0);
            dept.setUpdatedAt(LocalDateTime.now());
            departmentMapper.updateById(dept);
            updateCount++;
        }

        log.info("更新部门人数统计完成，共更新{}个部门", updateCount);
        return updateCount;
    }

    /**
     * 启用/禁用部门
     * 
     * @param id 部门ID
     * @param isActive 是否启用（1=启用，0=禁用）
     * @return 更新结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean setDepartmentActive(Long id, Integer isActive) {
        Department department = departmentMapper.selectById(id);
        if (department == null) {
            throw new IllegalArgumentException("部门不存在");
        }

        department.setIsActive(isActive);
        department.setUpdatedAt(LocalDateTime.now());
        int result = departmentMapper.updateById(department);

        return result > 0;
    }

    /**
     * 查询所有部门（树形结构，包含主管信息和人数统计）
     * 
     * @return 部门树形列表（包含主管姓名和部门人数）
     */
    @Override
    public List<DepartmentTreeVO> getDepartmentTreeWithDetails() {
        // 1. 查询所有部门
        List<Department> allDepartments = departmentMapper.selectList(null);

        // 2. 构建部门ID到部门的映射
        Map<Long, Department> deptMap = new HashMap<>();
        for (Department dept : allDepartments) {
            deptMap.put(dept.getId(), dept);
        }

        // 3. 统计每个部门的人数（仅本级部门，不包含下级部门）
        Map<String, Integer> deptEmployeeCountMap = new HashMap<>();
        Map<String, String> deptSupervisorMap = new HashMap<>();
        
        // 查询所有在职员工
        LambdaQueryWrapper<Employee> employeeWrapper = new LambdaQueryWrapper<>();
        employeeWrapper.eq(Employee::getIsActive, 1);
        List<Employee> allEmployees = employeeMapper.selectList(employeeWrapper);

        // 统计每个部门的人数
        for (Employee employee : allEmployees) {
            if (employee.getDepartNameCn() != null && !employee.getDepartNameCn().trim().isEmpty()) {
                String deptName = employee.getDepartNameCn();
                deptEmployeeCountMap.put(deptName, deptEmployeeCountMap.getOrDefault(deptName, 0) + 1);
            }
        }

        // 查找每个部门的主管
        // 主管逻辑：查找该部门下，supervisor_sf_user_id不在该部门的员工，通常是部门负责人
        for (Department dept : allDepartments) {
            String deptName = dept.getDeptName();
            String supervisorName = null;

            // 查找该部门下的所有员工
            List<Employee> deptEmployees = new ArrayList<>();
            for (Employee emp : allEmployees) {
                if (deptName.equals(emp.getDepartNameCn())) {
                    deptEmployees.add(emp);
                }
            }

            // 方法1：查找该部门下，supervisor_sf_user_id不在该部门的员工的主管
            Set<String> deptSfUserIds = new HashSet<>();
            for (Employee emp : deptEmployees) {
                if (emp.getSfUserId() != null) {
                    deptSfUserIds.add(emp.getSfUserId());
                }
            }

            for (Employee emp : deptEmployees) {
                if (emp.getSupervisorSfUserId() != null && !emp.getSupervisorSfUserId().trim().isEmpty()) {
                    // 如果主管不在该部门，说明该员工的主管可能是部门负责人
                    if (!deptSfUserIds.contains(emp.getSupervisorSfUserId())) {
                        // 查找主管信息
                        LambdaQueryWrapper<Employee> supervisorWrapper = new LambdaQueryWrapper<>();
                        supervisorWrapper.eq(Employee::getSfUserId, emp.getSupervisorSfUserId());
                        Employee supervisor = employeeMapper.selectOne(supervisorWrapper);
                        if (supervisor != null) {
                            supervisorName = supervisor.getName();
                            break;
                        }
                    }
                }
            }

            // 方法2：如果没找到主管，尝试查找该部门下职位最高的员工（如经理、主管等）
            if (supervisorName == null && !deptEmployees.isEmpty()) {
                // 按职位优先级查找（总经理 > 总监 > 经理 > 主管 > 其他）
                Employee bestMatch = null;
                int maxPriority = -1;
                
                for (Employee emp : deptEmployees) {
                    String jobName = emp.getJobNameCn();
                    if (jobName != null) {
                        int priority = 0;
                        String jobLower = jobName.toLowerCase();
                        if (jobLower.contains("总经理") || jobLower.contains("ceo")) {
                            priority = 5;
                        } else if (jobLower.contains("总监")) {
                            priority = 4;
                        } else if (jobLower.contains("经理")) {
                            priority = 3;
                        } else if (jobLower.contains("主管")) {
                            priority = 2;
                        } else if (jobLower.contains("负责人") || jobLower.contains("主任")) {
                            priority = 1;
                        }
                        
                        if (priority > maxPriority) {
                            maxPriority = priority;
                            bestMatch = emp;
                        }
                    }
                }
                
                if (bestMatch != null) {
                    supervisorName = bestMatch.getName();
                }
            }

            if (supervisorName != null) {
                deptSupervisorMap.put(deptName, supervisorName);
            }
        }

        // 4. 转换为VO并构建树形结构
        Map<Long, DepartmentTreeVO> voMap = new HashMap<>();
        List<DepartmentTreeVO> rootNodes = new ArrayList<>();

        // 先创建所有VO节点
        for (Department dept : allDepartments) {
            DepartmentTreeVO vo = new DepartmentTreeVO();
            BeanUtils.copyProperties(dept, vo);
            vo.setEmployeeCount(deptEmployeeCountMap.getOrDefault(dept.getDeptName(), 0));
            vo.setSupervisorName(deptSupervisorMap.getOrDefault(dept.getDeptName(), null));
            voMap.put(dept.getId(), vo);
        }

        // 构建树形结构
        for (Department dept : allDepartments) {
            DepartmentTreeVO vo = voMap.get(dept.getId());
            if (dept.getParentId() == null) {
                // 根节点
                rootNodes.add(vo);
            } else {
                // 子节点，添加到父节点的children中
                DepartmentTreeVO parent = voMap.get(dept.getParentId());
                if (parent != null) {
                    parent.getChildren().add(vo);
                } else {
                    // 父节点不存在，作为根节点处理
                    rootNodes.add(vo);
                }
            }
        }

        // 5. 对树进行排序
        sortDepartmentTree(rootNodes);

        return rootNodes;
    }

    /**
     * 对部门树进行排序
     * 
     * @param nodes 部门节点列表
     */
    private void sortDepartmentTree(List<DepartmentTreeVO> nodes) {
        if (nodes == null || nodes.isEmpty()) {
            return;
        }

        // 对当前层级排序
        nodes.sort((d1, d2) -> {
            int levelCompare = Integer.compare(d1.getDeptLevel(), d2.getDeptLevel());
            if (levelCompare != 0) {
                return levelCompare;
            }
            return d1.getDeptName().compareTo(d2.getDeptName());
        });

        // 递归排序子节点
        for (DepartmentTreeVO node : nodes) {
            if (node.getChildren() != null && !node.getChildren().isEmpty()) {
                sortDepartmentTree(node.getChildren());
            }
        }
    }
}

