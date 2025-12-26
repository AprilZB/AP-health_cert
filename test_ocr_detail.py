#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""
详细查看PaddleOCR响应结构
"""

import base64
import requests
import json
import sys
import io

if sys.platform == 'win32':
    sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8')

API_URL = "http://10.11.100.238:8081/ocr"
file_path = "TEST2.jpg"

print("="*60)
print("PaddleOCR详细响应结构分析")
print("="*60)

try:
    with open(file_path, "rb") as file:
        file_bytes = file.read()
        file_data = base64.b64encode(file_bytes).decode("utf-8")
    
    payload = {"file": file_data, "fileType": 1}
    response = requests.post(API_URL, json=payload, timeout=60)
    
    if response.status_code == 200:
        result_data = response.json()
        
        print("\n[完整响应结构]")
        print(json.dumps(result_data, ensure_ascii=False, indent=2))
        
        if "result" in result_data:
            result = result_data["result"]
            
            if "ocrResults" in result:
                ocr_results = result["ocrResults"]
                print(f"\n[OCR结果数量] {len(ocr_results)}")
                
                for i, res in enumerate(ocr_results):
                    print(f"\n{'='*60}")
                    print(f"结果 {i+1} 的完整结构:")
                    print(f"{'='*60}")
                    print(json.dumps(res, ensure_ascii=False, indent=2))
                    
                    if "prunedResult" in res:
                        pruned = res["prunedResult"]
                        print(f"\n[prunedResult类型] {type(pruned)}")
                        
                        # 查找文本内容
                        if isinstance(pruned, dict):
                            print(f"[prunedResult字段] {list(pruned.keys())}")
                            
                            # 常见的文本字段
                            text_fields = ["text", "content", "rec_text", "rec_texts", "texts", "words", "lines"]
                            for field in text_fields:
                                if field in pruned:
                                    print(f"\n[找到文本字段] {field}:")
                                    print(pruned[field])
                            
                            # 如果有嵌套结构，递归查找
                            def find_text_in_dict(d, path=""):
                                texts = []
                                if isinstance(d, dict):
                                    for k, v in d.items():
                                        if k in ["text", "content", "rec_text", "word"] and isinstance(v, str):
                                            texts.append((f"{path}.{k}", v))
                                        elif isinstance(v, (dict, list)):
                                            texts.extend(find_text_in_dict(v, f"{path}.{k}"))
                                elif isinstance(d, list):
                                    for idx, item in enumerate(d):
                                        texts.extend(find_text_in_dict(item, f"{path}[{idx}]"))
                                return texts
                            
                            texts = find_text_in_dict(pruned)
                            if texts:
                                print(f"\n[提取到的所有文本]")
                                for path, text in texts:
                                    print(f"  {path}: {text}")
    else:
        print(f"[失败] 状态码: {response.status_code}")
        print(f"[错误] {response.text}")
        
except Exception as e:
    print(f"[错误] {e}")
    import traceback
    traceback.print_exc()

