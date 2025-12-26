package com.microport.healthcert.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.microport.healthcert.dto.SyncResultDTO;
import com.microport.healthcert.entity.Department;
import com.microport.healthcert.entity.Employee;
import com.microport.healthcert.entity.SystemConfig;
import com.microport.healthcert.entity.remote.HrSync;
import com.microport.healthcert.mapper.DepartmentMapper;
import com.microport.healthcert.mapper.EmployeeMapper;
import com.microport.healthcert.mapper.SystemConfigMapper;
import com.microport.healthcert.mapper.remote.HrSyncMapper;
import com.microport.healthcert.service.SyncService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 员工同步服务实现类
 * 
 * @author system
 * @date 2024
 */
@Slf4j
@Service
public class SyncServiceImpl implements SyncService {

    /**
     * 全局同步标志的配置键
     */
    private static final String SYNC_FLAG_KEY = "sync.in_progress";

    @Autowired
    private HrSyncMapper hrSyncMapper;

    @Autowired
    private EmployeeMapper employeeMapper;

    @Autowired
    private DepartmentMapper departmentMapper;

    @Autowired
    private SystemConfigMapper systemConfigMapper;

    /**
     * 同步员工数据
     * 从远程hr_sync表同步到本地employees表
     * 
     * @return 同步结果（新增数/更新数/离职数）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public SyncResultDTO syncEmployees() {
        long startTime = System.currentTimeMillis();
        SyncResultDTO result = new SyncResultDTO();
        result.setStartTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        try {
            // 1. 设置全局同步标志
            setSyncFlag(true);

            // 2. 查询远程hr_sync表全部数据
            List<HrSync> remoteEmployees = hrSyncMapper.selectList(null);
            log.info("从远程数据库查询到{}条员工数据", remoteEmployees.size());

            // 3. 查询本地employees表全部数据
            List<Employee> localEmployees = employeeMapper.selectList(null);
            Map<String, Employee> localEmployeeMap = localEmployees.stream()
                    .collect(Collectors.toMap(Employee::getSfUserId, e -> e, (e1, e2) -> e1));

            // 4. 对比并同步员工数据
            int addedCount = 0;
            int updatedCount = 0;
            Map<String, Boolean> remoteSfUserIds = new HashMap<>();

            for (HrSync remote : remoteEmployees) {
                remoteSfUserIds.put(remote.getSfUserId(), true);

                Employee local = localEmployeeMap.get(remote.getSfUserId());

                if (local == null) {
                    // 远程有本地无 → 尝试新增
                    // 但可能由于大小写、空格等原因导致map中没有匹配到，所以再次查询数据库确认
                    LambdaQueryWrapper<Employee> checkWrapper = new LambdaQueryWrapper<>();
                    checkWrapper.eq(Employee::getSfUserId, remote.getSfUserId());
                    Employee existingEmployee = employeeMapper.selectOne(checkWrapper);
                    
                    if (existingEmployee != null) {
                        // 数据库中存在，说明map匹配失败（可能是大小写或空格问题），改为更新
                        local = existingEmployee;
                        log.warn("员工{}在map中未匹配到，但数据库中已存在，改为更新", remote.getSfUserId());
                    } else {
                        // 确实不存在，执行新增
                        Employee newEmployee = new Employee();
                        BeanUtils.copyProperties(remote, newEmployee);
                        // 远程表的密码字段是pwd，本地表的密码字段是password
                        // BeanUtils.copyProperties 会复制password字段（因为实体类中都有password属性）
                        if (remote.getPassword() != null && !remote.getPassword().trim().isEmpty()) {
                            newEmployee.setPassword(remote.getPassword());
                        } else {
                            // 如果远程没有密码，使用sf_user_id作为默认密码
                            newEmployee.setPassword(remote.getSfUserId());
                        }
                        // 远程表is_frontline_worker是字符类型('Y'/'N')，本地表是tinyint(1/0)
                        if (remote.getIsFrontlineWorker() != null && "Y".equalsIgnoreCase(remote.getIsFrontlineWorker().trim())) {
                            newEmployee.setIsFrontlineWorker(1);
                        } else {
                            newEmployee.setIsFrontlineWorker(0);
                        }
                        // 远程表没有mobile字段，新增时设置为null（后续可以从钉钉等其他渠道获取）
                        newEmployee.setMobile(null);
                        newEmployee.setIsActive(1);
                        newEmployee.setSyncTime(LocalDateTime.now());
                        try {
                            employeeMapper.insert(newEmployee);
                            addedCount++;
                            log.debug("新增员工：{}", remote.getSfUserId());
                        } catch (Exception e) {
                            // 如果插入失败（可能是唯一约束冲突），再次查询并更新
                            if (e.getMessage() != null && e.getMessage().contains("Duplicate entry")) {
                                log.warn("插入员工{}时发生唯一约束冲突，改为更新：{}", remote.getSfUserId(), e.getMessage());
                                Employee conflictEmployee = employeeMapper.selectOne(checkWrapper);
                                if (conflictEmployee != null) {
                                    local = conflictEmployee;
                                } else {
                                    throw e; // 如果查询不到，抛出原异常
                                }
                            } else {
                                throw e; // 其他异常直接抛出
                            }
                        }
                    }
                }
                
                // 如果local不为null（无论是从map获取的，还是从数据库查询到的，还是从异常处理中获取的），执行更新逻辑
                if (local != null) {
                    // 远程有本地有 → 更新信息
                    // 保存本地的mobile字段，因为远程表没有这个字段
                    String originalMobile = local.getMobile();
                    BeanUtils.copyProperties(remote, local);
                    // 远程表的密码字段是pwd，本地表的密码字段是password
                    // BeanUtils.copyProperties 会复制password字段（从remote.password到local.password）
                    if (remote.getPassword() != null && !remote.getPassword().trim().isEmpty()) {
                        local.setPassword(remote.getPassword());
                    }
                    // 远程表is_frontline_worker是字符类型('Y'/'N')，本地表是tinyint(1/0)
                    if (remote.getIsFrontlineWorker() != null && "Y".equalsIgnoreCase(remote.getIsFrontlineWorker().trim())) {
                        local.setIsFrontlineWorker(1);
                    } else {
                        local.setIsFrontlineWorker(0);
                    }
                    // 恢复本地的mobile字段，因为远程表没有这个字段，不应被覆盖
                    local.setMobile(originalMobile);
                    local.setSyncTime(LocalDateTime.now());
                    local.setIsActive(1); // 如果在远程库中存在，标记为在职
                    employeeMapper.updateById(local);
                    updatedCount++;
                    log.debug("更新员工：{}", remote.getSfUserId());
                }
            }

            // 5. 远程无本地有 → 标记is_active=0
            int inactiveCount = 0;
            for (Employee local : localEmployees) {
                if (!remoteSfUserIds.containsKey(local.getSfUserId())) {
                    local.setIsActive(0);
                    local.setSyncTime(LocalDateTime.now());
                    employeeMapper.updateById(local);
                    inactiveCount++;
                    log.debug("标记离职员工：{}", local.getSfUserId());
                }
            }

            // 6. 同步部门数据（根据depart_name_cn和sup_dep构建部门树）
            syncDepartments(remoteEmployees);

            // 7. 清除全局同步标志
            setSyncFlag(false);

            // 8. 设置同步结果
            long endTime = System.currentTimeMillis();
            result.setAddedCount(addedCount);
            result.setUpdatedCount(updatedCount);
            result.setInactiveCount(inactiveCount);
            result.setEndTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            result.setDuration(endTime - startTime);

            log.info("员工同步完成：新增{}条，更新{}条，离职{}条，耗时{}ms", 
                    addedCount, updatedCount, inactiveCount, endTime - startTime);

            return result;

        } catch (Exception e) {
            // 同步失败，清除同步标志
            setSyncFlag(false);
            log.error("员工同步失败", e);
            throw new RuntimeException("员工同步失败：" + e.getMessage(), e);
        }
    }

    /**
     * 同步部门数据
     * 根据depart_name_cn和sup_dep构建部门树
     * 
     * @param remoteEmployees 远程员工列表
     */
    private void syncDepartments(List<HrSync> remoteEmployees) {
        // 收集所有部门信息
        Map<String, String> deptParentMap = new HashMap<>(); // 部门名 -> 上级部门名

        for (HrSync employee : remoteEmployees) {
            if (employee.getDepartNameCn() != null && !employee.getDepartNameCn().trim().isEmpty()) {
                deptParentMap.put(employee.getDepartNameCn(), employee.getSupDep());
            }
        }

        // 同步部门数据
        for (Map.Entry<String, String> entry : deptParentMap.entrySet()) {
            String deptName = entry.getKey();
            String parentDeptName = entry.getValue();

            // 查询部门是否已存在
            LambdaQueryWrapper<Department> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Department::getDeptName, deptName);
            Department department = departmentMapper.selectOne(wrapper);

            if (department == null) {
                // 部门不存在，创建新部门
                department = new Department();
                department.setDeptName(deptName);
                department.setParentDeptName(parentDeptName);
                department.setIsActive(1);

                // 查找上级部门ID
                if (parentDeptName != null && !parentDeptName.trim().isEmpty()) {
                    LambdaQueryWrapper<Department> parentWrapper = new LambdaQueryWrapper<>();
                    parentWrapper.eq(Department::getDeptName, parentDeptName);
                    Department parentDept = departmentMapper.selectOne(parentWrapper);
                    if (parentDept != null) {
                        department.setParentId(parentDept.getId());
                        department.setDeptLevel(parentDept.getDeptLevel() + 1);
                        department.setDeptPath(parentDept.getDeptPath() + deptName + "/");
                    } else {
                        // 上级部门不存在，设为根部门
                        department.setDeptLevel(1);
                        department.setDeptPath("/" + deptName + "/");
                    }
                } else {
                    // 没有上级部门，设为根部门
                    department.setDeptLevel(1);
                    department.setDeptPath("/" + deptName + "/");
                }

                departmentMapper.insert(department);
                log.debug("新增部门：{}", deptName);
            } else {
                // 部门已存在，更新信息
                department.setParentDeptName(parentDeptName);
                department.setIsActive(1);

                // 更新上级部门ID和层级
                if (parentDeptName != null && !parentDeptName.trim().isEmpty()) {
                    LambdaQueryWrapper<Department> parentWrapper = new LambdaQueryWrapper<>();
                    parentWrapper.eq(Department::getDeptName, parentDeptName);
                    Department parentDept = departmentMapper.selectOne(parentWrapper);
                    if (parentDept != null) {
                        department.setParentId(parentDept.getId());
                        department.setDeptLevel(parentDept.getDeptLevel() + 1);
                        department.setDeptPath(parentDept.getDeptPath() + deptName + "/");
                    }
                }

                departmentMapper.updateById(department);
                log.debug("更新部门：{}", deptName);
            }
        }
    }

    /**
     * 设置全局同步标志
     * 
     * @param inProgress 是否正在同步
     */
    private void setSyncFlag(boolean inProgress) {
        LambdaQueryWrapper<SystemConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SystemConfig::getConfigKey, SYNC_FLAG_KEY);
        SystemConfig config = systemConfigMapper.selectOne(wrapper);

        if (config == null) {
            // 配置不存在，创建新配置
            config = new SystemConfig();
            config.setConfigKey(SYNC_FLAG_KEY);
            config.setConfigValue(String.valueOf(inProgress));
            config.setConfigType("string");
            config.setDescription("员工同步进行中标志");
            config.setGroupName("system");
            systemConfigMapper.insert(config);
        } else {
            // 更新配置值
            config.setConfigValue(String.valueOf(inProgress));
            systemConfigMapper.updateById(config);
        }
    }

    /**
     * 检查是否正在同步
     * 
     * @return true表示正在同步，false表示未同步
     */
    public boolean isSyncInProgress() {
        LambdaQueryWrapper<SystemConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SystemConfig::getConfigKey, SYNC_FLAG_KEY);
        SystemConfig config = systemConfigMapper.selectOne(wrapper);

        if (config == null) {
            return false;
        }

        return "true".equalsIgnoreCase(config.getConfigValue());
    }
}

