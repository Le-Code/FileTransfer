package dao;

import entity.ConfigInfo;
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
//        BufferedReader br = null;
        BufferedInputStream bis = null;
        try {
            callOnSuccess(listener, "start exec: " + command);
            process = Runtime.getRuntime().exec(command);
            bis = new BufferedInputStream(process.getInputStream());
            process.getOutputStream().flush();
            byte[] buffer = new byte[1024];
            int len = -1;
            while ((len = bis.read(buffer, 0, 1024)) != -1) {
                String line = new String(buffer);
                callOnSuccess(listener, line);
            }
            callOnSuccess(listener, "over exec: " + command);
        } catch (IOException e) {
            e.printStackTrace();
            callOnFailure(listener, e.getMessage());
        } finally {
            CloseUtil.close(bis);
            CloseUtil.close(process.getOutputStream());
        }
    }

    public void executeSyncCommands(List<String> commands, RuntimeExecListener listener) {
        StringBuffer sb = new StringBuffer();
        for (String command : commands) {
            sb.append(command + FileUtil.getLineSep());
        }
        executeSyncString(sb.toString(), listener);
    }

    public void executeAsyncCommands(List<String> commands, RuntimeExecListener listener) {
        StringBuffer sb = new StringBuffer();
        for (String command : commands) {
            sb.append(command + FileUtil.getLineSep());
        }
        executeAsyncString(sb.toString(), listener);
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

    public abstract void installHap(List<String> hapPaths, String dstPath, boolean sign, RuntimeExecListener listener);
    public abstract void sendFile(List<String> srcPaths, String dstPath, boolean reboot, RuntimeExecListener listener);
}
