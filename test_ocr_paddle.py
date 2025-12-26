#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""
按照PaddleOCR实际API格式进行测试
"""

import base64
import requests
import json
import sys
import io

if sys.platform == 'win32':
    sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8')

API_URL = "http://10.11.100.238:8081/ocr"
TEST_IMAGES = ["TEST1.jpg", "TEST2.jpg"]

print("="*60)
print("PaddleOCR API测试")
print("="*60)
print(f"API地址: {API_URL}")
print(f"测试图片: {', '.join(TEST_IMAGES)}")
print()

for file_path in TEST_IMAGES:
    print("="*60)
    print(f"测试图片: {file_path}")
    print("="*60)
    
    try:
        # 读取图片文件
        with open(file_path, "rb") as file:
            file_bytes = file.read()
            file_data = base64.b64encode(file_bytes).decode("utf-8")
        
        print(f"[文件] 图片大小: {len(file_bytes)} bytes")
        print(f"[编码] Base64长度: {len(file_data)} 字符")
        
        # 构建请求载荷
        payload = {"file": file_data, "fileType": 1}
        
        print(f"[请求] 发送POST请求到: {API_URL}")
        print(f"[参数] fileType: 1")
        
        # 发送请求
        response = requests.post(API_URL, json=payload, timeout=30)
        
        print(f"[响应] 状态码: {response.status_code}")
        print(f"[响应] Content-Type: {response.headers.get('Content-Type', 'N/A')}")
        
        if response.status_code == 200:
            print("[成功] OCR识别成功!")
            
            try:
                result_data = response.json()
                
                # 检查响应结构
                print(f"\n[结构] 响应字段: {list(result_data.keys())}")
                
                if "result" in result_data:
                    result = result_data["result"]
                    print(f"[结果] result字段类型: {type(result)}")
                    
                    if isinstance(result, dict):
                        print(f"[结果] result字段内容: {list(result.keys())}")
                        
                        if "ocrResults" in result:
                            ocr_results = result["ocrResults"]
                            print(f"[识别] OCR结果数量: {len(ocr_results)}")
                            
                            # 提取所有识别文本
                            all_texts = []
                            for i, res in enumerate(ocr_results):
                                print(f"\n--- 结果 {i+1} ---")
                                
                                if "prunedResult" in res:
                                    pruned_result = res["prunedResult"]
                                    json_str = json.dumps(pruned_result, ensure_ascii=False)
                                    print(f"[文本] prunedResult: {json_str[:200]}...")
                                    all_texts.append(pruned_result)
                                
                                if "ocrImage" in res:
                                    print(f"[图片] ocrImage字段存在，长度: {len(res['ocrImage'])} 字符")
                            
                            # 合并所有文本
                            merged_text = " ".join([str(text) for text in all_texts])
                            print(f"\n[合并] 所有识别文本:")
                            print(merged_text[:500] + "..." if len(merged_text) > 500 else merged_text)
                        else:
                            print("[警告] result中没有ocrResults字段")
                            print(f"[调试] result内容: {json.dumps(result, ensure_ascii=False, indent=2)[:500]}")
                    else:
                        print(f"[警告] result不是字典类型: {type(result)}")
                        print(f"[调试] result内容: {result}")
                else:
                    print("[警告] 响应中没有result字段")
                    print(f"[调试] 完整响应: {json.dumps(result_data, ensure_ascii=False, indent=2)[:1000]}")
                    
            except json.JSONDecodeError as e:
                print(f"[错误] JSON解析失败: {e}")
                print(f"[原始] 响应内容: {response.text[:500]}")
        else:
            print(f"[失败] OCR识别失败!")
            print(f"[错误] 响应内容: {response.text}")
            
    except FileNotFoundError:
        print(f"[错误] 图片文件不存在: {file_path}")
    except Exception as e:
        print(f"[错误] 异常: {e}")
        import traceback
        traceback.print_exc()
    
    print()

print("="*60)
print("测试完成")
print("="*60)

