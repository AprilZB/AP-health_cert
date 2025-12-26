#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""
测试不同的OCR请求格式
"""

import requests
import json
import sys
import io

if sys.platform == 'win32':
    sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8')

OCR_URL = "http://10.11.100.238:8081/ocr"

def test_format1():
    """格式1: image字段"""
    print("\n[格式1] 使用image字段")
    try:
        with open('TEST1.jpg', 'rb') as f:
            files = {'image': ('TEST1.jpg', f, 'image/jpeg')}
            response = requests.post(OCR_URL, files=files, timeout=10)
            print(f"  状态码: {response.status_code}")
            print(f"  响应: {response.text[:300]}")
            if response.status_code == 200:
                return response.json()
    except Exception as e:
        print(f"  异常: {e}")
    return None

def test_format2():
    """格式2: file字段"""
    print("\n[格式2] 使用file字段")
    try:
        with open('TEST1.jpg', 'rb') as f:
            files = {'file': ('TEST1.jpg', f, 'image/jpeg')}
            response = requests.post(OCR_URL, files=files, timeout=10)
            print(f"  状态码: {response.status_code}")
            print(f"  响应: {response.text[:300]}")
            if response.status_code == 200:
                return response.json()
    except Exception as e:
        print(f"  异常: {e}")
    return None

def test_format3():
    """格式3: img字段"""
    print("\n[格式3] 使用img字段")
    try:
        with open('TEST1.jpg', 'rb') as f:
            files = {'img': ('TEST1.jpg', f, 'image/jpeg')}
            response = requests.post(OCR_URL, files=files, timeout=10)
            print(f"  状态码: {response.status_code}")
            print(f"  响应: {response.text[:300]}")
            if response.status_code == 200:
                return response.json()
    except Exception as e:
        print(f"  异常: {e}")
    return None

def test_format4():
    """格式4: 直接发送二进制数据"""
    print("\n[格式4] 直接发送二进制数据")
    try:
        with open('TEST1.jpg', 'rb') as f:
            data = f.read()
            headers = {'Content-Type': 'image/jpeg'}
            response = requests.post(OCR_URL, data=data, headers=headers, timeout=10)
            print(f"  状态码: {response.status_code}")
            print(f"  响应: {response.text[:300]}")
            if response.status_code == 200:
                return response.json()
    except Exception as e:
        print(f"  异常: {e}")
    return None

def test_format5():
    """格式5: 使用base64编码"""
    print("\n[格式5] 使用base64编码")
    try:
        import base64
        with open('TEST1.jpg', 'rb') as f:
            img_base64 = base64.b64encode(f.read()).decode('utf-8')
            data = {'image': img_base64}
            response = requests.post(OCR_URL, json=data, timeout=10)
            print(f"  状态码: {response.status_code}")
            print(f"  响应: {response.text[:300]}")
            if response.status_code == 200:
                return response.json()
    except Exception as e:
        print(f"  异常: {e}")
    return None

print("="*60)
print("测试OCR请求格式")
print("="*60)

formats = [
    ("image字段", test_format1),
    ("file字段", test_format2),
    ("img字段", test_format3),
    ("二进制数据", test_format4),
    ("base64编码", test_format5),
]

for name, test_func in formats:
    result = test_func()
    if result:
        print(f"\n[成功] 找到正确的格式: {name}")
        print(f"[结果] {json.dumps(result, ensure_ascii=False, indent=2)[:500]}")
        break

