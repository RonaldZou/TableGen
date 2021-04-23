using System.IO;

namespace CSharpPack.codes
{
    internal class Config
    {
        public static string CWD = Directory.GetCurrentDirectory();

#if Release
        public static string ROOT_PATH = Path.Combine(CWD, @"../../../../../trunk/public/");
#else
        public static string ROOT_PATH = Path.Combine(CWD, @"../../../../../public/");
        public static string CLIENT_PATH = Path.Combine(CWD, @"../../../");
        public static string SRC_PATH => Path.Combine(CLIENT_PATH, @"goe/client/project");
#endif
        public static string RES_PATH => Path.Combine(ROOT_PATH, @"res");
        public static string EXP_PATH => Path.Combine(ROOT_PATH, @"exp");
#if !Release
        public static string SRC_PROJ_PATH => Path.Combine(SRC_PATH, @"Assets");
#endif

        //pack index
        public static string RES_EDITOR_PATH => Path.Combine(RES_PATH, "editor");
        public static string RES_LOG_PATH => Path.Combine(RES_PATH, "buildInfo.txt");
        public static string RES_LOG_Package_PATH => Path.Combine(RES_PATH, "PackageLog.txt");
        public static string DST_EDITOR_CONF_CODE_PATH => Path.Combine(RES_EDITOR_PATH, "Assets/conf/Editor");

        //game conf
#if Release
        public static string SRC_GAME_CONF_PATH => Path.Combine(ROOT_PATH, "config");
#else
        public static string SRC_GAME_CONF_PATH = Path.Combine(ROOT_PATH, "config");
#endif
        public static string RES_GAME_CONF_FILE_PATH => Path.Combine(RES_EDITOR_PATH, "Assets/Res/Config");
#if !Release
        public static string DST_GAME_CONF_CODE_PATH => Path.Combine(SRC_PROJ_PATH, "game/conf");
        public static string DST_GAME_LUA_CONF_CODE_PATH => Path.Combine(SRC_PATH, "../lua/conf");
#endif

        //tmpl 
        public static string TMPL_PATH = Path.Combine(CWD, "tmpl/");
        public static string CONF_TMPL => Path.Combine(TMPL_PATH, "conf_tmpl.tmpl");
        public static string CONF_LUA_TMPL => Path.Combine(TMPL_PATH, "conf_lua.tmpl");
        public static string CONF_EDITOR_TMPL => Path.Combine(TMPL_PATH, "conf_editor.tmpl");
        public static string CONF_FACT_TMPL => Path.Combine(TMPL_PATH, "conf_fact.tmpl");

        public static string SQLITE_NAME = "ConfData.bytes";
        public static string SQLITE_PATH = Path.Combine(CWD, SQLITE_NAME);
    }
}