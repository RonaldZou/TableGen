using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.IO;
using System.Text;

namespace CSharpPack.codes
{
    internal class BuildConfig
    {
        private static int DoGenConfig()
        {
            Console.WriteLine("Pack Config Begin...");
#if !Release
            if (PackConfig.PackConfigDir(Config.SRC_GAME_CONF_PATH, Config.DST_GAME_CONF_CODE_PATH,
                Config.RES_GAME_CONF_FILE_PATH,
                Config.DST_EDITOR_CONF_CODE_PATH) < 0)
            {
                Console.WriteLine("Pack Config Fail");
                return -1;
            }
#else
            if(PackConfig.PackConfigDir(Config.SRC_GAME_CONF_PATH, Config.RES_GAME_CONF_FILE_PATH,
                Config.DST_EDITOR_CONF_CODE_PATH) < 0){
                    Console.WriteLine("Pack Config Fail");
                    return -1;
            }
#endif
            Console.WriteLine("Pack Config Success");
            return 0;
        }

        private static int StartProcess(string file, string argument, string workingFolder = null)
        {
            try
            {
                var info = new ProcessStartInfo(file)
                {
                    Arguments = argument, UseShellExecute = false, CreateNoWindow = true
                };
                if (workingFolder != null)
                {
                    info.WorkingDirectory = workingFolder;
                }

                var p = Process.Start(info);
                p.WaitForExit();
                return p.ExitCode;
            }
            catch (Exception ex)
            {
                Console.WriteLine("Process start error:" + ex);
                return 1;
            }
        }

        private static void DoPackConfigAssets(string packtype = "1", string cmd = null)
        {
            Console.WriteLine("pack assets begin ...");
            //win32
            var system = Environment.OSVersion;
            Console.WriteLine("Platform:" + system.Platform);
            if (string.IsNullOrEmpty(cmd))
            {
                if (system.Platform == PlatformID.Win32NT)
                {
                    cmd = "Unity.exe";
                }
                else if (system.Platform == PlatformID.MacOSX || system.Platform == PlatformID.Unix)
                {
                    cmd = "/Applications/Unity2018/Unity.app/Contents/MacOS/Unity";
                }
            }

            var param = "-executeMethod MUEditor.Package.PackageManager.";

            if (packtype == "1")
            {
                param = param + "PackageAllWindows";
            }
            else if (packtype == "2")
            {
                param = param + "PackageAllAndroid";
            }
            else if (packtype == "3")
            {
                param = param + "PackageAlliOS";
            }
            else if (packtype == "11")
            {
                param = param + "ForcePackageAllWindows";
            }
            else if (packtype == "21")
            {
                param = param + "ForcePackageAllAndroid";
            }
            else if (packtype == "31")
            {
                param = param + "ForcePackageAlliOS";
            }
            else if (packtype == "7")
            {
                param = param + "PackAllIOSNoUI";
            }
            else
            {
                return;
            }

            Console.WriteLine(param);
            var returnCode = StartProcess(cmd,
                $"-batchmode -quit -projectPath {Config.RES_EDITOR_PATH} {param} -logFile {Config.RES_LOG_PATH}");
            if (returnCode != 0)
            {
                //Console.WriteLine(string.Format("打包资源失败: {0} ", returnCode));
                var file = new StreamReader(Config.RES_LOG_PATH, Encoding.Default);
                var infoList = new List<string>();
                while (!file.EndOfStream)
                {
                    infoList.Add(file.ReadLine());
                }

                var index = Math.Max(0, infoList.Count - 100);
                var info = string.Empty;
                for (var i = index; i < infoList.Count; i++)
                {
                    info += infoList[i] + Environment.NewLine;
                }

                Console.Write(info);
            }

            var debugInfo = Environment.NewLine + "-------------"
                                                + Environment.NewLine + "PackageLog: " + Environment.NewLine;
            Console.WriteLine(debugInfo);
            var packfile = new StreamReader(Config.RES_LOG_Package_PATH, Encoding.Default);
            while (!packfile.EndOfStream)
            {
                Console.WriteLine(packfile.ReadLine());
            }

            if (returnCode == 0)
            {
                Console.WriteLine("打包资源成功！");
            }
            else
            {
                throw new SystemException("打包失败!");
            }
        }

        private static int Main(string[] args)
        {
            Console.WriteLine("Pack Config");
            Config.SRC_GAME_CONF_PATH = Path.Combine(Config.CWD, args[0]);
            Config.TMPL_PATH = Path.Combine(Config.CWD, args[1]);
            Config.ROOT_PATH = Path.Combine(Config.CWD, args[2]);
            Config.CLIENT_PATH = Path.Combine(Config.CWD, args[3]);
#if Release
            if (args.Contains("config")){
                if (DoGenConfig() < 0){
                    return 1;
                }
            }
#else
            if (DoGenConfig() < 0)
            {
                return 1;
            }
#endif
            if (args.Length != 0)
            {
                string cmd = null;
                if (args[args.Length - 1].Contains("Unity"))
                {
                    cmd = args[args.Length - 1];
                }

                DoPackConfigAssets(args[0]);
            }

            Environment.Exit(0);
            return 0;
        }
    }
}