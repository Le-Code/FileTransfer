package dao;

import entity.ConfigInfo;
import entity.InputStreamEvent;
import listener.RuntimeExecListener;
import utils.CloseUtil;
import utils.FileUtil;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public abstract class CommandExecutor {

    protected ExecWorker worker;

    public void setWorker(ExecWorker worker) {
        this.worker = worker;
    }

    public void terminateCommand(RuntimeExecListener listener) {
        try {
            String[] command = {"cmd", "ctrl", "c"};
            Runtime.getRuntime().exec(command);
        } catch (IOException e) {
            e.printStackTrace();
            listener.onFailure(e.getMessage());
        }
    }

    private void callOnSuccess(RuntimeExecListener listener, String txt) {
        if (listener != null) {
            listener.onSuccess(txt);
        }
    }

    private void callOnFailure(RuntimeExecListener listener, String txt) {
        if (listener != null) {
            listener.onFailure(txt);
        }
    }

    public void executeSyncString(String command, RuntimeExecListener listener) {
        Process process = null;
        BufferedReader br = null;
        if (command == null || command.isEmpty()) {
            callOnFailure(listener, "command is empty");
            return;
        }
        try {
            callOnSuccess(listener, "start exec: " + command);
            process = Runtime.getRuntime().exec(command);

            // handle ErrorStream
            worker.pushInputStreamEvent(
                    new InputStreamEvent(process.getErrorStream(), "ErrorStream", listener));

            br = new BufferedReader(new InputStreamReader(process.getInputStream(), "GBK"));
            process.getOutputStream().flush();
            String line = null;
            while ((line = br.readLine()) != null) {
                callOnSuccess(listener, line);
            }
            callOnSuccess(listener, "over exec: " + command);
        } catch (IOException e) {
            e.printStackTrace();
            callOnFailure(listener, e.getMessage());
        } finally {
            CloseUtil.close(br);
            if (process != null) {
                CloseUtil.close(process.getOutputStream());
            }
        }
    }

    public void executeAsyncCommands(List<String> commands, RuntimeExecListener listener) {
        if (commands.isEmpty()) {
            listener.onFailure("application error");
            return;
        }
        worker.pushWork(new Runnable() {
            @Override
            public void run() {
                for (String command : commands) {
                    executeSyncString(command, listener);
                }
            }
        });
    }

    public void executeAsyncString(String command, RuntimeExecListener listener) {
        if (worker == null) {
            listener.onFailure("application error");
            return;
        }
        worker.pushWork(new Runnable() {
            @Override
            public void run() {
                executeSyncString(command, listener);
            }
        });
    }

    protected List<String> signHap(List<String> hapPaths, RuntimeExecListener listener) {
        List<String> signedPaths = new ArrayList<>();
        ConfigInfo configInfo = ConfigInfo.getInstance();
        if (!configInfo.checkSignInfo()) {
            return null;
        }
        for (String hapPath : hapPaths) {
            int lastSepIdx = hapPath.lastIndexOf(File.separator);
            String dir = hapPath.substring(0, lastSepIdx);
            int lastDotIdx = hapPath.lastIndexOf(".");

            String suffix = hapPath.substring(lastDotIdx);
            String fileName = hapPath.substring(lastSepIdx + 1, lastDotIdx);
            String signedName = fileName + "_signed" + suffix;
            String signedPath = dir + File.separator + signedName;

            signedPaths.add(signedPath);
            String command = "jar " + configInfo.getSignJarPath() + " " +
                             "username " + configInfo.getSignUserName() + " " +
                             "password " + configInfo.getSignPasswd() + " " +
                             "input " + hapPath + " " +
                             "output " + signedPath;
            executeSyncString(command, listener);
        }
        return signedPaths;
    }

    public void executeFile(File file, RuntimeExecListener listener) {
        if (!file.exists()) {
            listener.onFailure("file not exist");
            return;
        }
        worker.pushWork(new Runnable() {
            @Override
            public void run() {
                try {
                    String fileName = file.getAbsolutePath();
                    String[] command = {"cmd.exe","/C", "Start", fileName};
                    Runtime.getRuntime().exec(command);
                } catch (IOException e) {
                    e.printStackTrace();
                    callOnFailure(listener, e.getMessage());
                }
            }
        });
    }

    public abstract void installHap(List<String> hapPaths, String dstPath, boolean sign, RuntimeExecListener listener);
    public abstract void sendFile(List<String> srcPaths, String dstPath, boolean reboot, RuntimeExecListener listener);
}
