#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""
OCR功能测试脚本
测试PaddleOCR服务是否正常工作
"""

import requests
import json
import sys
import os

# 设置输出编码为UTF-8
if sys.platform == 'win32':
    import io
    sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8')

# OCR服务地址
OCR_URL = "http://10.11.100.238:8081/OCR"

# 测试图片路径
TEST_IMAGES = ["TEST1.jpg", "TEST2.jpg"]

def test_ocr(image_path):
    """测试OCR识别功能"""
    print(f"\n{'='*60}")
    print(f"测试图片: {image_path}")
    print(f"{'='*60}")
    
    if not os.path.exists(image_path):
        print(f"[错误] 图片文件不存在: {image_path}")
        return None
    
    try:
        # 读取图片文件
        with open(image_path, 'rb') as f:
            files = {'image': (os.path.basename(image_path), f, 'image/jpeg')}
            
            print(f"[发送] 正在发送请求到: {OCR_URL}")
            print(f"[文件] 图片大小: {os.path.getsize(image_path)} bytes")
            
            # 发送POST请求
            response = requests.post(OCR_URL, files=files, timeout=30)
            
            print(f"\n[响应] 状态码: {response.status_code}")
            print(f"[响应] Content-Type: {response.headers.get('Content-Type', 'N/A')}")
            
            if response.status_code == 200:
                print("[成功] OCR请求成功!")
                
                # 尝试解析JSON
                try:
                    result = response.json()
                    print(f"\n[JSON] OCR响应内容:")
                    print(json.dumps(result, ensure_ascii=False, indent=2))
                    
                    # 检查是否有rec_texts字段
                    if 'rec_texts' in result:
                        rec_texts = result['rec_texts']
                        print(f"\n[文本] 识别文本数量: {len(rec_texts) if isinstance(rec_texts, list) else 'N/A'}")
                        
                        if isinstance(rec_texts, list):
                            all_text = ' '.join([str(text) for text in rec_texts])
                            print(f"\n[文本] 合并后的识别文本:")
                            print(all_text[:500] + "..." if len(all_text) > 500 else all_text)
                    else:
                        print("[警告] 响应中没有rec_texts字段")
                        print(f"[字段] 响应字段: {list(result.keys()) if isinstance(result, dict) else 'N/A'}")
                    
                    return result
                    
                except json.JSONDecodeError as e:
                    print(f"[错误] JSON解析失败: {e}")
                    print(f"[原始] 响应内容: {response.text[:500]}")
                    return None
            else:
                print(f"[失败] OCR请求失败!")
                print(f"[错误] 错误信息: {response.text}")
                return None
                
    except requests.exceptions.RequestException as e:
        print(f"[错误] 网络请求异常: {e}")
        return None
    except Exception as e:
        print(f"[错误] 未知错误: {e}")
        import traceback
        traceback.print_exc()
        return None

def main():
    """主函数"""
    print("="*60)
    print("OCR功能测试")
    print("="*60)
    print(f"OCR服务地址: {OCR_URL}")
    print(f"测试图片: {', '.join(TEST_IMAGES)}")
    
    results = []
    
    # 测试每张图片
    for image_path in TEST_IMAGES:
        result = test_ocr(image_path)
        results.append((image_path, result))
    
    # 总结
    print(f"\n{'='*60}")
    print("测试总结")
    print(f"{'='*60}")
    
    success_count = sum(1 for _, result in results if result is not None)
    print(f"成功: {success_count}/{len(TEST_IMAGES)}")
    
    for image_path, result in results:
        status = "[成功]" if result is not None else "[失败]"
        print(f"  {image_path}: {status}")

if __name__ == "__main__":
    main()

