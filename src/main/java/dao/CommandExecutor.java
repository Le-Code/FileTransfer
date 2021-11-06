package dao;

import entity.ConfigInfo;
import entity.WorkerEvent;
import listener.RuntimeExecListener;
import utils.CommandHelp;
import utils.FileUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public abstract class CommandExecutor {

    protected ExecWorker worker;

    public void setWorker(ExecWorker worker) {
        this.worker = worker;
    }

    public void executeSyncString(String command, RuntimeExecListener listener) {
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
