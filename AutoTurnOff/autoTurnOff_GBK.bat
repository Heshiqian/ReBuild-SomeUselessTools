@echo off

:loop
cls
echo 1�������Զ��ػ��ű�
echo 2���رսű�
set /p ARG=����1��2����ִ������:

if /i '%ARG%'=='1' goto 1
if /i '%ARG%'=='2' goto 2
echo ������1��2
goto loop

:1
echo "д���ػ��ű��С�����"
set PATHR=%UserProfile%\shutdown.obj
echo shutdown /s /f /t 0 >%PATHR%
copy %PATHR% %UserProfile%\shutdownHook.bat
echo "д�����"
set /p TIME_HOUR=�������ڼ�����йػ���24Сʱ�ƣ�������10��������22���س�ȷ������
echo ���趨��%TIME_HOUR%:00
set TIME=%TIME_HOUR%:00
schtasks /Create /TN "ÿ��ҹ���Զ��ػ�" /RU "SYSTEM" /SC DAILY /TR %UserProfile%\shutdownHook.bat /ST %TIME% /RL HIGHEST
goto end

:2
schtasks /Delete /TN "ÿ��ҹ���Զ��ػ�" /F
del /f %UserProfile%\shutdown.obj
del /f %UserProfile%\shutdownHook.bat

:end
pause