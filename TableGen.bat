echo on

cd tables\gen
java -jar ExcelMerge.jar 1

cd ..\..\tools\CSharpPackSQLite\bin\
CSharpPack.exe ..\..\..\tables tmpl ..\..\..\output\public ..\..\..\output\client

pause