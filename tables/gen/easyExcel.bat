@echo off
call java -cp ExcelMerge.jar com.pch.office.excel.easyexcel.EasyFirst
if %ERRORLEVEL% NEQ 0 (
    pause
	exit
)
call java -cp ExcelMerge.jar com.pch.office.excel.easyexcel.EasyTwo
if %ERRORLEVEL% NEQ 0 (
    pause
	exit
)
call java -cp ExcelMerge.jar com.pch.office.excel.easyexcel.EasyThree
if %ERRORLEVEL% NEQ 0 (
    pause
	exit
)