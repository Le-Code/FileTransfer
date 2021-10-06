package utils;

import javax.swing.text.StringContent;
import java.io.*;

public class FileUtil {
    public static void writeContent(String str, String path) {
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(path));
            writer.write(str);
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            CloseUtil.close(writer);
        }
    }

    public static String readContent(String path) {
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(path));
            StringBuffer sb = new StringBuffer();
            String line = null;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            return sb.toString();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            CloseUtil.close(br);
        }
    }
}
