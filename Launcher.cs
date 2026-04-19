using System;
using System.Diagnostics;
using System.IO;
using System.Reflection;
using System.Windows.Forms;

class Program
{
    [STAThread]
    static void Main(string[] args)
    {
        string currentDir = Path.GetDirectoryName(Assembly.GetExecutingAssembly().Location);
        string jarPath = Path.Combine(currentDir, "StudentApp.jar");
        
        if (!File.Exists(jarPath)) {
            MessageBox.Show("لم يتم العثور على ملف StudentApp.jar.\nالرجاء التأكد من وجوده بجانب هذا البرنامج.", "ملف ناقص", MessageBoxButtons.OK, MessageBoxIcon.Error);
            return;
        }

        ProcessStartInfo psi = new ProcessStartInfo();
        psi.FileName = "javaw"; // javaw runs java without opening a console window
        psi.Arguments = "-jar \"" + jarPath + "\"";
        psi.UseShellExecute = false;
        psi.CreateNoWindow = true; 
        psi.WorkingDirectory = currentDir;
        
        try {
            Process.Start(psi);
        } catch (Exception ex) {
            MessageBox.Show("تعذر تشغيل البرنامج. هل Java مثبت لديك؟\n\nالرسالة الاصلية: " + ex.Message, "خطأ في تشغيل جافا", MessageBoxButtons.OK, MessageBoxIcon.Error);
        }
    }
}
