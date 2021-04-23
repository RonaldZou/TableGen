using System;
using System.Collections.Generic;
using System.IO;
using System.Text;
using SQLite;

namespace CSharpPack.codes
{
    public class SQLiteHelper
    {
        /// <summary>
        ///     数据库连接定义
        /// </summary>
        private SQLiteConnection m_dbConnection;

        /// <summary>
        ///     SQL命令定义
        /// </summary>
        private SQLiteCommand m_dbCommand;

        /// <summary>
        ///     instance
        /// </summary>
        private static SQLiteHelper m_instance;

        /// <summary>
        ///     构造函数
        /// </summary>
        /// <param name="connectionString">数据库连接字符串</param>
        public SQLiteHelper(string connectionString)
        {
            try
            {
                //构造数据库连接
                var cStr = Directory.GetCurrentDirectory() + "/" + connectionString;
                if (File.Exists(cStr))
                {
                    File.Delete(cStr);
                }

                m_dbConnection = new SQLiteConnection(cStr);
            }
            catch (Exception e)
            {
                Console.WriteLine(e.Message);
            }
        }

        public static SQLiteHelper Instance()
        {
            return m_instance ?? (m_instance = new SQLiteHelper(Config.SQLITE_NAME));
        }

        public SQLiteConnection GetSQLite()
        {
            return m_dbConnection;
        }

        /// <summary>
        ///     关闭数据库连接
        /// </summary>
        public void CloseConnection()
        {
            //销毁Connection
            m_dbConnection?.Close();

            m_dbConnection = null;
        }

        /// <summary>
        ///     向指定数据表中插入数据
        /// </summary>
        /// <param name="tableName"></param>
        /// <param name="value"></param>
        public void InsertValues(string tableName, string value)
        {
            m_dbConnection.Query<object>(value);
        }

        /// <summary>
        ///     创建数据表
        /// </summary>
        /// <param name="tableName"></param>
        /// <param name="colNames"></param>
        /// <param name="colType"></param>
        public void CreateTable(string tableName, string[] colNames, string[] colType)
        {
            var _builder = new StringBuilder();
            _builder.Append("CREATE TABLE IF NOT EXISTS " + tableName + " (");
            for (var i = 0; i < colNames.Length; i++)
            {
                if (i == colNames.Length - 1)
                {
                    _builder.Append("'" + colNames[i] + "'" + " " + colType[i]);
                }
                else
                {
                    if (i == 0)
                    {
                        _builder.Append("'" + colNames[i] + "'" + " " + colType[i] + " PRIMARY KEY NOT NULL , ");
                    }
                    else
                    {
                        _builder.Append("'" + colNames[i] + "'" + " " + colType[i] + ", ");
                    }
                }
            }

            _builder.Append("  ) ");
            m_dbConnection.Query<object>(_builder.ToString());
        }

        /// <summary>
        ///     读取全部表数据
        /// </summary>
        /// <param name="name"></param>
        /// <param name="idTitle"></param>
        /// <param name="valueTitle"></param>
        /// <returns></returns>
        public void ReadFullTable(string name, string idTitle, string valueTitle)
        {
            var query = "SELECT * FROM " + name;
            m_dbConnection.Query<object>(query);
        }

        /// <summary>
        ///     插入数据
        /// </summary>
        /// <param name="name"></param>
        /// <param name="id"></param>
        /// <param name="value"></param>
        /// <returns></returns>
        public void InsertInto(string name, string id, string value)
        {
            var query = "INSERT INTO " + name + " VALUES (" + "'" + id + "'" + ", " + "'" + value + "'" + ")";
            m_dbConnection.Query<object>(query);
        }

        /// <summary>
        ///     更新数据
        /// </summary>
        /// <param name="name"></param>
        /// <param name="id"></param>
        /// <param name="value"></param>
        /// <returns></returns>
        public void UpdateInto(string name, string id, string value)
        {
            var query = "UPDATE " + name + " SET " + "value" + " = " + "'" + value + "'" + " WHERE " + "id" + " = " +
                        "'" + id + "'";

            m_dbConnection.Query<object>(query);
        }

        /// <summary>
        ///     删除数据
        /// </summary>
        /// <param name="name"></param>
        /// <param name="id"></param>
        /// <returns></returns>
        public bool DeleteInfo(string name, string id)
        {
            if (id == null || "".Equals(id))
            {
                Console.WriteLine("deleteInto--------ID为空--------表名为:" + name);
                return false;
            }

            var query = "DELETE FROM " + name + " WHERE " + "id" + " = " + "'" + id + "'";

            m_dbConnection.Query<object>(query);

            return true;
        }

        /// <summary>
        ///     查询数据
        /// </summary>
        /// <param name="name"></param>
        /// <param name="id"></param>
        /// <param name="idTitle"></param>
        /// <param name="valueTitle"></param>
        /// <returns></returns>
        public List<object> SelectWhere(string name, string id, string idTitle, string valueTitle)
        {
            var query = "SELECT " + valueTitle + " FROM " + name + " WHERE " + idTitle + " = " + "'" + id + "'";
            return m_dbConnection.Query<object>(query);
        }
    }
}