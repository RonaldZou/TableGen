using Mono.Data.Sqlite;
using System;
using System.IO;
using System.Text;

namespace CSharpPack.codes
{
    internal class MonoSqliteHelper
    {
        private SqliteConnection dbConnection;

        private SqliteTransaction dbTransaction;

        private static MonoSqliteHelper instance;
        public static MonoSqliteHelper Instance => instance ?? (instance = new MonoSqliteHelper(Config.SQLITE_NAME));

        public MonoSqliteHelper(string connectionString)
        {
            try
            {
                var cStr = Directory.GetCurrentDirectory() + "/" + connectionString;
                Console.WriteLine(cStr);
                if (File.Exists(cStr))
                {
                    Console.WriteLine(" ------ Delete " + cStr);
                    File.Delete(cStr);
                }

                dbConnection = new SqliteConnection("URI=file:" + cStr);
                dbConnection.Open();
            }
            catch (Exception e)
            {
                Console.WriteLine(e.Message);
            }
        }

        public void CloseConnection()
        {
            if (dbConnection != null)
            {
                dbConnection.Close();
            }

            dbConnection.Dispose();
        }

        public void BeginTransaction()
        {
            dbTransaction = dbConnection.BeginTransaction();
        }

        public void Commit()
        {
            if (null != dbTransaction)
            {
                dbTransaction.Commit();
            }
        }

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

            using (var cmd = new SqliteCommand(_builder.ToString(), dbConnection))
            {
                cmd.ExecuteNonQuery();
            }
        }

        public void InsertValues(string tableName, string value)
        {
            using (var cmd = new SqliteCommand(value, dbConnection))
            {
                cmd.ExecuteNonQuery();
            }
        }
    }
}