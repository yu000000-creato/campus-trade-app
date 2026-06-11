@echo off
echo ========================================
echo 测试所有商品图片
echo ========================================

setlocal enabledelayedexpansion

set "basePath=d:\MyApplication5\backend\src\main\resources\images\products"
set "failedCount=0"
set "successCount=0"

echo 正在检查图片文件...
echo.

for /L %%i in (1,1,54) do (
    set "fileNum=00%%i"
    set "fileNum=!fileNum:~-3!"
    set "fileName=item!fileNum!.png"
    
    if exist "%basePath%\!fileName!" (
        for %%f in ("%basePath%\!fileName!") do set "fileSize=%%~zf"
        if !fileSize! gtr 0 (
            echo [OK] !fileName! - 大小: !fileSize! 字节
            set /a successCount+=1
        ) else (
            echo [FAIL] !fileName! - 文件为空
            set /a failedCount+=1
        )
    ) else (
        echo [FAIL] !fileName! - 文件不存在
        set /a failedCount+=1
    )
)

echo.
echo ========================================
echo 测试结果:
echo 成功: %successCount% 个文件
echo 失败: %failedCount% 个文件
echo ========================================

if %failedCount% gtr 0 (
    echo.
    echo 请检查失败的图片文件，可能需要重新上传
) else (
    echo.
    echo 所有图片文件正常！问题可能是Glide缓存
)

pause