#!/bin/bash
# OCR功能curl测试脚本

OCR_BASE="http://10.11.100.238:8081"
TEST_IMAGES=("TEST1.jpg" "TEST2.jpg")

echo "============================================================"
echo "OCR功能curl测试"
echo "============================================================"
echo "OCR服务地址: $OCR_BASE"
echo "测试图片: ${TEST_IMAGES[@]}"
echo ""

# 测试方式1: Base64编码方式 (JSON格式)
echo "============================================================"
echo "方式1: Base64编码方式 (POST /predict)"
echo "============================================================"

for image in "${TEST_IMAGES[@]}"; do
    if [ ! -f "$image" ]; then
        echo "[跳过] 图片不存在: $image"
        continue
    fi
    
    echo ""
    echo "测试图片: $image"
    echo "----------------------------------------"
    
    # 将图片转换为Base64
    BASE64_IMAGE=$(base64 -w 0 "$image" 2>/dev/null || base64 "$image" 2>/dev/null)
    
    if [ -z "$BASE64_IMAGE" ]; then
        echo "[错误] Base64编码失败"
        continue
    fi
    
    # 发送JSON请求
    RESPONSE=$(curl -s -w "\nHTTP_CODE:%{http_code}" \
        -X POST "$OCR_BASE/predict" \
        -H "Content-Type: application/json" \
        -d "{\"image\": \"$BASE64_IMAGE\", \"use_angle_cls\": true}")
    
    HTTP_CODE=$(echo "$RESPONSE" | grep "HTTP_CODE" | cut -d: -f2)
    BODY=$(echo "$RESPONSE" | sed '/HTTP_CODE/d')
    
    echo "状态码: $HTTP_CODE"
    if [ "$HTTP_CODE" = "200" ]; then
        echo "[成功] OCR识别成功"
        echo "响应内容:"
        echo "$BODY" | head -c 500
        echo ""
        if [ ${#BODY} -gt 500 ]; then
            echo "... (响应内容较长，已截断)"
        fi
    else
        echo "[失败] OCR识别失败"
        echo "错误信息: $BODY"
    fi
done

echo ""
echo "============================================================"
echo "方式2: Multipart方式 (POST /ocr)"
echo "============================================================"

for image in "${TEST_IMAGES[@]}"; do
    if [ ! -f "$image" ]; then
        echo "[跳过] 图片不存在: $image"
        continue
    fi
    
    echo ""
    echo "测试图片: $image"
    echo "----------------------------------------"
    
    # 发送multipart请求
    RESPONSE=$(curl -s -w "\nHTTP_CODE:%{http_code}" \
        -X POST "$OCR_BASE/ocr" \
        -F "file=@$image" \
        -F "use_angle_cls=true")
    
    HTTP_CODE=$(echo "$RESPONSE" | grep "HTTP_CODE" | cut -d: -f2)
    BODY=$(echo "$RESPONSE" | sed '/HTTP_CODE/d')
    
    echo "状态码: $HTTP_CODE"
    if [ "$HTTP_CODE" = "200" ]; then
        echo "[成功] OCR识别成功"
        echo "响应内容:"
        echo "$BODY" | head -c 500
        echo ""
        if [ ${#BODY} -gt 500 ]; then
            echo "... (响应内容较长，已截断)"
        fi
    else
        echo "[失败] OCR识别失败"
        echo "错误信息: $BODY"
    fi
done

echo ""
echo "============================================================"
echo "测试完成"
echo "============================================================"

