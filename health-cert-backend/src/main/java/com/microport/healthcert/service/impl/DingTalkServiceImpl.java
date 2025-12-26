package com.microport.healthcert.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microport.healthcert.entity.SystemConfig;
import com.microport.healthcert.mapper.SystemConfigMapper;
import com.microport.healthcert.service.DingTalkService;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 钉钉服务实现类
 * 
 * @author system
 * @date 2024
 */
@Slf4j
@Service
public class DingTalkServiceImpl implements DingTalkService {

    /**
     * 钉钉API地址 - 获取access_token
     */
    private static final String GET_TOKEN_URL = "https://oapi.dingtalk.com/gettoken";

    /**
     * 钉钉API地址 - 根据手机号获取userid
     */
    private static final String GET_USERID_BY_MOBILE_URL = "https://oapi.dingtalk.com/topapi/v2/user/getbymobile";

    /**
     * 钉钉API地址 - 发送工作通知
     */
    private static final String SEND_WORK_MESSAGE_URL = "https://oapi.dingtalk.com/topapi/message/corpconversation/asyncsend_v2";

    /**
     * Token缓存（key: "token", value: token字符串）
     */
    private static final Map<String, String> TOKEN_CACHE = new ConcurrentHashMap<>();

    /**
     * Token过期时间缓存（key: "token_expires_at", value: 过期时间戳）
     */
    private static final Map<String, Long> TOKEN_EXPIRES_CACHE = new ConcurrentHashMap<>();

    /**
     * Token缓存时间（毫秒），2小时
     */
    private static final long TOKEN_CACHE_DURATION = 2 * 60 * 60 * 1000L;

    @Autowired
    private SystemConfigMapper systemConfigMapper;

    /**
     * 获取access_token
     * 从system_configs读取dingtalk.corp_id和dingtalk.app_secret，调用钉钉API获取token，缓存token（2小时）
     * 
     * @return access_token
     */
    @Override
    public String getAccessToken() {
        try {
            // 检查缓存中是否有未过期的token
            String cachedToken = TOKEN_CACHE.get("token");
            Long expiresAt = TOKEN_EXPIRES_CACHE.get("token_expires_at");
            if (cachedToken != null && expiresAt != null && System.currentTimeMillis() < expiresAt) {
                return cachedToken;
            }

            // 从system_configs读取配置
            String corpId = getConfigValue("dingtalk.corp_id");
            String appSecret = getConfigValue("dingtalk.app_secret");

            if (corpId == null || appSecret == null || corpId.trim().isEmpty() || appSecret.trim().isEmpty()) {
                log.warn("钉钉配置不完整，无法获取access_token");
                return null;
            }

            // 调用钉钉API获取token（使用corpid和corpsecret参数）
            OkHttpClient client = new OkHttpClient();
            String url = GET_TOKEN_URL + "?corpid=" + corpId + "&corpsecret=" + appSecret;

            Request request = new Request.Builder()
                    .url(url)
                    .get()
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    log.error("获取钉钉access_token失败，HTTP状态码：{}", response.code());
                    return null;
                }

                String responseBody = response.body() != null ? response.body().string() : "";
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode jsonNode = objectMapper.readTree(responseBody);

                // 检查errcode
                int errcode = jsonNode.get("errcode") != null ? jsonNode.get("errcode").asInt() : -1;
                if (errcode != 0) {
                    String errmsg = jsonNode.get("errmsg") != null ? jsonNode.get("errmsg").asText() : "未知错误";
                    log.error("获取钉钉access_token失败，errcode：{}，errmsg：{}", errcode, errmsg);
                    return null;
                }

                // 提取access_token
                String accessToken = jsonNode.get("access_token").asText();

                // 缓存token（2小时）
                TOKEN_CACHE.put("token", accessToken);
                TOKEN_EXPIRES_CACHE.put("token_expires_at", System.currentTimeMillis() + TOKEN_CACHE_DURATION);

                log.info("获取钉钉access_token成功");
                return accessToken;
            }

        } catch (Exception e) {
            log.error("获取钉钉access_token异常", e);
            return null;
        }
    }

    /**
     * 根据手机号获取userid
     * 
     * @param mobile 手机号
     * @return 钉钉userid
     */
    @Override
    public String getUserIdByMobile(String mobile) {
        try {
            // 获取access_token
            String accessToken = getAccessToken();
            if (accessToken == null) {
                log.warn("无法获取access_token，无法查询userid");
                return null;
            }

            // 调用钉钉API根据手机号获取userid
            OkHttpClient client = new OkHttpClient();
            String url = GET_USERID_BY_MOBILE_URL + "?access_token=" + accessToken;

            // 构建请求体
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("mobile", mobile);

            ObjectMapper objectMapper = new ObjectMapper();
            String jsonBody = objectMapper.writeValueAsString(requestBody);

            RequestBody body = RequestBody.create(jsonBody, MediaType.parse("application/json; charset=utf-8"));
            Request request = new Request.Builder()
                    .url(url)
                    .post(body)
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    log.error("根据手机号获取userid失败，HTTP状态码：{}", response.code());
                    return null;
                }

                String responseBody = response.body() != null ? response.body().string() : "";
                JsonNode jsonNode = objectMapper.readTree(responseBody);

                // 检查errcode
                int errcode = jsonNode.get("errcode") != null ? jsonNode.get("errcode").asInt() : -1;
                if (errcode != 0) {
                    String errmsg = jsonNode.get("errmsg") != null ? jsonNode.get("errmsg").asText() : "未知错误";
                    log.error("根据手机号获取userid失败，errcode：{}，errmsg：{}", errcode, errmsg);
                    return null;
                }

                // 提取userid
                JsonNode resultNode = jsonNode.get("result");
                if (resultNode != null && resultNode.get("userid") != null) {
                    return resultNode.get("userid").asText();
                }

                return null;
            }

        } catch (Exception e) {
            log.error("根据手机号获取userid异常，手机号：{}", mobile, e);
            return null;
        }
    }

    /**
     * 发送工作通知
     * markdown格式，内容包含健康证信息
     * 
     * @param userId 钉钉用户ID
     * @param content 消息内容（markdown格式）
     */
    @Override
    public void sendWorkMessage(String userId, String content) {
        try {
            // 获取access_token
            String accessToken = getAccessToken();
            if (accessToken == null) {
                log.warn("无法获取access_token，无法发送工作通知");
                return;
            }

            // 从system_configs读取app_key
            String appKey = getConfigValue("dingtalk.app_key");
            if (appKey == null || appKey.trim().isEmpty()) {
                log.warn("钉钉app_key未配置，无法发送工作通知");
                return;
            }

            // 调用钉钉API发送工作通知
            OkHttpClient client = new OkHttpClient();
            String url = SEND_WORK_MESSAGE_URL + "?access_token=" + accessToken;

            // 构建请求体
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("agent_id", appKey);
            requestBody.put("userid_list", userId);

            // 构建消息内容（markdown格式）
            Map<String, Object> msgMap = new HashMap<>();
            msgMap.put("msgtype", "markdown");
            Map<String, String> markdownMap = new HashMap<>();
            markdownMap.put("title", "健康证提醒");
            markdownMap.put("text", content);
            msgMap.put("markdown", markdownMap);
            requestBody.put("msg", msgMap);

            ObjectMapper objectMapper = new ObjectMapper();
            String jsonBody = objectMapper.writeValueAsString(requestBody);

            RequestBody body = RequestBody.create(jsonBody, MediaType.parse("application/json; charset=utf-8"));
            Request request = new Request.Builder()
                    .url(url)
                    .post(body)
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    log.error("发送钉钉工作通知失败，HTTP状态码：{}", response.code());
                    return;
                }

                String responseBody = response.body() != null ? response.body().string() : "";
                JsonNode jsonNode = objectMapper.readTree(responseBody);

                // 检查errcode
                int errcode = jsonNode.get("errcode") != null ? jsonNode.get("errcode").asInt() : -1;
                if (errcode != 0) {
                    String errmsg = jsonNode.get("errmsg") != null ? jsonNode.get("errmsg").asText() : "未知错误";
                    log.error("发送钉钉工作通知失败，errcode：{}，errmsg：{}", errcode, errmsg);
                    return;
                }

                log.info("发送钉钉工作通知成功，userid：{}", userId);
            }

        } catch (Exception e) {
            log.error("发送钉钉工作通知异常，userid：{}", userId, e);
        }
    }

    /**
     * 从system_configs表读取配置值
     * 
     * @param configKey 配置键
     * @return 配置值，如果不存在返回null
     */
    private String getConfigValue(String configKey) {
        LambdaQueryWrapper<SystemConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SystemConfig::getConfigKey, configKey);
        SystemConfig config = systemConfigMapper.selectOne(wrapper);
        return config != null ? config.getConfigValue() : null;
    }
}

