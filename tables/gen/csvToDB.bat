java -Dfile.encoding=utf-8 -cp ExcelMerge.jar com.pch.office.main.MainSQLite ../data/csv/
if %ERRORLEVEL% NEQ 0 (
    pause
    exit
)
del ..\..\output\ConfData.bytes

echo f | xcopy config.db ..\..\output\ConfData.bytes