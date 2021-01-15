@echo off

:loop
cls
echo 1、开启自动关机脚本
echo 2、关闭脚本
set /p ARG=输入1或2，来执行命令:

if /i '%ARG%'=='1' goto 1
if /i '%ARG%'=='2' goto 2
echo 请输入1或2
goto loop

:1
echo "写出关机脚本中。。。"
set PATHR=%UserProfile%\shutdown.obj
echo shutdown /s /f /t 0 >%PATHR%
copy %PATHR% %UserProfile%\shutdownHook.bat
echo "写出完毕"
set /p TIME_HOUR=请输入在几点进行关机（24小时制，如晚上10点则输入22，回车确定）：
echo 已设定：%TIME_HOUR%:00
set TIME=%TIME_HOUR%:00
schtasks /Create /TN "每天夜间自动关机" /RU "SYSTEM" /SC DAILY /TR %UserProfile%\shutdownHook.bat /ST %TIME% /RL HIGHEST
goto end

:2
schtasks /Delete /TN "每天夜间自动关机" /F
del /f %UserProfile%\shutdown.obj
del /f %UserProfile%\shutdownHook.bat

:end
pause