#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""
测试不同的OCR API路径
"""

import requests
import sys
import io

if sys.platform == 'win32':
    sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8')

OCR_BASE = "http://10.11.100.238:8081"
TEST_PATHS = [
    "/OCR",
    "/predict",
    "/ocr",
    "/api/ocr",
    "/v1/ocr",
    "/ocr/predict",
    "/api/predict",
]

def test_path(path):
    """测试指定路径"""
    url = OCR_BASE + path
    print(f"\n测试路径: {path}")
    print(f"完整URL: {url}")
    
    try:
        with open('TEST1.jpg', 'rb') as f:
            files = {'image': ('TEST1.jpg', f, 'image/jpeg')}
            response = requests.post(url, files=files, timeout=10)
            
            print(f"  状态码: {response.status_code}")
            if response.status_code == 200:
                print(f"  [成功] 响应: {response.text[:200]}")
                return True
            else:
                print(f"  [失败] 响应: {response.text[:200]}")
                return False
    except Exception as e:
        print(f"  [异常] {e}")
        return False

print("="*60)
print("测试OCR API路径")
print("="*60)

for path in TEST_PATHS:
    if test_path(path):
        print(f"\n找到正确的路径: {path}")
        break

