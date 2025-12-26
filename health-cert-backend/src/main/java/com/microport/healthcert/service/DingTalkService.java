package com.microport.healthcert.service;

/**
 * 钉钉服务接口
 * 提供钉钉API调用功能
 * 
 * @author system
 * @date 2024
 */
public interface DingTalkService {

    /**
     * 获取access_token
     * 从system_configs读取dingtalk.corp_id和dingtalk.app_secret，调用钉钉API获取token，缓存token（2小时）
     * 
     * @return access_token
     */
    String getAccessToken();

    /**
     * 根据手机号获取userid
     * 
     * @param mobile 手机号
     * @return 钉钉userid
     */
    String getUserIdByMobile(String mobile);

    /**
     * 发送工作通知
     * markdown格式，内容包含健康证信息
     * 
     * @param userId 钉钉用户ID
     * @param content 消息内容（markdown格式）
     */
    void sendWorkMessage(String userId, String content);
}

