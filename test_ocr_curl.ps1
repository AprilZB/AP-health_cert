# OCR功能curl测试脚本 (PowerShell版本)

$OCR_BASE = "http://10.11.100.238:8081"
$TEST_IMAGES = @("TEST1.jpg", "TEST2.jpg")

Write-Host "============================================================" -ForegroundColor Cyan
Write-Host "OCR功能curl测试" -ForegroundColor Cyan
Write-Host "============================================================" -ForegroundColor Cyan
Write-Host "OCR服务地址: $OCR_BASE"
Write-Host "测试图片: $($TEST_IMAGES -join ', ')"
Write-Host ""

# 测试方式1: Base64编码方式 (JSON格式)
Write-Host "============================================================" -ForegroundColor Yellow
Write-Host "方式1: Base64编码方式 (POST /predict)" -ForegroundColor Yellow
Write-Host "============================================================" -ForegroundColor Yellow

foreach ($image in $TEST_IMAGES) {
    if (-not (Test-Path $image)) {
        Write-Host "[跳过] 图片不存在: $image" -ForegroundColor Yellow
        continue
    }
    
    Write-Host ""
    Write-Host "测试图片: $image" -ForegroundColor Green
    Write-Host "----------------------------------------"
    
    try {
        # 读取图片并转换为Base64
        $imageBytes = [System.IO.File]::ReadAllBytes($image)
        $base64Image = [System.Convert]::ToBase64String($imageBytes)
        
        # 构建JSON请求体
        $jsonBody = @{
            image = $base64Image
            use_angle_cls = $true
        } | ConvertTo-Json -Compress
        
        Write-Host "正在发送请求..." -ForegroundColor Cyan
        
        # 发送POST请求
        $response = Invoke-WebRequest -Uri "$OCR_BASE/predict" `
            -Method POST `
            -ContentType "application/json" `
            -Body $jsonBody `
            -TimeoutSec 30 `
            -ErrorAction Stop
        
        Write-Host "状态码: $($response.StatusCode)" -ForegroundColor Green
        if ($response.StatusCode -eq 200) {
            Write-Host "[成功] OCR识别成功" -ForegroundColor Green
            Write-Host "响应内容:" -ForegroundColor Cyan
            $responseText = $response.Content
            if ($responseText.Length -gt 500) {
                Write-Host $responseText.Substring(0, 500) -ForegroundColor White
                Write-Host "... (响应内容较长，已截断)" -ForegroundColor Gray
            } else {
                Write-Host $responseText -ForegroundColor White
            }
        }
    } catch {
        Write-Host "[失败] OCR识别失败" -ForegroundColor Red
        Write-Host "错误信息: $($_.Exception.Message)" -ForegroundColor Red
        if ($_.Exception.Response) {
            $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
            $responseBody = $reader.ReadToEnd()
            Write-Host "响应内容: $responseBody" -ForegroundColor Red
        }
    }
}

Write-Host ""
Write-Host "============================================================" -ForegroundColor Yellow
Write-Host "方式2: Multipart方式 (POST /ocr)" -ForegroundColor Yellow
Write-Host "============================================================" -ForegroundColor Yellow

foreach ($image in $TEST_IMAGES) {
    if (-not (Test-Path $image)) {
        Write-Host "[跳过] 图片不存在: $image" -ForegroundColor Yellow
        continue
    }
    
    Write-Host ""
    Write-Host "测试图片: $image" -ForegroundColor Green
    Write-Host "----------------------------------------"
    
    try {
        Write-Host "正在发送请求..." -ForegroundColor Cyan
        
        # 创建multipart/form-data请求
        $boundary = [System.Guid]::NewGuid().ToString()
        $fileBytes = [System.IO.File]::ReadAllBytes($image)
        $fileName = Split-Path $image -Leaf
        
        $bodyLines = @()
        $bodyLines += "--$boundary"
        $bodyLines += "Content-Disposition: form-data; name=`"file`"; filename=`"$fileName`""
        $bodyLines += "Content-Type: image/jpeg"
        $bodyLines += ""
        $bodyLines += [System.Text.Encoding]::GetEncoding("iso-8859-1").GetString($fileBytes)
        $bodyLines += "--$boundary"
        $bodyLines += "Content-Disposition: form-data; name=`"use_angle_cls`""
        $bodyLines += ""
        $bodyLines += "true"
        $bodyLines += "--$boundary--"
        
        $body = $bodyLines -join "`r`n"
        $bodyBytes = [System.Text.Encoding]::GetEncoding("iso-8859-1").GetBytes($body)
        
        $headers = @{
            "Content-Type" = "multipart/form-data; boundary=$boundary"
        }
        
        # 发送POST请求
        $response = Invoke-WebRequest -Uri "$OCR_BASE/ocr" `
            -Method POST `
            -Body $bodyBytes `
            -Headers $headers `
            -TimeoutSec 30 `
            -ErrorAction Stop
        
        Write-Host "状态码: $($response.StatusCode)" -ForegroundColor Green
        if ($response.StatusCode -eq 200) {
            Write-Host "[成功] OCR识别成功" -ForegroundColor Green
            Write-Host "响应内容:" -ForegroundColor Cyan
            $responseText = $response.Content
            if ($responseText.Length -gt 500) {
                Write-Host $responseText.Substring(0, 500) -ForegroundColor White
                Write-Host "... (响应内容较长，已截断)" -ForegroundColor Gray
            } else {
                Write-Host $responseText -ForegroundColor White
            }
        }
    } catch {
        Write-Host "[失败] OCR识别失败" -ForegroundColor Red
        Write-Host "错误信息: $($_.Exception.Message)" -ForegroundColor Red
        if ($_.Exception.Response) {
            $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
            $responseBody = $reader.ReadToEnd()
            Write-Host "响应内容: $responseBody" -ForegroundColor Red
        }
    }
}

Write-Host ""
Write-Host "============================================================" -ForegroundColor Cyan
Write-Host "测试完成" -ForegroundColor Cyan
Write-Host "============================================================" -ForegroundColor Cyan

