using System;
using System.Collections.Concurrent;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using NPOI.HSSF.UserModel;
using NPOI.SS.UserModel;
using NPOI.XSSF.UserModel;

namespace CSharpPack.codes
{
    public class PackConfig
    {
        private class CfgData
        {
            public string[] cl_list;
            public string[] type_list;
            public string[] name_list;
            public string[] msg_list;
            public bool[] arr_list;
            public List<string[]> data_list;
        }

        private class ParseException
        {
        }

        public class SqliteData
        {
            public string m_str;
            public bool m_isStr;

            public bool m_bool;
            public bool n_isBool;

            public short m_short;
            public bool m_isShort;

            public int m_int;
            public bool m_isInt;

            public byte m_byte;
            public bool m_isByte;

            public long m_long;
            public bool m_isLong;

            public double m_double;
            public bool m_isDouble;

            public float m_float;
            public bool m_isFloat;

            //             public byte[] m_byteArray;
            //             public bool m_isByteArray;
        }

        private CfgData ParseXls(ISheet sheet)
        {
            var cfgData = new CfgData();
            InitProps(sheet, cfgData);
            InitData(sheet, cfgData);
            return cfgData;
        }

        private void InitProps(ISheet worksheet, CfgData cfgData)
        {
            var columns = worksheet.GetRow(0).LastCellNum;
            var rows = worksheet.LastRowNum;

            cfgData.cl_list = new string[columns];
            cfgData.type_list = new string[columns];
            cfgData.name_list = new string[columns];
            cfgData.msg_list = new string[columns];
            cfgData.arr_list = new bool[columns];

            IRow row;
            row = worksheet.GetRow(0);
            for (var j = 0; j < columns; j++)
            {
                if (row.GetCell(j) != null)
                {
                    var tmp = row.GetCell(j).ToString();
                    cfgData.cl_list[j] = tmp.ToLower().Trim();
                }
                else
                {
                    cfgData.cl_list[j] = "";
                }
            }

            row = worksheet.GetRow(1);
            for (var j = 0; j < columns; j++)
            {
                if (row.GetCell(j) != null)
                {
                    var tmp = row.GetCell(j).ToString();
                    cfgData.type_list[j] = tmp.ToLower().Trim();
                }
                else
                {
                    cfgData.type_list[j] = "";
                }
            }

            row = worksheet.GetRow(2);
            for (var j = 0; j < columns; j++)
            {
                if (row.GetCell(j) != null)
                {
                    var tmp = row.GetCell(j).ToString();
                    cfgData.name_list[j] = tmp.Trim();
                }
                else
                {
                    cfgData.name_list[j] = "";
                }
            }

            row = worksheet.GetRow(3);
            if (row != null)
            {
                for (var j = 0; j < columns; j++)
                {
                    if (row.GetCell(j) != null)
                    {
                        var tmp = row.GetCell(j).ToString();
                        cfgData.msg_list[j] = tmp.Trim();
                    }
                    else
                    {
                        cfgData.msg_list[j] = "";
                    }
                }
            }

            for (var j = 0; j < columns; j++)
            {
                var tmp = cfgData.type_list[j];
                if (!string.IsNullOrEmpty(tmp) && tmp.Contains('[') && tmp.Contains(']'))
                {
                    cfgData.arr_list[j] = true;
                }
                else
                {
                    cfgData.arr_list[j] = false;
                }
            }
        }

        private void InitData(ISheet sheet, CfgData cfgData)
        {
            var dataList = DataExtraction(sheet);
            cfgData.data_list = dataList;
        }

        private void AppendData(ISheet sheet, CfgData cfgData)
        {
            var dataList = DataExtraction(sheet);
            cfgData.data_list.AddRange(dataList);
        }

        private static List<string[]> DataExtraction(ISheet sheet)
        {
            var dataList = new List<string[]>();
            var firstRow = sheet.GetRow(0);
            var rowMax = 0;
            var blank = 0;
            for (var i = 0; i <= firstRow.LastCellNum; i++)
            {
                rowMax = i;
                if (firstRow.GetCell(i) == null || firstRow.GetCell(i).StringCellValue == string.Empty)
                {
                    blank++;
                }
                else
                {
                    blank = 0;
                }

                if (blank > 8)
                {
                    break;
                }
            }

            for (var i = 4; i <= sheet.LastRowNum; i++)
            {
                var row = sheet.GetRow(i);
                if (row == null)
                {
                    continue;
                }

                if (row.GetCell(0) == null)
                {
                    continue;
                }

                var itemList = new string[sheet.GetRow(0).LastCellNum];
                var sn = row.GetCell(0).ToString();
                if (string.IsNullOrEmpty(sn))
                {
                    continue;
                }

                if (sn.StartsWith("#"))
                {
                    continue;
                }

                for (var j = 0; j < rowMax; j++)
                {
                    var value = row.GetCell(j);
                    if (value == null)
                    {
                        continue;
                    }

                    string nvalue = null;
                    //if (sheet.SheetName.Contains("Quest"))
                    //{
                    //    Console.WriteLine("ok");
                    //}
                    nvalue = value.ToString();
                    if (value.CellType == CellType.Formula)
                    {
                        switch (value.CachedFormulaResultType)
                        {
                            case CellType.Numeric:
                                nvalue = value.NumericCellValue.ToString();
                                break;
                            case CellType.String:
                                nvalue = value.StringCellValue;
                                break;
                        }
                    }

                    //C#如何unicode转化为utf-8
                    if (string.IsNullOrEmpty(nvalue))
                    {
                        continue;
                    }

                    nvalue = nvalue.Replace("\\n", "\n");
                    nvalue = nvalue.Replace("\\t", "\t");
                    nvalue = nvalue.Replace("\\r", "\r");
                    itemList[j] = nvalue;
                }

                dataList.Add(itemList);
            }

            return dataList;
        }

        private bool IsClientXls(CfgData cfgData)
        {
            var isClient = false;
            for (var i = 0; i < cfgData.cl_list.Length; i++)
            {
                if (cfgData.cl_list[i].Contains('c'))
                {
                    isClient = true;
                    break;
                }
            }

            return isClient;
        }

        private static int ColumnCnt(CfgData cfgData)
        {
            var cnt = 0;
            foreach (var item in cfgData.cl_list)
            {
                if (item.Contains('c'))
                {
                    cnt++;
                }
            }

            return cnt;
        }

        private bool IsEditorXls(CfgData head)
        {
            var isEditor = false;
            for (var i = 0; i < head.cl_list.Length; i++)
            {
                if (head.cl_list[i].Contains('e'))
                {
                    isEditor = true;
                    break;
                }
            }

            return isEditor;
        }

        private static void ExpContentFile(string file_name, CfgData cfgData, string class_name)
        {
            BinaryWriter bw;
            try
            {
                bw = new BinaryWriter(new FileStream(file_name, FileMode.Create));
            }
            catch (IOException e)
            {
                Console.WriteLine(e.Message + "Cannot create file.");
                return;
            }

            var idList = new List<int>();

            foreach (var row in cfgData.data_list)
            {
                for (var i = 0; i < cfgData.cl_list.Length; i++)
                {
                    if (!cfgData.cl_list[i].Contains('c'))
                    {
                        continue;
                    }

                    var item = row[i];
                    if (cfgData.arr_list[i])
                    {
                        bw.Write("[");

                        if (cfgData.arr_list[i])
                        {
                            var arr = item.Split(',');
                            foreach (var elem in arr)
                            {
                                WriteTxt(bw, cfgData.type_list[i], elem);
                            }
                        }

                        bw.Write("]");
                    }
                    else
                    {
                        WriteTxt(bw, cfgData.type_list[i], item);
                    }

                    bw.Write(", ");
                    bw.Flush();
                }

                bw.Write("\n");
                bw.Write("\n");
                var length = bw.BaseStream.Position;
                bw.Seek(0, SeekOrigin.Begin);
                bw.Flush();
                bw.Close();
            }
        }

        private static void WriteColumns(BinaryWriter bw, CfgData cfgData)
        {
            for (var i = 0; i < cfgData.cl_list.Length; i++)
            {
                if (cfgData.cl_list[i].Contains('c'))
                {
                    bw.Write(cfgData.name_list[i]);
                    bw.Write(cfgData.type_list[i]);
                }
            }
        }

        public class MyData
        {
            public string[] colNames;
            public string[] colType;
            public List<string> endValues = new List<string>();
        }

        public static ConcurrentDictionary<string, MyData> dataDic = new ConcurrentDictionary<string, MyData>();

        private int ExpBinFile(string conf_name, string file_name, CfgData cfgData, string class_name)
        {
            var myData = new MyData();
            //创建表头
            var SQLhead = new List<string>();
            var SQLheadType = new List<string>();
            var dateCount = 0;
            var idList = new List<string>();
            var rowIndex = 0;
            foreach (var row in cfgData.data_list)
            {
                rowIndex++;
                var _builder = new StringBuilder("INSERT INTO " + class_name + " VALUES (");
                for (var colIndex = 0; colIndex < cfgData.cl_list.Length; colIndex++)
                {
                    if (!cfgData.cl_list[colIndex].Contains('c'))
                    {
                        continue;
                    }

                    if (colIndex == 0)
                    {
                        if (row[0].Contains('#'))
                        {
                            continue;
                        }

                        if (!idList.Contains(row[0]))
                        {
                            idList.Add(row[0]);
                        }
                        else
                        {
                            Console.WriteLine("重复的Id(SN) : " + row[0]);
                            return -3;
                        }
                    }

                    if (rowIndex == 1) //表头
                    {
                        SQLhead.Add(cfgData.name_list[colIndex]);
                    }

                    var item = row[colIndex];
                    if (cfgData.arr_list[colIndex]) //是否是数组
                    {
                        var ms = new MemoryStream();
                        var bw = new BinaryWriter(ms);

                        if (!string.IsNullOrEmpty(item))
                        {
                            if (rowIndex == 1)
                            {
                                SQLheadType.Add("text");
                            }

                            var _sqlData = new SqliteData();
                            _sqlData.m_isStr = true;
                            _sqlData.m_str = Convert.ToBase64String(Encoding.UTF8.GetBytes(item));
                            _builder.Append("'" + _sqlData.m_str + "'" + ",");
                        }
                        else
                        {
                            bw.Write(0);
                            if (rowIndex == 1)
                            {
                                SQLheadType.Add("text");
                            }

                            var _sqlData = new SqliteData();
                            _sqlData.m_str = Convert.ToBase64String(ms.ToArray());
                            _sqlData.m_isStr = true;
                            _builder.Append("'" + _sqlData.m_str + "'" + ",");
                        }

                        bw.Close();
                        ms.Close();
                    }
                    else
                    {
                        try
                        {
                            WriteItem(_builder, SQLheadType, cfgData.type_list[colIndex], item, rowIndex,
                                cfgData.name_list[colIndex]);
                        }
                        catch (Exception e)
                        {
                            Console.WriteLine(
                                $"出错的数据是:{conf_name}表{cfgData.data_list[rowIndex - 1][colIndex]}\n{e.Message} {conf_name} 行 ：{rowIndex} 列 ：{cfgData.name_list[colIndex]} SN :{cfgData.data_list[rowIndex - 1][0]}");
                            Console.ReadKey();
                            return -1;
                        }
                    }
                }

                dateCount++;
                if (rowIndex == 1)
                {
                    if (SQLhead.Count == SQLheadType.Count) //创建表头
                    {
                        myData.colNames = SQLhead.ToArray();
                        myData.colType = SQLheadType.ToArray();
                        //SQLiteHelper.Instance().CreateTable(class_name, SQLhead.ToArray(), SQLheadType.ToArray());
                    }

                    //SQLiteHelper.Instance().GetSQLite().BeginTransaction();
                }

                _builder.Append(")");
                _builder.Replace(",)", ")");
                var _endValue = _builder.ToString();
                myData.endValues.Add(_endValue);
                //SQLiteHelper.Instance().InsertValues(class_name, _endValue);
            }

            if (dateCount > 0)
            {
                if (!dataDic.TryAdd(class_name, myData))
                {
                    Console.WriteLine("====================class name has already in: " + class_name);
                }

                //SQLiteHelper.Instance().GetSQLite().Commit();
            }

            return 0;
        }

        private static void WriteTxt(BinaryWriter bw, string type, string value)
        {
            bw.Write(type + " : " + value);
        }

        private static void WriteItem(StringBuilder _builder, List<string> _listType, string type, string value,
            int rowIndex, string colName, BinaryWriter bw = null)
        {
            var strVal = string.IsNullOrEmpty(value) ? "" : value.ToLower().Trim();
            if (type.StartsWith("bool"))
            {
                if (string.IsNullOrEmpty(strVal) || strVal == "false" || strVal == "0.0" || strVal == "0")
                {
                    var nvalue = false;
                    if (rowIndex == 1 && _listType != null)
                    {
                        _listType.Add("bool");
                    }

                    if (bw != null)
                    {
                        bw.Write(nvalue);
                    }
                    else
                    {
                        var _sqlData = new SqliteData();
                        _sqlData.m_byte = 0;
                        _sqlData.m_isByte = true;
                        _builder.Append(_sqlData.m_byte + ",");
                    }
                }
                else if (string.IsNullOrEmpty(strVal) || strVal == "true" || strVal == "1.0" || strVal == "1")
                {
                    var nvalue = true;
                    if (rowIndex == 1 && _listType != null)
                    {
                        _listType.Add("bool");
                    }

                    if (bw != null)
                    {
                        bw.Write(nvalue);
                    }
                    else
                    {
                        var _sqlData = new SqliteData();
                        _sqlData.m_byte = 1;
                        _sqlData.m_isByte = true;
                        _builder.Append(_sqlData.m_byte + ",");
                    }
                }
                else
                {
                    throw new Exception("不可识别的bool值: " + strVal);
                }
            }
            else if (type.StartsWith("byte"))
            {
                if (string.IsNullOrEmpty(strVal))
                {
                    byte nvalue = 0;
                    if (rowIndex == 1 && _listType != null)
                    {
                        _listType.Add("byte");
                    }

                    if (bw != null)
                    {
                        bw.Write(nvalue);
                    }
                    else
                    {
                        var _sqlData = new SqliteData();
                        _sqlData.m_byte = nvalue;
                        _sqlData.m_isByte = true;
                        _builder.Append(_sqlData.m_byte + ",");
                    }
                }
                else
                {
                    var nvalue = byte.Parse(strVal);
                    if (rowIndex == 1 && _listType != null)
                    {
                        _listType.Add("byte");
                    }

                    if (bw != null)
                    {
                        bw.Write(nvalue);
                    }
                    else
                    {
                        var _sqlData = new SqliteData();
                        _sqlData.m_byte = nvalue;
                        _sqlData.m_isByte = true;
                        _builder.Append(_sqlData.m_byte + ",");
                    }
                }
            }
            else if (type.StartsWith("short"))
            {
                if (string.IsNullOrEmpty(strVal))
                {
                    short nvalue = 0;
                    if (rowIndex == 1 && _listType != null)
                    {
                        _listType.Add("short");
                    }

                    if (bw != null)
                    {
                        bw.Write(nvalue);
                    }
                    else
                    {
                        var _sqlData = new SqliteData();
                        _sqlData.m_short = nvalue;
                        _sqlData.m_isShort = true;
                        _builder.Append(_sqlData.m_short + ",");
                    }
                }
                else
                {
                    var nvalue = short.Parse(strVal);
                    if (rowIndex == 1 && _listType != null)
                    {
                        _listType.Add("short");
                    }

                    if (bw != null)
                    {
                        bw.Write(nvalue);
                    }
                    else
                    {
                        var _sqlData = new SqliteData();
                        _sqlData.m_short = nvalue;
                        _sqlData.m_isShort = true;
                        _builder.Append(_sqlData.m_short + ",");
                    }
                }
            }
            else if (type.StartsWith("int"))
            {
                if (string.IsNullOrEmpty(strVal))
                {
                    var nvalue = 0;
                    if (rowIndex == 1 && _listType != null)
                    {
                        _listType.Add("integer");
                    }

                    if (bw != null)
                    {
                        bw.Write(nvalue);
                    }
                    else
                    {
                        var _sqlData = new SqliteData();
                        _sqlData.m_int = nvalue;
                        _sqlData.m_isInt = true;
                        //_listValue.Add(_sqlData);
                        _builder.Append(_sqlData.m_int + ",");
                    }
                }
                else
                {
                    var nvalue = 0;
                    //if (strVal.Contains(".0")) {
                    //    double dvalue = double.Parse(strVal);
                    //    nvalue = (int)dvalue;
                    //}
                    nvalue = int.Parse(strVal);
                    if (rowIndex == 1 && _listType != null)
                    {
                        _listType.Add("integer");
                    }

                    if (bw != null)
                    {
                        bw.Write(nvalue);
                    }
                    else
                    {
                        var _sqlData = new SqliteData();
                        _sqlData.m_int = nvalue;
                        _sqlData.m_isInt = true;
                        //_listValue.Add(_sqlData);
                        _builder.Append(_sqlData.m_int + ",");
                    }
                }
            }
            else if (type.StartsWith("long"))
            {
                if (string.IsNullOrEmpty(strVal))
                {
                    long nvalue = 0;
                    if (rowIndex == 1 && _listType != null)
                    {
                        _listType.Add("long");
                    }

                    if (bw != null)
                    {
                        bw.Write(nvalue);
                    }
                    else
                    {
                        var _sqlData = new SqliteData();
                        _sqlData.m_long = nvalue;
                        _sqlData.m_isLong = true;
                        // _listValue.Add(_sqlData);
                        _builder.Append(_sqlData.m_long + ",");
                    }
                }
                else
                {
                    var nvalue = long.Parse(strVal);
                    //
                    if (rowIndex == 1 && _listType != null)
                    {
                        _listType.Add("long");
                    }

                    if (bw != null)
                    {
                        bw.Write(nvalue);
                    }
                    else
                    {
                        var _sqlData = new SqliteData();
                        _sqlData.m_long = nvalue;
                        _sqlData.m_isLong = true;
                        // _listValue.Add(_sqlData);
                        _builder.Append(_sqlData.m_long + ",");
                    }
                }
            }
            else if (type.StartsWith("float"))
            {
                if (string.IsNullOrEmpty(strVal))
                {
                    var nvalue = 0.0f;

                    if (rowIndex == 1 && _listType != null)
                    {
                        _listType.Add("float");
                    }

                    if (bw != null)
                    {
                        bw.Write(nvalue);
                    }
                    else
                    {
                        var _sqlData = new SqliteData();
                        _sqlData.m_float = nvalue;
                        _sqlData.m_isFloat = true;
                        // _listValue.Add(_sqlData);
                        _builder.Append(_sqlData.m_float + ",");
                    }
                }
                else
                {
                    var nvalue = float.Parse(strVal);
                    // 
                    if (rowIndex == 1 && _listType != null)
                    {
                        _listType.Add("float");
                    }

                    if (bw != null)
                    {
                        bw.Write(nvalue);
                    }
                    else
                    {
                        var _sqlData = new SqliteData();
                        _sqlData.m_float = nvalue;
                        _sqlData.m_isFloat = true;
                        // _listValue.Add(_sqlData);
                        _builder.Append(_sqlData.m_float + ",");
                    }
                }
            }
            else if (type.StartsWith("double"))
            {
                if (string.IsNullOrEmpty(strVal))
                {
                    var nvalue = 0.0;
                    //
                    if (rowIndex == 1 && _listType != null)
                    {
                        _listType.Add("double");
                    }

                    if (bw != null)
                    {
                        bw.Write(nvalue);
                    }
                    else
                    {
                        var _sqlData = new SqliteData();
                        _sqlData.m_double = nvalue;
                        _sqlData.m_isDouble = true;
                        // _listValue.Add(_sqlData);
                        _builder.Append(_sqlData.m_double + ",");
                    }
                }
                else
                {
                    var nvalue = double.Parse(strVal);
                    //

                    if (rowIndex == 1 && _listType != null)
                    {
                        _listType.Add("double");
                    }

                    if (bw != null)
                    {
                        bw.Write(nvalue);
                    }
                    else
                    {
                        var _sqlData = new SqliteData();
                        _sqlData.m_double = nvalue;
                        _sqlData.m_isDouble = true;
                        //_listValue.Add(_sqlData);
                        _builder.Append(_sqlData.m_double + ",");
                    }
                }
            }
            else if (type.StartsWith("string"))
            {
                if (string.IsNullOrEmpty(strVal))
                {
                    if (rowIndex == 1 && _listType != null)
                    {
                        _listType.Add("text");
                    }

                    if (bw != null)
                    {
                        bw.Write("");
                    }
                    else
                    {
                        var _sqlData = new SqliteData();
                        _sqlData.m_str = "";
                        _sqlData.m_isStr = true;
                        //_listValue.Add(_sqlData);
                        _builder.Append("'" + _sqlData.m_str + "'" + ",");
                    }
                }
                else
                {
                    //
                    if (rowIndex == 1 && _listType != null)
                    {
                        _listType.Add("text");
                    }

                    if (bw != null)
                    {
                        bw.Write(value);
                    }
                    else
                    {
                        var _sqlData = new SqliteData();
                        _sqlData.m_str = value;
                        _sqlData.m_isStr = true;
                        //_listValue.Add(_sqlData);
                        _builder.Append("'" + _sqlData.m_str + "'" + ",");
                    }
                }
            }
            else if (type.StartsWith("vector3d"))
            {
                if (string.IsNullOrEmpty(strVal))
                {
                    if (rowIndex == 1 && _listType != null)
                    {
                        _listType.Add("text");
                    }

                    if (bw != null)
                    {
                        bw.Write("");
                    }
                    else
                    {
                        var _sqlData = new SqliteData();
                        _sqlData.m_str = "";
                        _sqlData.m_isStr = true;
                        //_listValue.Add(_sqlData);
                        _builder.Append("'" + _sqlData.m_str + "'" + ",");
                    }
                }
                else
                {
                    //
                    if (rowIndex == 1 && _listType != null)
                    {
                        _listType.Add("text");
                    }

                    if (bw != null)
                    {
                        bw.Write(value);
                    }
                    else
                    {
                        var _sqlData = new SqliteData();
                        _sqlData.m_str = value;
                        _sqlData.m_isStr = true;
                        //_listValue.Add(_sqlData);
                        _builder.Append("'" + _sqlData.m_str + "'" + ",");
                    }
                }
            }
            else if (type.StartsWith("vector2d"))
            {
                if (string.IsNullOrEmpty(strVal))
                {
                    if (rowIndex == 1 && _listType != null)
                    {
                        _listType.Add("text");
                    }

                    if (bw != null)
                    {
                        bw.Write("");
                    }
                    else
                    {
                        var _sqlData = new SqliteData();
                        _sqlData.m_str = "";
                        _sqlData.m_isStr = true;
                        //_listValue.Add(_sqlData);
                        _builder.Append("'" + _sqlData.m_str + "'" + ",");
                    }
                }
                else
                {
                    if (rowIndex == 1 && _listType != null)
                    {
                        _listType.Add("text");
                    }

                    if (bw != null)
                    {
                        bw.Write(value);
                    }
                    else
                    {
                        var _sqlData = new SqliteData();
                        _sqlData.m_str = value;
                        _sqlData.m_isStr = true;
                        //_listValue.Add(_sqlData);
                        _builder.Append("'" + _sqlData.m_str + "'" + ",");
                    }
                }
            }
            else
            {
                throw new Exception("数据类型不正确 " + type);
            }
        }
#if !Release
        public static int PackConfigDir(string srcDir, string codeDir, string binDir, string editorCodeDir)
#else
        public static int PackConfigDir(string srcDir, string binDir, string editorCodeDir)
#endif
        {
#if !Release
            //PathUtil.ClearFolderByType(codeDir, "cs");
            PathUtil.ClearFolderByType(Config.DST_GAME_LUA_CONF_CODE_PATH, "lua");
#endif
            PathUtil.ClearFolderByType(binDir, "bytes");
            PathUtil.ClearFolderByType(editorCodeDir, "cs");

            var cls_name_list = new List<string>();
            PathUtil.DirectoryTouch(srcDir);
            var di = new DirectoryInfo(srcDir);
            var files = di.GetFiles();
            //foreach (FileInfo file in files)
            Parallel.ForEach(files, file =>
            {
                string[] fullFileName;
                FileStream fs = null;
                IWorkbook workbook = null;
                fullFileName = file.Name.Split('.');
                if (fullFileName.Length <= 0 || file.Name.StartsWith("~"))
                {
                    return;
                }

                var fileExt = fullFileName[fullFileName.Length - 1];
                if (fileExt == "xls" || fileExt == "xlsx" || fileExt == "xlsm")
                {
                    var fileName = file.Name;
                    //try
                    //{
                    fs = new FileStream(srcDir + "/" + fileName, FileMode.Open, FileAccess.Read, FileShare.ReadWrite);
                    if (fileName.IndexOf(".xlsx") > 0 || fileName.IndexOf(".xlsm") > 0) // 2007版本及以后
                    {
                        workbook = new XSSFWorkbook(fs);
                    }
                    else if (fileName.IndexOf(".xls") > 0) // 2003版本
                    {
                        workbook = new HSSFWorkbook(fs);
                    }

                    var vSheetCount = workbook.NumberOfSheets;
                    if (vSheetCount == 0)
                    {
                        Console.WriteLine("无法正确读取 : " + file.Name + " 数据表");
                        return;
                    }

                    Console.WriteLine("pack file : " + file.Name + "  Prepare");

                    var curTableIndex = 0;
                    var packConfig = new PackConfig();
                    while (curTableIndex < vSheetCount)
                    {
                        var worksheet = workbook.GetSheetAt(curTableIndex);

                        if ( /*worksheet.SheetName.ToLower().Contains("sheet") || */
                            worksheet.SheetName.Split('|').Length != 2)
                        {
                            curTableIndex++;
                            continue;
                        }

                        var tname = worksheet.SheetName.Split('|')[1];
                        var className = "Conf" + tname;
                        var codeFile = className + ".cs";
                        var binFile = tname + ".bytes";
                        var csvFile = tname + ".csv";
                        var txtFile = tname + ".txt";

                        var cfgData = packConfig.ParseXls(worksheet);
                        if (packConfig.IsClientXls(cfgData))
                        {
                            Console.WriteLine("pack file : " + tname);
#if !Release
                            var code_file_path = Path.Combine(codeDir, codeFile);
                            new CodeGenerator().GenCode(code_file_path, worksheet, file.Name);
#endif
                            cls_name_list.Add(className);

                            var bin_path = Path.Combine(binDir, binFile);
                            var nextTableIndex = curTableIndex + 1;
                            while (nextTableIndex < vSheetCount)
                            {
                                var nextWorkSheet = workbook.GetSheetAt(nextTableIndex);
                                if (nextWorkSheet.SheetName.Split('|').Length != 2 ||
                                    nextWorkSheet.SheetName.ToLower().Contains("sheet"))
                                {
                                    nextTableIndex++;
                                }
                                else
                                {
                                    var nextTableName = nextWorkSheet.SheetName.Split('|')[1];
                                    if (nextTableName == tname)
                                    {
                                        Console.WriteLine("append file: " + tname);
                                        packConfig.AppendData(nextWorkSheet, cfgData);
                                        curTableIndex = nextTableIndex;
                                        nextTableIndex++;
                                    }
                                    else
                                    {
                                        break;
                                    }
                                }
                            }

                            if (packConfig.ExpBinFile(fileName, bin_path, cfgData, className) < 0)
                            {
                                Console.WriteLine("生成二进制数据文件 " + fileName + "文件," + worksheet.SheetName + "表" +
                                                  bin_path + " 出错");
                                return;
                            }
                        }

                        if (packConfig.IsEditorXls(cfgData))
                        {
                            //Console.WriteLine("gen config class file for editor :" + file);
                            //string editor_code_file_path = Path.Combine(editorCodeDir, codeFile);
                            //new CodeGenerator().GenEditorCode(editor_code_file_path, worksheet, file.Name);
                        }

                        curTableIndex++;
                    }

                    //}
                    //catch (Exception ex)
                    //{
                    //    Console.WriteLine("=====================error file: " + fileName);
                    //    Console.WriteLine(ex.ToString());
                    //}
                }

                //}
            });
#if mac
            MonoSqliteHelper.Instance.BeginTransaction();
#else
            SQLiteHelper.Instance().GetSQLite().BeginTransaction();
#endif
            foreach (var data in dataDic)
            {
                if (data.Value.colNames != null)
                {
#if mac
                    MonoSqliteHelper.Instance.CreateTable(data.Key, data.Value.colNames, data.Value.colType);
#else
                    SQLiteHelper.Instance().CreateTable(data.Key, data.Value.colNames, data.Value.colType);
#endif
                }

                for (var i = 0; i < data.Value.endValues.Count; i++)
                {
#if mac
                    MonoSqliteHelper.Instance.InsertValues(data.Key, data.Value.endValues[i]);
#else
                    SQLiteHelper.Instance().InsertValues(data.Key, data.Value.endValues[i]);
#endif
                }
            }
#if mac
            MonoSqliteHelper.Instance.Commit();
#else
            SQLiteHelper.Instance().GetSQLite().Commit();
#endif
            File.Copy(Config.SQLITE_PATH, Path.Combine(binDir, Config.SQLITE_NAME), true);
            var p = Path.Combine(Config.SRC_PATH, "../bin/res");
            PathUtil.DirectoryTouch(p);
            File.Copy(Config.SQLITE_PATH, Path.Combine(p, Config.SQLITE_NAME), true);
#if !Release
            //string fact_code_file_path = Path.Combine(codeDir, "ConfFact.cs");
            //CodeGenerator.GenFactCode(fact_code_file_path, cls_name_list);
#endif
#if mac
            MonoSqliteHelper.Instance.CloseConnection();
#else
            SQLiteHelper.Instance().CloseConnection();
#endif
            return 0;
        }
    }
}