package dao.impl;

import dao.CommandExecutor;
import listener.RuntimeExecListener;
import utils.CloseUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class AdbCommandExecutor extends CommandExecutor {
    @Override
    public void installHap(List<String> hapPaths, String dstPath, boolean sign, RuntimeExecListener listener) {
        worker.pushWork(new Runnable() {
            @Override
            public void run() {
                List<String> execPaths = new ArrayList<>(hapPaths);
                if (sign) {
                    execPaths = signHap(hapPaths, listener);
                    if (execPaths == null) {
                        callOnFailure(listener, "signed error");
                        return;
                    }
                }
                String installCommand = configInfo.getAdbInstallCommand();
                if (installCommand == null) {
                    callOnFailure(listener, "adb install command not config");
                    return;
                }
                for (String hapPath : execPaths) {
                    String command = installCommand + " " + hapPath;
                    executeSyncString(command, listener);
                }
            }
        });
    }

    @Override
    public void sendFile(List<String> srcPaths, String dstPath, boolean reboot, RuntimeExecListener listener) {
        worker.pushWork(new Runnable() {
            @Override
            public void run() {
                String pushCommand = configInfo.getAdbFileSendCommand();
                if (pushCommand == null) {
                    callOnFailure(listener, "adb push file command not config");
                    return;
                }
                for (String srcPath : srcPaths) {
                    String command = pushCommand + " " + srcPath + " " + dstPath;
                    executeSyncString(command, listener);
                }
                if (reboot) {
                    String rebootCommand = configInfo.getAdbRebootCommand();
                    if (rebootCommand == null) {
                        callOnFailure(listener, "adb reboot command not config");
                        return;
                    }
                    executeSyncString(rebootCommand, null);
                }
            }
        });
    }

    @Override
    public void log(String filter, RuntimeExecListener listener) {
        String command = configInfo.getAdbLogCommand();
        if (command == null) {
            callOnFailure(listener, "adb command is not config");
            return;
        }
        Runnable logRunnable = new LogRunnable(command, filter, listener);
        logThread = new Thread(logRunnable);
        logThread.start();
    }
}
