package utils;

import javax.swing.text.StringContent;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

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
        File recordFile = new File(path);
        if (!recordFile.exists()) {
            return null;
        }
        try {
            br = new BufferedReader(new FileReader(recordFile));
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

    public static List<String> readLineContent(String path) {
        BufferedReader br = null;
        File recordFile = new File(path);
        if (!recordFile.exists()) {
            return null;
        }
        try {
            List<String> contents = new ArrayList<>();
            br = new BufferedReader(new FileReader(recordFile));
            String line = null;
            while ((line = br.readLine()) != null) {
                contents.add(line);
            }
            return contents;
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
