@echo off
echo ========================================
echo 测试图片服务配置
echo ========================================

echo.
echo 1. 检查图片文件是否存在...
if exist "d:\MyApplication5\backend\src\main\resources\images\products\item001.png" (
    echo OK: item001.png 存在
) else (
    echo ERROR: item001.png 不存在
)

echo.
echo 2. 检查WebConfig文件是否存在...
if exist "d:\MyApplication5\backend\src\main\java\com\example\campustrade\config\WebConfig.java" (
    echo OK: WebConfig.java 存在
) else (
    echo ERROR: WebConfig.java 不存在
)

echo.
echo 3. 显示WebConfig内容...
type "d:\MyApplication5\backend\src\main\java\com\example\campustrade\config\WebConfig.java"

echo.
echo ========================================
echo 请重启后端服务后测试图片访问
echo ========================================
pause