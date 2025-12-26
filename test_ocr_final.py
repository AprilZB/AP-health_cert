#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""
最终OCR测试脚本 - 使用file字段
"""

import requests
import json
import sys
import io

if sys.platform == 'win32':
    sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8')

OCR_URL = "http://10.11.100.238:8081/ocr"
TEST_IMAGES = ["TEST1.jpg", "TEST2.jpg"]

def test_ocr(image_path):
    """测试OCR识别功能"""
    print(f"\n{'='*60}")
    print(f"测试图片: {image_path}")
    print(f"{'='*60}")
    
    try:
        with open(image_path, 'rb') as f:
            # 使用file字段（根据错误信息，API需要file字段）
            files = {'file': (image_path, f, 'image/jpeg')}
            
            print(f"[发送] URL: {OCR_URL}")
            print(f"[文件] 图片大小: {os.path.getsize(image_path)} bytes")
            
            response = requests.post(OCR_URL, files=files, timeout=30)
            
            print(f"\n[响应] 状态码: {response.status_code}")
            print(f"[响应] Content-Type: {response.headers.get('Content-Type', 'N/A')}")
            
            if response.status_code == 200:
                print("[成功] OCR请求成功!")
                
                try:
                    result = response.json()
                    print(f"\n[JSON] OCR响应:")
                    print(json.dumps(result, ensure_ascii=False, indent=2))
                    
                    # 检查响应结构
                    if isinstance(result, dict):
                        print(f"\n[字段] 响应字段: {list(result.keys())}")
                        
                        # 检查是否有rec_texts字段
                        if 'rec_texts' in result:
                            rec_texts = result['rec_texts']
                            print(f"[文本] 识别文本数量: {len(rec_texts) if isinstance(rec_texts, list) else 'N/A'}")
                            
                            if isinstance(rec_texts, list):
                                all_text = ' '.join([str(text) for text in rec_texts])
                                print(f"\n[文本] 合并后的识别文本:")
                                print(all_text)
                        else:
                            # 检查其他可能的字段
                            for key in ['text', 'result', 'data', 'ocr_result']:
                                if key in result:
                                    print(f"[字段] 找到字段: {key}")
                                    print(f"[内容] {result[key]}")
                    
                    return result
                    
                except json.JSONDecodeError as e:
                    print(f"[错误] JSON解析失败: {e}")
                    print(f"[原始] 响应内容: {response.text}")
                    return None
            else:
                print(f"[失败] OCR请求失败!")
                print(f"[错误] {response.text}")
                return None
                
    except Exception as e:
        print(f"[错误] 异常: {e}")
        import traceback
        traceback.print_exc()
        return None

import os

print("="*60)
print("OCR功能测试 - 使用file字段")
print("="*60)

for image_path in TEST_IMAGES:
    if os.path.exists(image_path):
        test_ocr(image_path)
    else:
        print(f"\n[警告] 图片不存在: {image_path}")

