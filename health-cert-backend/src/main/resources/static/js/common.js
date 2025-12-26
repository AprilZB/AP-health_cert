/**
 * 公共工具函数模块
 * 提供通用的UI交互和工具函数
 */

/**
 * 显示加载提示
 * 创建一个全屏遮罩层，显示加载动画
 */
function showLoading() {
    // 如果已经存在loading元素，直接返回
    if (document.getElementById('globalLoading')) {
        return;
    }
    
    // 创建loading容器
    const loadingDiv = document.createElement('div');
    loadingDiv.id = 'globalLoading';
    loadingDiv.style.cssText = `
        position: fixed;
        top: 0;
        left: 0;
        width: 100%;
        height: 100%;
        background: rgba(0, 0, 0, 0.5);
        display: flex;
        justify-content: center;
        align-items: center;
        z-index: 9999;
    `;
    
    // 创建loading内容
    const loadingContent = document.createElement('div');
    loadingContent.style.cssText = `
        background: white;
        padding: 30px 40px;
        border-radius: 8px;
        display: flex;
        flex-direction: column;
        align-items: center;
        gap: 15px;
    `;
    
    // 创建旋转动画
    const spinner = document.createElement('div');
    spinner.style.cssText = `
        width: 40px;
        height: 40px;
        border: 4px solid #f3f3f3;
        border-top: 4px solid #3498db;
        border-radius: 50%;
        animation: spin 1s linear infinite;
    `;
    
    // 添加旋转动画样式
    if (!document.getElementById('loadingStyle')) {
        const style = document.createElement('style');
        style.id = 'loadingStyle';
        style.textContent = `
            @keyframes spin {
                0% { transform: rotate(0deg); }
                100% { transform: rotate(360deg); }
            }
        `;
        document.head.appendChild(style);
    }
    
    // 创建提示文字
    const text = document.createElement('div');
    text.textContent = '加载中...';
    text.style.cssText = `
        color: #333;
        font-size: 14px;
    `;
    
    // 组装元素
    loadingContent.appendChild(spinner);
    loadingContent.appendChild(text);
    loadingDiv.appendChild(loadingContent);
    
    // 添加到页面
    document.body.appendChild(loadingDiv);
}

/**
 * 隐藏加载提示
 * 移除全屏遮罩层
 */
function hideLoading() {
    const loadingDiv = document.getElementById('globalLoading');
    if (loadingDiv) {
        loadingDiv.remove();
    }
}

/**
 * 显示消息提示
 * @param {string} type - 消息类型：'success'（成功）、'error'（错误）、'warning'（警告）、'info'（信息）
 * @param {string} message - 消息内容
 * @param {number} duration - 显示时长（毫秒），默认3000，0表示不自动关闭
 */
function showMessage(type = 'info', message = '', duration = 3000) {
    // 如果已经存在消息元素，先移除
    const existingMessage = document.getElementById('globalMessage');
    if (existingMessage) {
        existingMessage.remove();
    }
    
    // 创建消息容器
    const messageDiv = document.createElement('div');
    messageDiv.id = 'globalMessage';
    
    // 根据类型设置样式
    const typeStyles = {
        success: {
            background: '#4caf50',
            color: '#fff',
            icon: '✓',
        },
        error: {
            background: '#f44336',
            color: '#fff',
            icon: '✕',
        },
        warning: {
            background: '#ff9800',
            color: '#fff',
            icon: '⚠',
        },
        info: {
            background: '#2196f3',
            color: '#fff',
            icon: 'ℹ',
        },
    };
    
    const style = typeStyles[type] || typeStyles.info;
    
    messageDiv.style.cssText = `
        position: fixed;
        top: 20px;
        left: 50%;
        transform: translateX(-50%);
        background: ${style.background};
        color: ${style.color};
        padding: 12px 24px;
        border-radius: 4px;
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
        z-index: 10000;
        display: flex;
        align-items: center;
        gap: 10px;
        font-size: 14px;
        max-width: 500px;
        word-wrap: break-word;
        animation: slideDown 0.3s ease-out;
    `;
    
    // 添加滑入动画样式
    if (!document.getElementById('messageStyle')) {
        const styleElement = document.createElement('style');
        styleElement.id = 'messageStyle';
        styleElement.textContent = `
            @keyframes slideDown {
                from {
                    opacity: 0;
                    transform: translateX(-50%) translateY(-20px);
                }
                to {
                    opacity: 1;
                    transform: translateX(-50%) translateY(0);
                }
            }
        `;
        document.head.appendChild(styleElement);
    }
    
    // 创建图标
    const icon = document.createElement('span');
    icon.textContent = style.icon;
    icon.style.cssText = `
        font-weight: bold;
        font-size: 16px;
    `;
    
    // 创建文本
    const text = document.createElement('span');
    text.textContent = message;
    
    // 组装元素
    messageDiv.appendChild(icon);
    messageDiv.appendChild(text);
    
    // 添加到页面
    document.body.appendChild(messageDiv);
    
    // 自动关闭
    if (duration > 0) {
        setTimeout(() => {
            if (messageDiv.parentNode) {
                messageDiv.style.animation = 'slideDown 0.3s ease-out reverse';
                setTimeout(() => {
                    messageDiv.remove();
                }, 300);
            }
        }, duration);
    }
    
    // 点击关闭
    messageDiv.addEventListener('click', () => {
        messageDiv.style.animation = 'slideDown 0.3s ease-out reverse';
        setTimeout(() => {
            messageDiv.remove();
        }, 300);
    });
}

/**
 * 格式化日期时间
 * @param {number|string|Date} timestamp - 时间戳（毫秒）或日期字符串或Date对象
 * @param {string} format - 格式化模式，默认 'YYYY-MM-DD HH:mm:ss'
 *                         支持：YYYY(年)、MM(月)、DD(日)、HH(时)、mm(分)、ss(秒)
 * @returns {string} 格式化后的日期字符串
 */
function formatDate(timestamp, format = 'YYYY-MM-DD HH:mm:ss') {
    // 转换为Date对象
    let date;
    if (timestamp instanceof Date) {
        date = timestamp;
    } else if (typeof timestamp === 'number') {
        // 如果是秒级时间戳，转换为毫秒
        if (timestamp < 10000000000) {
            timestamp = timestamp * 1000;
        }
        date = new Date(timestamp);
    } else if (typeof timestamp === 'string') {
        date = new Date(timestamp);
    } else {
        return '';
    }
    
    // 检查日期是否有效
    if (isNaN(date.getTime())) {
        return '';
    }
    
    // 提取日期组件
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    const hours = String(date.getHours()).padStart(2, '0');
    const minutes = String(date.getMinutes()).padStart(2, '0');
    const seconds = String(date.getSeconds()).padStart(2, '0');
    
    // 替换格式化字符串
    return format
        .replace('YYYY', year)
        .replace('MM', month)
        .replace('DD', day)
        .replace('HH', hours)
        .replace('mm', minutes)
        .replace('ss', seconds);
}

/**
 * 用户登出
 * 清除本地存储的token和用户信息，并跳转到登录页
 */
function logout() {
    // 清除本地存储
    localStorage.removeItem('token');
    localStorage.removeItem('userId');
    localStorage.removeItem('username');
    localStorage.removeItem('userType');
    
    // 跳转到登录页
    window.location.href = '/login.html';
}

