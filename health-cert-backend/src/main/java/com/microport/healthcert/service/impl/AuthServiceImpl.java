package com.microport.healthcert.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.microport.healthcert.dto.LoginDTO;
import com.microport.healthcert.entity.Admin;
import com.microport.healthcert.entity.Employee;
import com.microport.healthcert.entity.OperationLog;
import com.microport.healthcert.entity.remote.HrSync;
import com.microport.healthcert.mapper.AdminMapper;
import com.microport.healthcert.mapper.EmployeeMapper;
import com.microport.healthcert.mapper.OperationLogMapper;
import com.microport.healthcert.mapper.remote.HrSyncMapper;
import com.microport.healthcert.service.AuthService;
import com.microport.healthcert.util.JwtUtil;
import com.microport.healthcert.vo.LoginVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 认证服务实现类
 * 
 * @author system
 * @date 2024
 */
@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private AdminMapper adminMapper;

    @Autowired
    private EmployeeMapper employeeMapper;

    @Autowired
    private HrSyncMapper hrSyncMapper;

    @Autowired
    private OperationLogMapper operationLogMapper;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 用户登录
     * 先查本地admins表，如果不存在，再查本地employees表，最后查远程hr_sync表
     * 如果找到远程员工，同步到本地employees表
     * 
     * @param loginDTO 登录请求DTO
     * @return 登录响应VO（包含token和用户信息）
     */
    @Override
    public LoginVO login(LoginDTO loginDTO) {
        String username = loginDTO.getUsername();
        String password = loginDTO.getPassword();

        // 1. 先查本地admins表
        LambdaQueryWrapper<Admin> adminWrapper = new LambdaQueryWrapper<>();
        adminWrapper.eq(Admin::getUsername, username)
                    .eq(Admin::getPassword, password)
                    .eq(Admin::getIsActive, 1);
        Admin admin = adminMapper.selectOne(adminWrapper);

        if (admin != null) {
            // 管理员登录成功
            // 更新最后登录时间
            admin.setLastLoginTime(LocalDateTime.now());
            adminMapper.updateById(admin);

            // 生成Token
            String token = jwtUtil.generateToken(admin.getId(), admin.getUsername(), "admin");

            // 创建LoginVO
            LoginVO loginVO = new LoginVO();
            loginVO.setToken(token);
            loginVO.setUserId(admin.getId());
            loginVO.setUsername(admin.getUsername());
            loginVO.setUserType("admin");
            loginVO.setRealName(admin.getRealName());
            loginVO.setEmail(admin.getEmail());

            // 记录操作日志
            saveOperationLog(admin.getId(), admin.getUsername(), "admin", "login", "管理员登录");

            return loginVO;
        }

        // 2. 查本地employees表（优先使用本地数据）
        LambdaQueryWrapper<Employee> employeeWrapper = new LambdaQueryWrapper<>();
        employeeWrapper.eq(Employee::getSfUserId, username)
                      .eq(Employee::getPassword, password)
                      .eq(Employee::getIsActive, 1);
        Employee employee = employeeMapper.selectOne(employeeWrapper);

        if (employee != null) {
            // 本地员工登录成功
            // 生成Token
            String token = jwtUtil.generateToken(employee.getId(), employee.getSfUserId(), "employee");

            // 创建LoginVO
            LoginVO loginVO = new LoginVO();
            loginVO.setToken(token);
            loginVO.setUserId(employee.getId());
            loginVO.setUsername(employee.getSfUserId());
            loginVO.setUserType("employee");
            loginVO.setRealName(employee.getName());
            loginVO.setEmail(employee.getEmail());

            // 记录操作日志
            saveOperationLog(employee.getId(), employee.getSfUserId(), "employee", "login", "员工登录");

            return loginVO;
        }

        // 3. 如果本地employees表不存在，再查远程hr_sync表
        LambdaQueryWrapper<HrSync> hrSyncWrapper = new LambdaQueryWrapper<>();
        hrSyncWrapper.eq(HrSync::getSfUserId, username)
                     .eq(HrSync::getPassword, password);
        HrSync hrSync = hrSyncMapper.selectOne(hrSyncWrapper);

        if (hrSync != null) {
            // 找到远程员工，同步到本地employees表
            // 先查本地是否已存在（可能密码已更新）
            LambdaQueryWrapper<Employee> existingEmployeeWrapper = new LambdaQueryWrapper<>();
            existingEmployeeWrapper.eq(Employee::getSfUserId, hrSync.getSfUserId());
            Employee existingEmployee = employeeMapper.selectOne(existingEmployeeWrapper);

            if (existingEmployee == null) {
                // 本地不存在，创建新员工记录
                Employee newEmployee = new Employee();
                BeanUtils.copyProperties(hrSync, newEmployee);
                // 远程表is_frontline_worker是字符类型('Y'/'N')，本地表是tinyint(1/0)
                if (hrSync.getIsFrontlineWorker() != null && "Y".equalsIgnoreCase(hrSync.getIsFrontlineWorker().trim())) {
                    newEmployee.setIsFrontlineWorker(1);
                } else {
                    newEmployee.setIsFrontlineWorker(0);
                }
                // 远程表没有mobile字段，新增时设置为null
                newEmployee.setMobile(null);
                newEmployee.setDingtalkUserid(null);
                newEmployee.setIsActive(1);
                newEmployee.setSyncTime(LocalDateTime.now());
                employeeMapper.insert(newEmployee);
                
                // 重新查询获取完整信息（包括ID）
                employee = employeeMapper.selectOne(existingEmployeeWrapper);
            } else {
                // 本地已存在，更新员工信息和密码
                // 保存本地的mobile和dingtalk_userid字段，因为远程表没有这些字段
                String originalMobile = existingEmployee.getMobile();
                String originalDingtalkUserid = existingEmployee.getDingtalkUserid();
                BeanUtils.copyProperties(hrSync, existingEmployee);
                // 远程表is_frontline_worker是字符类型('Y'/'N')，本地表是tinyint(1/0)
                if (hrSync.getIsFrontlineWorker() != null && "Y".equalsIgnoreCase(hrSync.getIsFrontlineWorker().trim())) {
                    existingEmployee.setIsFrontlineWorker(1);
                } else {
                    existingEmployee.setIsFrontlineWorker(0);
                }
                // 恢复本地的mobile和dingtalk_userid字段，因为远程表没有这些字段，不应被覆盖
                existingEmployee.setMobile(originalMobile);
                existingEmployee.setDingtalkUserid(originalDingtalkUserid);
                existingEmployee.setSyncTime(LocalDateTime.now());
                existingEmployee.setIsActive(1); // 如果在远程库中存在，标记为在职
                employeeMapper.updateById(existingEmployee);
                
                // 重新查询获取完整信息
                employee = employeeMapper.selectById(existingEmployee.getId());
            }

            if (employee == null) {
                throw new RuntimeException("同步员工信息失败");
            }

            // 生成Token
            String token = jwtUtil.generateToken(employee.getId(), employee.getSfUserId(), "employee");

            // 创建LoginVO
            LoginVO loginVO = new LoginVO();
            loginVO.setToken(token);
            loginVO.setUserId(employee.getId());
            loginVO.setUsername(employee.getSfUserId());
            loginVO.setUserType("employee");
            loginVO.setRealName(employee.getName());
            loginVO.setEmail(employee.getEmail());

            // 记录操作日志
            saveOperationLog(employee.getId(), employee.getSfUserId(), "employee", "login", "员工登录（从远程同步）");

            return loginVO;
        }

        // 登录失败，抛出异常
        throw new RuntimeException("用户名或密码错误");
    }

    /**
     * 用户登出
     * 记录登出日志
     * 
     * @param userId 用户ID
     */
    @Override
    public void logout(Long userId) {
        // 记录操作日志
        saveOperationLog(userId, null, null, "logout", "用户登出");
    }

    /**
     * 获取当前用户信息
     * 根据用户类型返回Admin或Employee信息
     * 
     * @param userId 用户ID
     * @return 用户信息（Admin或Employee对象）
     */
    @Override
    public Object getCurrentUser(Long userId) {
        // 先查管理员表
        Admin admin = adminMapper.selectById(userId);
        if (admin != null) {
            return admin;
        }

        // 再查员工表
        Employee employee = employeeMapper.selectById(userId);
        if (employee != null) {
            return employee;
        }

        // 用户不存在
        throw new RuntimeException("用户不存在");
    }

    /**
     * 保存操作日志
     * 
     * @param userId 用户ID
     * @param userName 用户名
     * @param userType 用户类型
     * @param operation 操作类型
     * @param description 操作描述
     */
    private void saveOperationLog(Long userId, String userName, String userType, String operation, String description) {
        OperationLog log = new OperationLog();
        log.setUserId(userId);
        log.setUserName(userName);
        log.setUserType(userType);
        log.setOperation(operation);
        log.setDescription(description);
        log.setResult("success");
        log.setCreatedAt(LocalDateTime.now());
        operationLogMapper.insert(log);
    }
}

