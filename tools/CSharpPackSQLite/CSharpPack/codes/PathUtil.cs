using System.IO;

namespace CSharpPack.codes
{
    public class PathUtil
    {
        public static void ClearFolderByType(string dir_path, string type)
        {
            var di = new DirectoryInfo(dir_path);
            di.Create();
            var files = di.GetFiles();
            foreach (var file in files)
            {
                var fullFileName = file.Name.Split('.');
                if (fullFileName.Length <= 0)
                {
                    continue;
                }

                var fileExt = fullFileName[fullFileName.Length - 1];
                if (fileExt != type)
                {
                    continue;
                }

                var file_path = Path.Combine(dir_path, file.Name);
                File.Delete(file_path);
            }
        }

        public static void DirectoryCreate(string dirName, bool recursive = true)
        {
            if (Directory.Exists(dirName))
            {
                return;
            }

            var dirInfo = new DirectoryInfo(dirName);
            DirectoryCreate(dirInfo, recursive);
        }

        public static void DirectoryCreate(DirectoryInfo dirInfo, bool recursive = true)
        {
            if (dirInfo.Exists)
            {
                return;
            }

            if (recursive)
            {
                if (null != dirInfo.Parent && !dirInfo.Parent.Exists)
                {
                    DirectoryCreate(dirInfo.Parent);
                }
            }

            dirInfo.Create();
        }

        public static void DirectoryDelete(string dirName, bool recursive = true)
        {
            if (!Directory.Exists(dirName))
            {
                return;
            }

            // Delete all files and sub-folders?
            if (recursive)
            {
                // Yep... Let's do this
                var subfolders = Directory.GetDirectories(dirName);
                foreach (var s in subfolders)
                {
                    DirectoryDelete(s, recursive);
                }
            }

            // Get all files of the folder
            var files = Directory.GetFiles(dirName);
            foreach (var f in files)
            {
                // Get the attributes of the file
                var attr = File.GetAttributes(f);

                // Is this file marked as 'read-only'?
                if ((attr & FileAttributes.ReadOnly) == FileAttributes.ReadOnly)
                {
                    // Yes... Remove the 'read-only' attribute, then
                    File.SetAttributes(f, attr ^ FileAttributes.ReadOnly);
                }

                // Delete the file
                File.Delete(f);
            }

            // When we get here, all the files of the folder were
            // already deleted, so we just delete the empty folder
            Directory.Delete(dirName);
        }

        public static void DirectoryTouch(string dirName)
        {
            if (Directory.Exists(dirName))
            {
                return;
            }

            DirectoryCreate(dirName);
        }

        public static bool IsDirectory(string path)
        {
            return Directory.Exists(path) &&
                   (File.GetAttributes(path) & FileAttributes.Directory) == FileAttributes.Directory;
        }
    }
}