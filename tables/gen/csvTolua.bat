rem del ..\data\csv\clientRecord.txt

java -Dfile.encoding=utf-8 -cp ExcelMerge.jar com.pch.office.main.MainLua ../data/csv/ ./conf/ ./config/

rd/s/q ..\..\output\Lua

xcopy conf\*.* ..\..\output\Lua\ /s
rd /s /q conf