using System.Collections.Generic;
using System.IO;
using System.Text.RegularExpressions;
using NPOI.SS.UserModel;
using RazorEngine;

namespace CSharpPack.codes
{
    public class CodeGenerator
    {
        public void GenCode(string targetFileName, ISheet worksheet, string excelName)
        {
            var tableName = worksheet.SheetName.Split('|')[1];
            var msg = ParseXls(worksheet);
            msg.TableName = tableName;
            msg.ExcelName = excelName;
            //GenFile(msg, targetFileName);
#if !Release
            //添加lua生成代码
            var luaFile = Path.GetFileName(targetFileName);
            var luaPath = Path.Combine(Config.DST_GAME_LUA_CONF_CODE_PATH, luaFile.Replace(".cs", ".lua"));
            GenLuaFile(msg, luaPath);
#endif
        }

        private static string confLuaTempl;
        private static object lockObj = new object();

        private void GenLuaFile(SheetMsg msg, string genFile)
        {
            if (confLuaTempl == null)
            {
                confLuaTempl = File.ReadAllText(Config.CONF_LUA_TMPL);
            }

            lock (lockObj)
            {
                var result = Razor.Parse(confLuaTempl, msg, "Lua");
                File.WriteAllText(genFile, result);
            }
        }

        private static string confTempl;

        private static void GenFile(SheetMsg msg, string genFile)
        {
            if (confTempl == null)
            {
                confTempl = File.ReadAllText(Config.CONF_TMPL);
            }

            var result = Razor.Parse(confTempl, msg, "Conf");
            result = result.Replace("boolean", "bool");
            File.WriteAllText(genFile, result);
        }

        public void GenEditorCode(string targetFileName, ISheet worksheet, string excelName)
        {
            var tableName = worksheet.SheetName.Split('|')[1];
            var msg = ParseXls(worksheet);
            msg.TableName = tableName;
            msg.ExcelName = excelName;
            GenEditorFile(msg, targetFileName);
        }

        public static void GenFactCode(string targetFileName, List<string> cls_name_list)
        {
            var msg = new ClassListMsg();
            msg.cls_name_list = cls_name_list;
            GenFactFile(msg, targetFileName);
        }

        private SheetMsg ParseXls(ISheet worksheet)
        {
            var msg = new SheetMsg();
            var columns = worksheet.GetRow(0).LastCellNum;
            var col_cnt = 0;
            for (var j = 0; j < columns; j++)
            {
                var row = worksheet.GetRow(0);
                //string cl_flag = row.GetCell(j).ToString();
                //if (!string.IsNullOrEmpty(cl_flag) && cl_flag.Contains("c"))
                //{
                //    col_cnt++;
                //}
                if (row.GetCell(j) != null)
                {
                    var cl_flag = row.GetCell(j).ToString();
                    if (cl_flag.Contains("c"))
                    {
                        col_cnt++;
                    }
                }
            }

            msg.AttributeTypes = new string[col_cnt];
            msg.AttributeNames = new string[col_cnt];
            msg.AttributeValues = new int[col_cnt];
            msg.SheetColumns = col_cnt;
            col_cnt = 0;
            for (var j = 0; j < columns; j++)
            {
                //string cl_flag = worksheet.GetRow(0).GetCell(j).ToString();
                //if (!string.IsNullOrEmpty(cl_flag) && cl_flag.Contains("c"))
                //{
                //    string tmp = worksheet.GetRow(1).GetCell(j).ToString();
                //    msg.AttributeTypes[col_cnt] = tmp.Trim();
                //    tmp = worksheet.GetRow(2).GetCell(j).ToString();
                //    msg.AttributeNames[col_cnt] = tmp.Trim();
                //    col_cnt++;
                //}
                var row = worksheet.GetRow(0);
                if (row.GetCell(j) != null)
                {
                    var cl_flag = row.GetCell(j).ToString();
                    if (cl_flag.Contains("c"))
                    {
                        var tmp = worksheet.GetRow(1).GetCell(j).ToString();
                        msg.AttributeTypes[col_cnt] = tmp.Trim();
                        tmp = worksheet.GetRow(2).GetCell(j).ToString();
                        msg.AttributeNames[col_cnt] = tmp.Trim();
                        msg.AttributeValues[col_cnt] = ReadColumnData(msg.AttributeTypes[col_cnt]);
                        col_cnt++;
                    }
                }
            }

            return msg;
        }

        private void GenEditorFile(SheetMsg msg, string genFile)
        {
            var content = File.ReadAllText(Config.CONF_EDITOR_TMPL);
            var result = Razor.Parse(content, msg);
            result = result.Replace("boolean", "bool");
            File.WriteAllText(genFile, result);
        }

        private static void GenFactFile(ClassListMsg msg, string genFile)
        {
            var content = File.ReadAllText(Config.CONF_FACT_TMPL);
            var result = Razor.Parse(content, msg);
            result = result.Replace("boolean", "bool");
            File.WriteAllText(genFile, result);
        }

        /// <summary>
        ///     重新规划类型
        ///     1-20   基础类型
        ///     21-40  基础类型1维数组
        ///     41-60  基础类型2维数组
        /// </summary>
        /// <param name="type"></param>
        /// <returns></returns>
        private static int ReadColumnData(string type)
        {
            if (type.StartsWith("int"))
            {
                var count = Regex.Matches(type, @"\[").Count;
                if (count == 0)
                {
                    return 1;
                }

                if (count == 1)
                {
                    return 21;
                }

                if (count == 2)
                {
                    return 41;
                }
            }
            else if (type.StartsWith("string") || type.StartsWith("String"))
            {
                var count = Regex.Matches(type, @"\[").Count;
                if (count == 0)
                {
                    return 2;
                }

                if (count == 1)
                {
                    return 22;
                }

                if (count == 2)
                {
                    return 42;
                }
            }
            else if (type.StartsWith("float"))
            {
                var count = Regex.Matches(type, @"\[").Count;
                if (count == 0)
                {
                    return 3;
                }

                if (count == 1)
                {
                    return 23;
                }

                if (count == 2)
                {
                    return 43;
                }
            }
            else if (type.StartsWith("bool") || type.StartsWith("boolean"))
            {
                var count = Regex.Matches(type, @"\[").Count;
                if (count == 0)
                {
                    return 4;
                }

                if (count == 1)
                {
                    return 24;
                }

                if (count == 2)
                {
                    return 44;
                }
            }
            else if (type.StartsWith("short"))
            {
                var count = Regex.Matches(type, @"\[").Count;
                if (count == 0)
                {
                    return 5;
                }

                if (count == 1)
                {
                    return 25;
                }

                if (count == 2)
                {
                    return 45;
                }
            }
            else if (type.StartsWith("byte"))
            {
                var count = Regex.Matches(type, @"\[").Count;
                if (count == 0)
                {
                    return 6;
                }

                if (count == 1)
                {
                    return 26;
                }

                if (count == 2)
                {
                    return 46;
                }
            }
            else if (type.StartsWith("long"))
            {
                var count = Regex.Matches(type, @"\[").Count;
                if (count == 0)
                {
                    return 7;
                }

                if (count == 1)
                {
                    return 27;
                }

                if (count == 2)
                {
                    return 47;
                }
            }
            else if (type.Contains("double"))
            {
                var count = Regex.Matches(type, @"\[").Count;
                if (count == 0)
                {
                    return 8;
                }

                if (count == 1)
                {
                    return 28;
                }

                if (count == 2)
                {
                    return 48;
                }
            }
            else if (type.Contains("vector3d"))
            {
                var count = Regex.Matches(type, @"\[").Count;
                if (count == 0)
                {
                    return 9;
                }

                if (count == 1)
                {
                    return 29;
                }

                if (count == 2)
                {
                    return 49;
                }
            }
            else if (type.Contains("vector2d"))
            {
                var count = Regex.Matches(type, @"\[").Count;
                if (count == 0)
                {
                    return 10;
                }

                if (count == 1)
                {
                    return 30;
                }

                if (count == 2)
                {
                    return 50;
                }
            }

            return 0;
        }
    }

    public class SheetMsg
    {
        public string[] AttributeTypes { get; set; }
        public string[] AttributeNames { get; set; }
        public string TableName { get; set; }
        public int SheetColumns { get; set; }
        public string ExcelName { get; set; }
        public int[] AttributeValues { get; set; }
    }

    public class ClassListMsg
    {
        public List<string> cls_name_list;
    }
}