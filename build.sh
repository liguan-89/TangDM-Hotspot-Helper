#!/bin/bash
# 比亚迪唐DM热点自启动应用构建脚本

echo "🚗 比亚迪唐DM热点自启动应用构建工具"
echo "========================================"

# 检查是否在项目根目录
if [ ! -f "build.gradle" ]; then
    echo "❌ 错误：请在项目根目录运行此脚本"
    exit 1
fi

# 清理旧构建
echo "🧹 清理旧构建文件..."
./gradlew clean

if [ $? -ne 0 ]; then
    echo "❌ 清理失败"
    exit 1
fi

# 构建APK
echo "🔨 构建APK..."
./gradlew assembleDebug

if [ $? -ne 0 ]; then
    echo "❌ 构建失败"
    exit 1
fi

# 检查构建结果
APK_PATH="app/build/outputs/apk/debug/app-debug.apk"
if [ -f "$APK_PATH" ]; then
    APK_SIZE=$(du -h "$APK_PATH" | cut -f1)
    echo ""
    echo "✅ 构建成功！"
    echo "📦 APK信息："
    echo "   文件: $APK_PATH"
    echo "   大小: $APK_SIZE"
    echo ""
    echo "📱 安装到车机："
    echo "   方法1: adb install $APK_PATH"
    echo "   方法2: 复制到U盘，用车机文件管理器安装"
    echo ""
    echo "🔧 配置步骤："
    echo "   1. 安装APK"
    echo "   2. 打开'唐DM热点'应用"
    echo "   3. 点击'开启无障碍权限'"
    echo "   4. 在系统设置中开启无障碍服务"
    echo "   5. 返回应用测试热点功能"
    echo "   6. 重启车机测试自动开启"
    echo ""
    echo "📋 注意事项："
    echo "   - 确保车机系统为Android 7.0"
    echo "   - 需要开启无障碍权限"
    echo "   - 首次需要手动测试一次"
    echo "   - 查看日志: adb logcat | grep TangDMHotspot"
else
    echo "❌ 构建失败：APK文件未生成"
    exit 1
fi