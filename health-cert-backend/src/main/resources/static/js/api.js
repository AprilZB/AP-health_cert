/**
 * API请求封装模块
 * 提供统一的API调用接口，自动处理JWT token、错误处理和401跳转
 */

// API基础地址
const API_BASE_URL = '/api';

/**
 * 封装fetch请求
 * @param {string} url - 请求URL（相对路径，会自动拼接API_BASE_URL）
 * @param {Object} options - fetch选项（method, body, headers等）
 * @returns {Promise<Object>} 返回解析后的JSON数据
 */
async function apiRequest(url, options = {}) {
    // 确保URL以/开头
    if (!url.startsWith('/')) {
        url = '/' + url;
    }
    
    // 完整URL
    const fullUrl = API_BASE_URL + url;
    
    // 默认请求配置
    const defaultOptions = {
        method: 'GET',
        headers: {
            'Content-Type': 'application/json',
        },
    };
    
    // 合并用户自定义headers
    const headers = {
        ...defaultOptions.headers,
        ...(options.headers || {}),
    };
    
    // 自动添加JWT token（排除登录接口）
    if (!url.includes('/auth/login')) {
        const token = localStorage.getItem('token');
        if (token) {
            headers['Authorization'] = 'Bearer ' + token;
        }
    }
    
    // 合并配置
    const finalOptions = {
        ...defaultOptions,
        ...options,
        headers: headers,
    };
    
    // 如果是FormData，删除Content-Type让浏览器自动设置
    if (options.body instanceof FormData) {
        delete finalOptions.headers['Content-Type'];
    }
    
    try {
        // 发送请求
        const response = await fetch(fullUrl, finalOptions);
        
        // 处理401未授权错误
        if (response.status === 401) {
            // 清除本地存储的token和用户信息
            localStorage.removeItem('token');
            localStorage.removeItem('userId');
            localStorage.removeItem('username');
            localStorage.removeItem('userType');
            
            // 跳转到登录页
            window.location.href = '/login.html';
            
            // 抛出错误，阻止后续处理
            throw new Error('未授权，请重新登录');
        }
        
        // 解析响应数据
        let data;
        const contentType = response.headers.get('content-type');
        
        if (contentType && contentType.includes('application/json')) {
            data = await response.json();
        } else {
            // 非JSON响应（如文件下载）
            data = {
                code: response.ok ? 200 : response.status,
                message: response.ok ? '成功' : '请求失败',
                data: await response.blob(),
            };
        }
        
        // 统一错误处理
        if (!response.ok || (data.code && data.code !== 200)) {
            const errorMessage = data.message || `请求失败: ${response.status} ${response.statusText}`;
            throw new Error(errorMessage);
        }
        
        // 返回数据
        return data;
        
    } catch (error) {
        // 网络错误或其他异常
        if (error.name === 'TypeError' && error.message.includes('fetch')) {
            throw new Error('网络连接失败，请检查网络设置');
        }
        
        // 重新抛出错误，让调用者处理
        throw error;
    }
}

/**
 * GET请求
 * @param {string} url - 请求URL
 * @param {Object} params - URL参数对象（可选）
 * @returns {Promise<Object>} 返回解析后的JSON数据
 */
async function apiGet(url, params = {}) {
    // 构建查询字符串
    if (Object.keys(params).length > 0) {
        const queryString = new URLSearchParams(params).toString();
        url += (url.includes('?') ? '&' : '?') + queryString;
    }
    
    return apiRequest(url, {
        method: 'GET',
    });
}

/**
 * POST请求
 * @param {string} url - 请求URL
 * @param {Object} body - 请求体数据
 * @returns {Promise<Object>} 返回解析后的JSON数据
 */
async function apiPost(url, body = {}) {
    return apiRequest(url, {
        method: 'POST',
        body: JSON.stringify(body),
    });
}

/**
 * PUT请求
 * @param {string} url - 请求URL
 * @param {Object} body - 请求体数据
 * @returns {Promise<Object>} 返回解析后的JSON数据
 */
async function apiPut(url, body = {}) {
    return apiRequest(url, {
        method: 'PUT',
        body: JSON.stringify(body),
    });
}

/**
 * DELETE请求
 * @param {string} url - 请求URL
 * @returns {Promise<Object>} 返回解析后的JSON数据
 */
async function apiDelete(url) {
    return apiRequest(url, {
        method: 'DELETE',
    });
}

/**
 * 文件上传请求
 * @param {string} url - 请求URL
 * @param {FormData} formData - FormData对象
 * @returns {Promise<Object>} 返回解析后的JSON数据
 */
async function apiUpload(url, formData) {
    return apiRequest(url, {
        method: 'POST',
        body: formData,
    });
}

