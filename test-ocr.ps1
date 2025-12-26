# OCR功能测试脚本
# 测试PaddleOCR服务是否正常工作

Write-Host "开始测试OCR功能..." -ForegroundColor Green

# OCR服务地址
$ocrUrl = "http://10.11.100.238:8081/OCR"

# 测试图片路径
$testImage1 = "TEST1.jpg"
$testImage2 = "TEST2.jpg"

# 测试函数
function Test-Ocr {
    param(
        [string]$imagePath,
        [string]$testName
    )
    
    Write-Host "`n测试 $testName ($imagePath)..." -ForegroundColor Yellow
    
    if (-not (Test-Path $imagePath)) {
        Write-Host "错误: 图片文件不存在: $imagePath" -ForegroundColor Red
        return
    }
    
    try {
        # 创建multipart/form-data请求
        $boundary = [System.Guid]::NewGuid().ToString()
        $fileBytes = [System.IO.File]::ReadAllBytes($imagePath)
        $fileName = Split-Path $imagePath -Leaf
        
        $bodyLines = @()
        $bodyLines += "--$boundary"
        $bodyLines += "Content-Disposition: form-data; name=`"image`"; filename=`"$fileName`""
        $bodyLines += "Content-Type: image/jpeg"
        $bodyLines += ""
        $bodyLines += [System.Text.Encoding]::GetEncoding("iso-8859-1").GetString($fileBytes)
        $bodyLines += "--$boundary--"
        
        $body = $bodyLines -join "`r`n"
        $bodyBytes = [System.Text.Encoding]::GetEncoding("iso-8859-1").GetBytes($body)
        
        # 发送请求
        $headers = @{
            "Content-Type" = "multipart/form-data; boundary=$boundary"
        }
        
        Write-Host "正在调用OCR服务..." -ForegroundColor Cyan
        $response = Invoke-WebRequest -Uri $ocrUrl -Method Post -Body $bodyBytes -Headers $headers -TimeoutSec 30
        
        Write-Host "OCR响应状态码: $($response.StatusCode)" -ForegroundColor Green
        Write-Host "OCR响应内容:" -ForegroundColor Cyan
        Write-Host $response.Content
        
        # 尝试解析JSON
        try {
            $json = $response.Content | ConvertFrom-Json
            Write-Host "`nJSON解析成功:" -ForegroundColor Green
            $json | ConvertTo-Json -Depth 10
        } catch {
            Write-Host "JSON解析失败: $_" -ForegroundColor Yellow
        }
        
    } catch {
        Write-Host "错误: $_" -ForegroundColor Red
        Write-Host "详细错误信息:" -ForegroundColor Red
        Write-Host $_.Exception.Message
    }
}

# 测试第一张图片
if (Test-Path $testImage1) {
    Test-Ocr -imagePath $testImage1 -testName "TEST1"
} else {
    Write-Host "警告: 测试图片 $testImage1 不存在" -ForegroundColor Yellow
}

# 测试第二张图片
if (Test-Path $testImage2) {
    Test-Ocr -imagePath $testImage2 -testName "TEST2"
} else {
    Write-Host "警告: 测试图片 $testImage2 不存在" -ForegroundColor Yellow
}

Write-Host "`n测试完成!" -ForegroundColor Green

