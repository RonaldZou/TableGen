echo on

cd tables\gen
del ..\Floder\index.txt
call easyExcel.bat
del /q /s ..\data\csv\*.*
java -cp ExcelMerge.jar com.pch.office.csv.ExcelToCSV ../data/csv/ ../ a
if %ERRORLEVEL% NEQ 0 (
    pause
)

call csvToDB.bat
call csvTolua.bat

pause