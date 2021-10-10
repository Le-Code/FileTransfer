package utils;

import listener.RuntimeExecListener;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class CommandHelp {
    public static void Exec(String command, RuntimeExecListener listener) {
        try {
            Runtime runtime = Runtime.getRuntime();
            BufferedReader br = new BufferedReader(new InputStreamReader(runtime.exec(command).getInputStream()));
            StringBuffer sb = new StringBuffer();
            String line = null;
            while ((line = br.readLine()) != null) {
                sb.append(line + "\n");
            }
            if (listener != null) {
                listener.onSuccess(sb.toString());
            }
        } catch (IOException e) {
            e.printStackTrace();
            if (listener != null) {
                listener.onFailure(e.getMessage());
            }
        }
    }
}
