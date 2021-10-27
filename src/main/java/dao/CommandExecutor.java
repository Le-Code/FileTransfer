package dao;

import listener.RuntimeExecListener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public abstract class CommandExecutor {

    public void executeString(String command, RuntimeExecListener listener) {
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

    public abstract void installHap(String hapPath, String dstPath, RuntimeExecListener listener);
    public abstract void sendFile(String filePath, String dstPath, RuntimeExecListener listener);
}
